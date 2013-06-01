<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Edit Description - Virtual Judge</title>
		<s:include value="/header.jsp" />

		<style type="text/css" media="screen">
			#editorsForm {
				height: 520px;
				overflow: auto;
				border: 0;
				margin: 0;
				padding: 0;
			}
		</style>
		<script type="text/javascript" src="javascript/jquery.cookie.js"></script>
		<script type="text/javascript" src="ckeditor/ckeditor.js"></script>

		<script type="text/javascript" src="javascript/editDescription.js?<%=application.getAttribute("version")%>"></script>
	</head>

	<body>
		<s:include value="/top.jsp" />
		<table>
			<tr>
				<td>Title:</td>
				<td><s:property value="%{problem.title}" /></td>
			</tr>
			<tr>
				<td>Time Limit:</td>
				<td><s:property value="%{problem.timeLimit}" /> MS</td>
			</tr>
			<tr>
				<td>Memory Limit:</td>
				<td><s:property value="%{problem.memoryLimit}" /> KB</td>
			</tr>
			<tr>
				<td>Source:</td>
				<td>${problem.source}</td>
			</tr>
		</table>
		<div id="topSpace"></div>
		<form id="editorsForm" action="problem/editDescription.action" method="post">
			<div id="tabs">
				<ul>
					<li><a href="#a_remarks">Remarks</a></li>
					<li><a href="#a_description">Description</a></li>
					<li><a href="#a_input">Input</a></li>
					<li><a href="#a_output">Output</a></li>
					<li><a href="#a_sampleInput">Sample Input</a></li>
					<li><a href="#a_sampleOutput">Sample Output</a></li>
					<li><a href="#a_hint">Hint</a></li>
				</ul>
				<div id="a_remarks">
					<s:textarea id="remarks" name="description.remarks" value="" cols="160" rows="27" />
				</div>
				<div id="a_description">
					<s:textarea id="description" name="description.description" cols="120" rows="15" />
				</div>
				<div id="a_input">
					<s:textarea id="input" name="description.input" cols="120" rows="15" />
				</div>
				<div id="a_output">
					<s:textarea id="output" name="description.output" cols="120" rows="15" />
				</div>
				<div id="a_sampleInput">
					<s:textarea id="sampleInput" name="description.sampleInput" cols="120" rows="15" />
				</div>
				<div id="a_sampleOutput">
					<s:textarea id="sampleOutput" name="description.sampleOutput" cols="120" rows="15" />
				</div>
				<div id="a_hint">
					<s:textarea id="hint" name="description.hint" cols="120" rows="15" />
				</div>
			</div>
			<s:hidden name="id" value="%{problem.id}" />
			<s:hidden name="redir" />
			<input class="bnt1" type="submit" value="Submit" />
			<input class="bnt1" type="button" value="Cancel" onclick="history.go(-1)" />
			<s:actionerror />
		</form>
		<div id="bottomSpace"></div>

		<s:include value="/bottom.jsp" />
	</body>

</html>
