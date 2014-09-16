<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<div id="status" style="font-size:14px;padding:12px 0">
	<form id="form_status" style="font-size:12px">
		Username:<input type="text" name="un" value="${un}" />&nbsp;&nbsp;
		Problem:<s:select name="num" list="%{numList}" cssStyle="width:160px" />&nbsp;&nbsp;
		Result:<s:select name="res" cssStyle="width:160px" list="#{'0':'All','1':'Accepted','2':'Presentation Error','3':'Wrong Answer','4':'Time Limit Exceed','5':'Memory Limit Exceed','6':'Output Limit Exceed','7':'Runtime Error','8':'Compile Error','9':'Unknown Error','10':'Queuing && Judging','11':'Submit Error'}" />&nbsp;&nbsp;
		<input type="submit" value="Filter" id="filter"/>&nbsp;&nbsp;
		<input type="button" value="Reset" id="reset" />
	</form>

	<table cellpadding="0" cellspacing="0" border="0" class="display" id="table_status" style="text-align:center">
		<thead>
			<tr>
				<th>RunID</th>
				<th>User</th>
				<th>Problem</th>
				<th>Result</th>
				<th style="text-align:right;padding:3px">Memory</th>
				<th style="text-align:right;padding:3px">Time</th>
				<th>Language</th>
				<th style="text-align:right;padding:3px">Length</th>
				<th>Submit Time</th>
				<th></th>
				<th></th>
				<th></th>
				<th></th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td colspan="13">Loading data from server</td>
			</tr>
		</tbody>
	</table>
	<s:hidden name="isSup" />

</div>
