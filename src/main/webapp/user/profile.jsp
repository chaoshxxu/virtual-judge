<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html>
	<head>
		<%@ include file="/header.jsp" %>
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
