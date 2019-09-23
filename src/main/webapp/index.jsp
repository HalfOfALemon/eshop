<!DOCTYPE html>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" isELIgnored="false"%>
<html>
<body>
<h2>Hello World!</h2>
SpringMVC图片上传
<form name="form" action="manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_name">
    <input type="submit" value="springMVC上传文件">
</form>
<br>
富文本文件上传：
<form name="form" action="manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_name">
    <input type="submit" value="富文本上传文件">
</form>
<h1> 胡牛逼 </h1>

</body>
</html>
