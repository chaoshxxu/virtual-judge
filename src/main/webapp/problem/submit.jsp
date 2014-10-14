<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html>
	<head>
		<%@ include file="/header.jsp" %>
	    <title>Submit - Virtual Judge</title>
<%-- 		<script type="text/javascript" src="${contextPath}/javascript/submit.js?<%=application.getAttribute("version")%>"></script> --%>
	</head>

	<body>
        <s:hidden id="js_require_submit" />
		<s:include value="/top.jsp" />
		<div style="width:800px;MARGIN-RIGHT:auto;MARGIN-LEFT:auto;">
			<form id="form" action="${contextPath}/problem/submit.action" method="post">
				<table>
					<tr>
						<td>Problem:</td>
						<td><b>${problem.originOJ} ${problem.originProb}</b> - <a href="${contextPath}/problem/viewProblem.action?id=${problem.id}">${problem.title}</a></td>
					</tr>
					<tr>
						<td>Language:</td>
						<td><s:select name="language" listKey="key" listValue="value" list="languageList" theme="simple" cssClass="select" /></td>
					</tr>
					<tr>
						<td>Share code:</td>
						<td>
							<s:radio name="isOpen" list="#{'0':'No', '1':'Yes'}" theme="simple"></s:radio>
						</td>
					</tr>
				</table>
				<s:textarea name="tmp_source" rows="25" cols="100" />
				<input name="source" type="hidden" />
				<br />
				<input style="float:left" class="bnt1" type="submit" id="submit" value="Submit" />
				<input style="margin-left:20px;float:left" class="bnt1" type="button" value="Cancel" onclick="history.go(-1)" />
				<div id="errorMsg" style="color:red;font-weight:bold;float:left"><s:actionerror /></div>
				<input type="hidden" value="${problem.id}" name="id" />
			</form>
			<input type="hidden" value="${problem.originOJ}" name="oj" />
		</div>
		<s:include value="/bottom.jsp" />
	</body>
</html>
