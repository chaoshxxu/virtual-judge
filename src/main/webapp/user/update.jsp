<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html>
	<head>
		<%@ include file="/header.jsp" %>
	    <title>Update profile - Virtual Judge</title>
	</head>

	<body>
		<s:include value="/top.jsp" />
		<form action="${contextPath}/user/update.action" method="post">
			<table border="0" style="MARGIN-RIGHT:auto;MARGIN-LEFT: auto;">
				<tr>
					<td>Username:</td>
					<td><s:property value="username"/></td>
				</tr>	
				<tr>
					<td>Old Password:</td>
					<td><s:password name="password" size="50"/>(needed to update profile)</td>
				</tr>
				<tr>
					<td>New Password:</td>
					<td><s:password name="newpassword" size="50"/>(leave it blank if not to modify)</td>
				</tr>
				<tr>
					<td>Repeat Password:</td>
					<td><s:password name="repassword" size="50"/></td>
				</tr>
				<tr>
					<td>Nickname:</td>
					<td><s:textfield name="nickname" value="%{nickname}" size="50" /></td>
				</tr>
				<tr>
					<td>School:</td>
					<td><s:textfield name="school" value="%{school}" size="50" /></td>
				</tr>
				<tr>
					<td>QQ:</td>
					<td><s:textfield name="qq" value="%{qq}" size="50" /></td>
				</tr>
				<tr>
					<td>Email:</td>
					<td><s:textfield name="email" value="%{email}" size="50" /></td>
				</tr>
				<tr>
					<td class="form_title">Blog:</td>
					<td><s:textarea name="blog" value="%{blog}" rows="5" cols="41" /></td>
				</tr>
				<tr>
					<td class="form_title">Share code<br />by default:</td>
					<td><s:radio name="share" list="#{'0':'No', '1':'Yes'}" value="%{share}" theme="simple" /></td>
				</tr>
				<tr>
					<td></td>
					<td>
						<input type="hidden" name="uid" value="${uid}" />
						<input type="hidden" name="username" value="${username}" />
						<input type="hidden" value="${redir}" name="redir" />
						<input type="submit" value="Submit" />
						<input type="button" value="Cancel" onclick="history.go(-1);" />
					</td>
				</tr>
			</table>
			<center><s:actionerror /></center>
		</form>
		<s:include value="/bottom.jsp" />
	</body>
</html>
