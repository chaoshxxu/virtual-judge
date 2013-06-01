<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<s:include value="/header.jsp" />
		<title>Contests - Virtual Judge</title>
		<style type="text/css" media="screen">
			.dataTables_info { padding-top: 0; }
			.dataTables_paginate { padding-top: 0; }
			.css_right { float: right; }
			#example_wrapper .fg-toolbar { font-size: 0.8em }
			#theme_links span { float: left; padding: 2px 10px; }
		</style>
		<script type="text/javascript" src="javascript/jquery.cookie.js"></script>
		<script type="text/javascript" src="javascript/listContest.js?<%=application.getAttribute("version")%>"></script>
	</head>

	<body>
		<s:include value="/top.jsp" />
		<s:actionerror />
		
		<div id="head_status" style="float:right;display:none">
			<input name="contestType" id="contestType0" checked="checked" value="0" onclick="this.blur()" type="radio" /><label for="contestType0"><img src="images/contest.jpg" height="20" title="Real contest" /></label>
			<input name="contestType" id="contestType1" value="1" onclick="this.blur()" type="radio" /><label for="contestType1"><img src="images/replay.png" height="20" title="Replay" /></label>
			<span class="Scheduled">&nbsp;&nbsp;&nbsp;&nbsp;Scheduled:</span><input type="checkbox" name="scheduled" checked="checked" value="1" />  
			<span class="Running">&nbsp;&nbsp;Running:</span><input type="checkbox" name="running" checked="checked" value="2" />  
			<span class="Ended">&nbsp;&nbsp;Ended:</span><input type="checkbox" name="ended" checked="checked" value="3" />  
		</div>
		
		<div class="ptt" style="margin-bottom:12px;position:relative;">
			Contest List
			<div style="float:right;position:absolute;right:0px;top:0">
				<a href="contest/statistic.action" id="statistic_contest" target="_blank" style="color:black">Statistic</a>
				<a href="contest/toAddContest.action" class="login" id="add_contest" style="color:black">Add a contest</a>
			</div>
		</div>
		
		<table cellpadding="0" cellspacing="0" border="0" class="display" id="listContest">
			<thead>
				<tr><th>ID</th><th>Title</th><th>Begin Time</th><th>Length</th><th>Type</th><th style="width:85px">Manager</th><th></th><th></th><th></th></tr>
			</thead>
			<tbody>
				<tr>
					<td colspan="9">Loading data from server</td>
				</tr>
			</tbody>
		</table>
		
		<s:hidden name="cid" id="cid" />
		<div id="dialog-form-contest-login" style="display:none" title="Login contest">
			<p class="validateTips"></p>
			<fieldset>
				<label for="contest_password">Password *</label>
				<input type="password" id="contest_password" class="text ui-widget-content ui-corner-all" />
			</fieldset>
		</div>

		<s:include value="/bottom.jsp" />
	</body>
</html>
