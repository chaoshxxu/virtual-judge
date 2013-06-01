<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<s:include value="/header.jsp" />
	    <title>Online Users - Virtual Judge</title>
	</head>

	<body>
		<s:include value="/top.jsp" />
		<table border="1" style="MARGIN-RIGHT:auto;MARGIN-LEFT:auto;">
			<tr>
				<td width="200">sessionId:</td>
				<td width="800"><s:property value="session.id"/></td>
			</tr>	
			<tr>
				<td>IP:</td>
				<td>
					<a href="http://www.ip138.com/ips.asp?ip=${ip}">
						${ip}
					</a>
				</td>
			</tr>
			<tr>
				<td>Username:</td>
				<td>
					<a href="user/profile.action?uid=<s:property value="user.id"/>">
						<s:property value="user.username"/>
					</a>
				</td>
			</tr>
			<tr>
				<td>Create Time:</td>
				<td><s:date name="creationTime" format="yyyy-MM-dd HH:mm:ss" /></td>
			</tr>
			<tr>
				<td>Last Accessed Time:</td>
				<td><s:date name="lastAccessedTime" format="yyyy-MM-dd HH:mm:ss" /></td>
			</tr>
			<tr>
				<td>Max Inactive Interval:</td>
				<td><s:property value="session.maxInactiveInterval"/></td>
			</tr>
			<tr>
				<td>Referer:</td>
				<td>
					<a href="${referer}" >
						${referer}
					</a>
				</td>
			</tr>
			<tr>
				<td>User Agent:</td>
				<td>${userAgent}</td>
			</tr>
		</table>
		
		<s:include value="/bottom.jsp" />
	</body>
</html>
