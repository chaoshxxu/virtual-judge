<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<s:include value="/header.jsp" />
		<title><s:property value="contest.title" escape="false" /> - Virtual Judge</title>

		<style type="text/css" media="screen">
			table.blue_border td {border:1px solid #A6C9E2;}
		</style>
	
		<script type="text/javascript" src="dwr/interface/judgeService.js"></script>
		<script type='text/javascript' src='javascript/engine.js'></script>
		<script type='text/javascript' src='dwr/util.js'></script>
		<script type="text/javascript" src="javascript/editContest.js?<%=application.getAttribute("version")%>"></script>
	</head>

	<body>
		<s:include value="/top.jsp" />
		<div class="ptt">Modify Contest</div>
		<form id="form" action="contest/editContest.action" method="post" enctype="multipart/form-data">
		<div style="position:relative"> 
		<div id="div_left" style="float:left:position:absolute;top:0;left:0">
			<table class="blue_border" style="border:1px solid #A6C9E2;border-collapse:collapse;width:545px">
				<tr>
					<td class="form_title" style="width:120px">Type:</td>
					<td class="form_value"><s:radio name="contestType" list="#{'0':'Real Contest', '1':'Replay'}" onclick="this.blur()" ></s:radio></td>
				</tr>
				<tr>
					<td class="form_title">Title:</td>
					<td class="form_value"><s:textfield name="contest.title" cssStyle="width:100%" /></td>
				</tr>
				<tr>
					<td class="form_title">Begin Time:</td>
					<td class="form_value">
						<s:textfield name="_beginTime" cssStyle="width:79px" readonly="true" />
						<s:textfield name="hour" cssStyle="width:25px" maxlength="2" cssClass="clk_select" />:<s:textfield name="minute" cssStyle="width:25px" maxlength="2" cssClass="clk_select" />:00
						<span class="replay_element" style="color:green"><br/>Fill the begin time of the origin contest</span>
					</td>
				</tr>
				<tr>
					<td class="form_title">Length:</td>
					<td class="form_value">
						<s:textfield name="d_day" cssStyle="width:63px" cssClass="clk_select" />天 <s:textfield name="d_hour" cssStyle="width:25px" maxlength="2" cssClass="clk_select" />:<s:textfield name="d_minute" cssStyle="width:25px" maxlength="2" cssClass="clk_select" />:00
					</td>
				</tr>
				<tr class="real_contest_element" style="display:none">
					<td class="form_title">Password:</td>
					<td class="form_value">
						<s:password name="contest.password" />
						<span style="color:green">Leave blank to make it public</span>
					</td>
				</tr>
				<tr class="replay_element" style="display:none">
					<td class="form_title">Ranklist<br />(xls/csv):</td>
					<td class="form_value">
						<s:file name="ranklistFile" />
                        <a href="http://hi.baidu.com/xh176233756/item/a0bf0f3304afc3c11a9696f7" target="_blank"><img src="http://www.iconpng.com/png/cristal-intense/aide.png" border="0" height="20" /></a>
                        <a href="https://www.youtube.com/watch?v=MkjIG7cbp5M" target="_blank"><img src="images/youtube.png" border="0" height="20" /></a>
					</td>
				</tr>
				<tr class="real_contest_element">
					<td class="form_title">Time Machine:</td>
					<td class="form_value">
						<s:radio name="contest.enableTimeMachine" list="#{'0':'Disable', '1':'Enable'}" />
					</td>
				</tr>
				<tr>
					<td class="form_title">Description:</td>
					<td class="form_value"><s:textarea name="contest.description" cssStyle="width:100%" rows="5" /></td>
				</tr>
				<tr>
					<td class="form_title">Announcement:</td>
					<td class="form_value"><s:textarea name="contest.announcement" cssStyle="width:100%" rows="3" /></td>
				</tr>
				<tr>
					<td colspan="2">
						<div id="errorMsg" style="color:red;font-weight:bold;float:right"><s:actionerror /></div>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="hidden" name="cid" value="${cid}" />
						<input style="margin-left:20px;float:right" class="bnt1" type="button" value="Cancel" onclick="history.go(-1)" />
						<input style="margin-left:20px;float:right" class="bnt1" type="button" value="Reset" onclick="document.forms[0].reset();location.reload();" />
						<input style="float:right" id="submit" class="bnt1" type="submit" value="Submit" />
					</td>
				</tr>
			</table>
		</div>

		<div id="div_right" style="float:left;position:absolute;top:0;left:550px">
			<table id="addTable" style="width:700px">
			<thead>
				<tr>
					<th style="width:30px"><a id="addBtn" href="javascript:void(0)"><img height="18" src="images/ico_add.png" border="0"/></a></th>
					<th style="width:97px">OJ</th>
					<th style="width:80px">ProbNum</th>
					<th style="width:153px">Alias</th>
					<th style="width:24px"></th>
					<th style="text-align:left">Title</th>
				</tr>
			</thead>
			<s:iterator value="OJs" status="stat">	
				<tr class="tr_problem">
					<td><a class="deleteRow" href="javascript:void(0)"><img height="18" src="images/ico_delete.gif" border="0"/></a></td>
					<td><s:select name="OJs" value="%{OJs[#stat.index]}" list="OJList" /><s:hidden name="pids" value="%{pids[#stat.index]}" /></td>
					<td><s:textfield cssStyle="width:80px" name="probNums" value="%{probNums[#stat.index]}" /></td>
					<td><s:textfield name="titles" value="%{titles[#stat.index]}" /></td>
					<td></td>
					<td></td>
				</tr>
			</s:iterator>
				<tr id="addRow" class="tr_problem" style="display:none">
					<td><a class="deleteRow" href="javascript:void(0)"><img height="18" src="images/ico_delete.gif" border="0"/></a></td>
					<td><s:select name="OJs" list="OJList" /><s:hidden name="pids" /></td>
					<td><s:textfield cssStyle="width:80px" name="probNums" /></td>
					<td><s:textfield name="titles" /></td>
					<td></td>
					<td></td>
				</tr>
			</table>
		</div>
		</div>
		<s:hidden name="beginTime" />
		</form>
		<s:include value="/bottom.jsp" />
	</body>
</html>
