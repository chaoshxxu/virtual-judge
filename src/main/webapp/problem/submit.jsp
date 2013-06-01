<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<s:include value="/header.jsp" />
	    <title>Submit - Virtual Judge</title>
		<script type="text/javascript" src="javascript/jquery.cookie.js"></script>
		<script type="text/javascript" src="javascript/base64.js?<%=application.getAttribute("version")%>"></script>
		<script type="text/javascript" src="javascript/submit.js?<%=application.getAttribute("version")%>"></script>
	</head>

	<body>
		<s:include value="/top.jsp" />
		<div style="width:800px;MARGIN-RIGHT:auto;MARGIN-LEFT:auto;">
			<form id="form" action="problem/submit.action" method="post">
				<table>
					<tr>
						<td>Problem:</td>
						<td><b>${problem.originOJ} ${problem.originProb}</b> - <a href="problem/viewProblem.action?id=${problem.id}">${problem.title}</a></td>
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
