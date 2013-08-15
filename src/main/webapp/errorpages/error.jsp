<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%
String basePath = (String)application.getAttribute("basePath");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
    	<base href="<%=basePath%>" />
	    <title>Virtual Judge</title>
		<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
		<link rel="shortcut icon" href="<%=basePath%>images/logo.ico" />
		<link rel="stylesheet" type="text/css" href="css/global.css?<%=application.getAttribute("version")%>" />
	</head>

	<body>
		<table width="100%" border="0" class="banner">
			<tr>
				<td>
					<a href="toIndex.action">Home</a>
				</td>
				<td>
					<a href="problem/toListProblem.action">Problems</a>
				</td>
				<td>
					<a href="problem/status.action">Status</a>
				</td>
				<td>
					<a href="contest/toListContest.action">Contest</a>
				</td>
                <td></td>
                <td></td>
			</tr>
		</table>
		<br /><br />

		<center>
			<h1>Error occured!</h1>
			<br/><img src="images/beiju.jpg" />
			<br/><img src="images/lr.gif" />&nbsp;&nbsp;&nbsp;
			<img src="images/beiju1.gif" />&nbsp;&nbsp;&nbsp;
			<img src="images/beiju2.gif" />&nbsp;&nbsp;&nbsp;
			<img src="images/beiju3.gif" />&nbsp;&nbsp;&nbsp;
			<img src="images/rl.gif" />
		</center>
	</body>
</html>
