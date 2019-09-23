package com.eshop.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.eshop.common.Const;
import com.eshop.common.ServerResponce;
import com.eshop.dao.*;
import com.eshop.pojo.*;
import com.eshop.service.IOrderService;
import com.eshop.util.BigDecimalUtil;
import com.eshop.util.DateTimeUtil;
import com.eshop.util.FTPUtil;
import com.eshop.util.PropertiesUtil;
import com.eshop.vo.OrderItemVo;
import com.eshop.vo.OrderProductVo;
import com.eshop.vo.OrderVo;
import com.eshop.vo.ShippingVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {
    private static final Logger logger= LoggerFactory.getLogger(OrderServiceImpl.class);
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponce createOrder(Integer userId, Integer shipping){
        //从购物车中获取已经选中的数据
        List<Cart> cartList=cartMapper.selectCheckedCartByUserId(userId);
        //计算这个订单总价，并获取订单项
        ServerResponce  serverResponce= this.getCartOrderItem(userId, cartList);
        if(!serverResponce.isSuccess()){
            return serverResponce;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponce.getData();
        if(CollectionUtils.isEmpty(orderItemList)){
            return ServerResponce.createByErrorMessage("购物车为空");
        }
        BigDecimal payment=this.getOrderTotalPrice(orderItemList);
        //生成订单，保存到数据库
        Order order = this.assembleOrder(userId, shipping, payment);
        if(order==null){
            return ServerResponce.createByErrorMessage("生成订单错误");
        }
        //订单项插入订单号
        for(OrderItem orderItem:orderItemList){
            orderItem.setOrderNo(order.getOrderNo());
            orderItem.setUpdateTime(new Date());
        }
        //mybatis批量插入订单项
        orderItemMapper.batchInsert(orderItemList);
        //减少产品库存
        this.reduceProductStock(orderItemList);
        //清空购物车
        this.cleanCart(cartList);

        /*返回给前端数据 ：VO 对象*/
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        return ServerResponce.createBySuccess(orderVo);
    }

    /**
     * 组装订单Vo
     * @param order
     * @param orderItemList
     * @return
     */
    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList){
        OrderVo orderVo=new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());

        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());

        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));

        //组装地址
        orderVo.setShippingId(order.getShippingId());
        Shipping shipping=shippingMapper.selectByPrimaryKey(order.getShippingId());
        if(shipping!=null){
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(this.assembleShippingVo(shipping));
        }
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        //组装OrderItem
        List<OrderItemVo> orderItemVoList=Lists.newArrayList();
        for(OrderItem orderItem:orderItemList){
            OrderItemVo orderItemVo=this.assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }

    /**
     * 组装订单项Vo
     * @param orderItem
     * @return
     */
    private OrderItemVo assembleOrderItemVo(OrderItem orderItem){
        OrderItemVo orderItemVo =new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVo;
    }
    /**
     * 组装地址Vo
     * @param shipping
     * @return
     */
    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo=new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        return shippingVo;
    }

    /**
     * 清空购物车
     * @param cartList
     */
    private void cleanCart(List<Cart> cartList){
        for(Cart cartItem:cartList){
            cartMapper.deleteByPrimaryKey(cartItem.getId());
        }
    }
    /**
     * 减少库存
     * @param orderItemList
     */
    private void reduceProductStock(List<OrderItem> orderItemList){
        for(OrderItem orderItem:orderItemList){
            Product product=productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock()-orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    /**
     *组装并创建一个订单到数据库
     * @param userId
     * @param shippingId
     * @param payment
     * @return
     */
    private Order assembleOrder(Integer userId,Integer shippingId,BigDecimal payment){
        Order order=new Order();
        long orderNo=this.generateorderNo();
        //组装
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setShippingId(shippingId);
        order.setPayment(payment);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPostage(0);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setCloseTime(new Date());
        order.setUpdateTime(new Date());
        //支付时间；发货时间；交易完成时间；交易关闭时间；
        int rowCount = orderMapper.insert(order);
        if(rowCount>0){
            return order;
        }
        return null;
    }

    /**
     * 生成订单号
     * @return
     */
    private long generateorderNo(){
        long currentTime = System.currentTimeMillis();
        Random random=new Random();
        return currentTime+ random.nextInt(10);
    }
    /**
     * 计算订单总价
     * @param orderItemList
     * @return
     */
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList){
        BigDecimal payment=new BigDecimal("0");
        for(OrderItem orderItem:orderItemList){
            payment=BigDecimalUtil.add(payment.doubleValue(),orderItem.getCurrentUnitPrice().doubleValue());
        }
        return payment;
    }

    /**
     * 将选中的购物车项，组装子订单明细：订单总价等，但是没有存在数据库中
     * @param userId
     * @param cartList
     * @return
     */
    private ServerResponce getCartOrderItem(Integer userId,List<Cart> cartList){
        List<OrderItem> orderItemList=Lists.newArrayList();
        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponce.createByErrorMessage("购物车为空");
        }
        //校验购物车的产品状态和数量
        for(Cart cartItem:cartList){
            OrderItem orderItem=new OrderItem();
            Product product=productMapper.selectByPrimaryKey(cartItem.getId());
            //校验产品状态
            if(Const.ProductStatusEnmu.ON_SALE.getCode() != product.getStatus()){
                return ServerResponce.createByErrorMessage("产品："+product.getName()+"不是在售状态");
            }
            //校验产品数量
            if(cartItem.getQuantity() > product.getStock()){
                return ServerResponce.createByErrorMessage("产品："+product.getStock()+"库存不足");
            }
            //组装orderItem，orderItemList
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartItem.getQuantity()));
            orderItem.setCreateTime(new Date());
            orderItem.setUpdateTime(new Date());

            orderItemList.add(orderItem);
        }
        return ServerResponce.createBySuccess(orderItemList);
    }

    /**
     * 取消订单
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponce<String> cancel(Integer userId, Long orderNo) {
        Order order=orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if(order==null){
            return ServerResponce.createByErrorMessage("该用户没有此订单");
        }
        if(Const.OrderStatusEnum.NO_PAY.getCode()!=order.getStatus()){
            return ServerResponce.createByErrorMessage("该订单已付款，不可取消");
        }
        Order updateOrder=new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        updateOrder.setUpdateTime(new Date());
        int rouCount = orderMapper.updateByPrimaryKeySelective(order);
        if(rouCount>0){
            return ServerResponce.createBySuccess("取消订单成功");
        }
        return ServerResponce.createByErrorMessage("取消订单失败");
    }

    /**
     * 在点击购物车的 去结算 ，得到订单的商品详情；这时数据库并没有产生订单，当在这个页面点击 提交订单 才会产生订单。
     * @param userId
     * @return
     */
    @Override
    public ServerResponce getOrderCartProduct(Integer userId){
        OrderProductVo orderProductVo=new OrderProductVo();
        //从购物车中获取数据
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        ServerResponce serverResponce = this.getCartOrderItem(userId, cartList);
        if(!serverResponce.isSuccess()){
            return serverResponce;
        }
        List<OrderItemVo> orderItemVoList=Lists.newArrayList();
        BigDecimal payment=new BigDecimal("0");

        List<OrderItem> orderItemList=(List<OrderItem>)serverResponce.getData();
        for(OrderItem orderItem:orderItemList){
            orderItemVoList.add(assembleOrderItemVo(orderItem));
            payment=BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponce.createBySuccess(orderProductVo);
    }

    /**
     * 得到订单详情
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponce<OrderVo> getOrderDetail(Integer userId, Long orderNo) {
        Order order=orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if(order!=null){
            List<OrderItem> orderItemList=orderItemMapper.selectByOrderNoAndUserId(orderNo,userId);
            OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
            return ServerResponce.createBySuccess(orderVo);
        }
        return ServerResponce.createByErrorMessage("没有找到该订单");
    }

    /**
     * 获得订单list
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponce<PageInfo> getOrderList(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList=orderMapper.selectOrderByUserId(userId);
        List<OrderVo> orderVoList = assembleOrderVolist(orderList, userId);

        PageInfo pageInfo=new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponce.createBySuccess(pageInfo);
    }
    private List<OrderVo> assembleOrderVolist(List<Order> orderList,Integer userId){
        List<OrderVo> orderVoList=Lists.newArrayList();
        for(Order order:orderList){
            List<OrderItem> orderItemList =Lists.newArrayList();
            if(userId==null){
                //todo 管理员查询的时候，不需要传userId
            }else {
                orderItemList = orderItemMapper.selectByOrderNoAndUserId(order.getOrderNo(), userId);
            }
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    /**
     * 用于创建支付
     * @param orderNo
     * @param userId
     * @param path
     * @return 订单编号，支付二维码的URL地址
     */
    @Override
    public ServerResponce pay(Long orderNo, Integer userId, String path){
        //用于返回给前端的订单号和二维码的地址
        Map<String,String> resultMap= Maps.newHashMap();
        //查询是否存在这个用户和订单号
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if(order==null){
            return ServerResponce.createByErrorMessage("用户没有该订单");
        }
        resultMap.put("orderNo",String.valueOf(order.getOrderNo()));

        /**
         * 填充订单信息,用于返回二维码
         */
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("eshop扫码支付，订单号：").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(order.getOrderNo()).append("购买商品共").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        /*填充订单*/
        List<OrderItem> orderItemList=orderItemMapper.selectByOrderNoAndUserId(orderNo,userId);
        for(OrderItem orderItem:orderItemList){
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(),new Double(100).doubleValue()).longValue(),
                    orderItem.getQuantity());
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(goods);
        }

        // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
        //GoodsDetail goods1 = GoodsDetail.newInstance("goods_id001", "xxx小面包", 1000, 1);
        // 创建好一个商品后添加至商品明细列表
        //goodsDetailList.add(goods1);

        // 继续创建并添加第一条商品信息，用户购买的产品为“黑人牙刷”，单价为5.00元，购买了两件
        //GoodsDetail goods2 = GoodsDetail.newInstance("goods_id002", "xxx牙刷", 500, 2);
        //goodsDetailList.add(goods2);

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                /**
                 * 下单成功，要把图片上传到upload，再上传到FTP服务器，最后返回一个地址给前端
                 */
                File folder=new File(path);
                if(!folder.exists()){
                    folder.setWritable(true);
                    folder.mkdirs();
                }
                // 需要修改为运行机器上的路径,  !!!!注意 path后面加 /
                String qrPath = String.format(path+"/qr-%s.png", response.getOutTradeNo());
                String qrFilePath=String.format("qr-%s.png", response.getOutTradeNo());
                //支付宝封装的一个上传方法
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);
                //上传到ftp服务器
                File targetFile =new File(path,qrFilePath);
                try {
                    FTPUtil.uploadFile("qr_code",Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    logger.error("上传二维码异常",e);
                }
                logger.info("qrPath:" + qrPath);
                //                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, filePath);
                String qrUrl=PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName();
                resultMap.put("qrUrl",qrUrl);
                return ServerResponce.createBySuccess(resultMap);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponce.createByErrorMessage("支付宝预下单失败");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponce.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponce.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    /**
     * 用于验证回调的正确性
     * @param params
     * @return
     */
    @Override
    public ServerResponce alipayCallback(Map<String,String> params){
        //订单号
        Long orderNo= Long.parseLong(params.get("out_trade_no"));
        //支付宝交易号
        String tradeNo=params.get("trade_no");
        //交易状态
        String tradeStatus=params.get("trade_status");
        //查询数据库是否存在该订单
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order==null){
            return ServerResponce.createByErrorMessage("非eshop商城订单，回调忽略");
        }
        //订单，已支付，重复调用了
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponce.createBySuccess("支付宝重复调用");
        }
        //这次回调，用户刚好支付，修改订单状态，支付时间，修改时间
        if(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            order.setUpdateTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }

        PayInfo payInfo=new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());//支付方式
        payInfo.setPlatformStatus(tradeStatus);//支付宝状态
        payInfo.setPlatformNumber(tradeNo);//支付宝订单号
        payInfo.setCreateTime(new Date());
        payInfo.setUpdateTime(new Date());
        payInfoMapper.insert(payInfo);
        return ServerResponce.createBySuccess();
    }

    @Override
    public ServerResponce<Boolean> queryOrderPayStatus(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if(order==null){
            return ServerResponce.createByErrorMessage("用户没有该订单");
        }
        //订单，已支付
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponce.createBySuccess();
        }
        return ServerResponce.createByError();
    }


}
