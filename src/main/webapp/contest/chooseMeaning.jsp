<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html>
	<head>
		<%@ include file="/header.jsp" %>
		<title>Edit Replay - Virtual Judge</title>
	</head>

	<body>
		<s:include value="/top.jsp" />
		<div class="ptt">Add Contest Replay</div>
		<div style="width:650px;font-size:14px;margin-left:auto;margin-right:auto;">
			<p style="margin-top:15px;font: 20px 'Lucida Grande',Verdana,Arial,Helvetica,sans-serif;">What do these cells mean:</p>
			<form id="form" action="${contextPath}/contest/addReplay.action" method="post">
			<table class="display" cellpadding="0" cellspacing="2" border="0">
				<s:iterator value="cellMeaningOptions" status="stat">
				<tr>
					<td style="text-align: center; background-color:#CCDDFF;"><s:property value="key"/></td>
					<td style="line-height:40px">: <s:select list="%{value}" name="selectedCellMeaning" cssStyle="width:450px"/></td>
				</tr>
				</s:iterator>	
				<tr>
					<td></td>
					<td>
						<input style="margin-left:20px;float:right" class="bnt1" type="button" value="Cancel" onclick="history.go(-1)" />
						<input style="float:right" id="submit" class="bnt1" type="submit" value="Submit" />
						<div id="errorMsg" style="color:red;font-weight:bold;float:right"><s:actionerror /></div>
					</td>
				</tr>
			</table>
			</form>
		</div>
		<s:include value="/bottom.jsp" />
	</body>
</html>
