<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<s:include value="/header.jsp" />
	    <title>Online Users - Virtual Judge</title>
		<script type="text/javascript" src="javascript/listOL.js?<%=application.getAttribute("version")%>"></script>
	</head>

	<body>
		<s:include value="/top.jsp" />
		
		<div class="ptt">Online Users</div>
		
		<table>
			<tr>
				<td style="font-weight:bold;text-align:right;padding-right:5px">Session number: </td>
				<td><s:property value="%{dataList.size()}" /></td>
			</tr>
			<tr>
				<td style="font-weight:bold;text-align:right;padding-right:5px">User number: </td>
				<td><s:property value="loginUsers" /></td>
			</tr>
			<tr>
				<td style="font-weight:bold;text-align:right;padding-right:5px">IP map size: </td>
				<td><s:property value="ipMapCnt" /></td>
			</tr>
		</table>
		
		
		<table id="listOL" cellpadding="0" cellspacing="0" border="0" class="display" style="text-align:center" >
			<thead>
				<tr>
					<th>Session ID</th>
					<th>Username</th>
					<th>IP</th>
					<th>Address</th>
					<th>Arrive Time</th>
					<th>Active Length</th>
					<th>Freeze Length</th>
					<th>Browser</th>
					<th>OS</th>
				</tr>
			</thead>

			<s:iterator value="dataList" status="stat">
			<tr>
				<td>
					<a href="stat/viewOL.action?id=<s:property value='dataList[#stat.index][0]' />">Detail</a>
				</td>
				<td>
					<a href="user/profile.action?uid=<s:property value='dataList[#stat.index][2]' />">
						<s:property value='dataList[#stat.index][1]' />
					</a>
				</td>
				<td>
					<s:property value="dataList[#stat.index][3]" />
				</td>
				<td>
					<s:property value="dataList[#stat.index][4]" />
				</td>
				<td><s:date name="dataList[#stat.index][5]" format="yyyy-MM-dd HH:mm:ss" /></td>
				<td><s:property value="dataList[#stat.index][6]" /></td>
				<td><s:property value="dataList[#stat.index][7]" /></td>
				<td><s:property value="dataList[#stat.index][8]" /></td>
				<td><s:property value="dataList[#stat.index][9]" /></td>
			</tr>
		</s:iterator>
		</table>

		<s:include value="/bottom.jsp" />
	</body>
</html>
