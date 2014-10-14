<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html>
	<head>
        <%@ include file="/header.jsp" %>
	    <title>Error! - Virtual Judge</title>
	</head>

	<body>
        <div id='cssmenu'>
            <ul>
                <li id="nav_home"><a href="${contextPath}/toIndex.action">Home</a></li>
                <li id="nav_problem"><a href="${contextPath}/problem/toListProblem.action">Problem</a></li>
                <li id="nav_status"><a href="${contextPath}/problem/status.action">Status</a></li>
                <li id="nav_contest" class='has-sub'><a href="${contextPath}/contest/toListContest.action">Contest</a>
                   <ul>
                      <li><a href='${contextPath}/contest/toAddContest.action' class="login">Add Contest</a></li>
                      <li><a href='${contextPath}/contest/statistic.action'>Statistic</a></li>
                   </ul>
                </li>
            </ul>
        </div>
		<br /><br />

		<center>
			<h1>Error occured!</h1>
			<br/><img src="${contextPath}/images/beiju.jpg" />
			<br/><img src="${contextPath}/images/lr.gif" />&nbsp;&nbsp;&nbsp;
			<img src="${contextPath}/images/beiju1.gif" />&nbsp;&nbsp;&nbsp;
			<img src="${contextPath}/images/beiju2.gif" />&nbsp;&nbsp;&nbsp;
			<img src="${contextPath}/images/beiju3.gif" />&nbsp;&nbsp;&nbsp;
			<img src="${contextPath}/images/rl.gif" />
		</center>
	</body>
</html>
