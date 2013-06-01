<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<s:include value="/header.jsp" />
	    <title>${user.username}'s profile - Virtual Judge</title>
	</head>

	<body>
		<s:include value="/top.jsp" />
		<table border="1" style="MARGIN-RIGHT:auto;MARGIN-LEFT:auto;">
			<tr>
				<td width="100">Username:</td>
				<td width="300"><s:property value="user.username"/></td>
			</tr>	
			<tr>
				<td>Nickname:</td>
				<td><s:property value="user.nickname"/></td>
			</tr>
			<tr>
				<td>School:</td>
				<td><s:property value="user.school"/></td>
			</tr>
			<tr>
				<td>QQ:</td>
				<td><s:property value="user.qq"/></td>
			</tr>
			<tr>
				<td>Email:</td>
				<td><s:property value="user.email"/></td>
			</tr>
			<tr>
				<td>Blog:</td>
				<td height="100	"><s:property value="user.blog"/></td>
			</tr>
		</table>
		<s:include value="/bottom.jsp" />
	</body>
</html>
