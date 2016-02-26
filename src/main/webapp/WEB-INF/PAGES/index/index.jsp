<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>高苏杨专用查班系统</title>
</head>
<body  >
<form id="indexForm" action="/login">
<script type="text/javascript">
    function refresh(){ 	document.getElementById("authImg").src = 'authImg?now=' + new Date(); }
</script>
    <div style="margin-left: 400px;margin-top: 200px">
        <img src="/authImg" width="160" height="80" ><br/><br/>
        验证码:<input type="text" name="verCode" ><br/><br/>
        <input type="submit" value="登录">
    </div>

<%--${indexResult}
<input  type="button" id="s" onclick="refresh()" value="刷新" />


    验证码:<input type="text" name="verCode"><br/>
    <input type="submit" value="登录">--%>
</form>
</body>



</html>
