<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<div id="overview">
	
	<table style="margin:auto" class="plm">
		<tr>
			<td class="alignRight"><b>Current Time: </b></td>
			<td class="alignLeft"><span class="currentTime"></span></td>
			<td class="alignRight"><b>Contest Type: </b></td>
			<td class="alignLeft"><s:if test="contest.password == null"><font color="blue">Public</font></s:if><s:else><font color="red">Private</font></s:else></td>
		</tr>
		<tr>
			<td class="alignRight"><b>Start Time: </b></td>
			<td class="alignLeft"><span class="plainDate">${beginTime}</span></td>
			<td class="alignRight"><b>Contest Status: </b></td>
			<td class="alignLeft">
				<s:if test="curDate.compareTo(contest.beginTime) < 0"><font color="blue">Scheduled</font></s:if>
				<s:elseif test="curDate.compareTo(contest.endTime) < 0"><font color="red">Running</font></s:elseif>
				<s:else><font color="green">Ended</font></s:else>
			</td>
		</tr>
		<tr>
			<td class="alignRight"><b>End Time: </b></td>
			<td class="alignLeft"><span class="plainDate">${endTime}</span></td>
			<td class="alignRight"><b>Manager: </b></td>
			<td class="alignLeft"><a href="user/profile.action?uid=<s:property value='contest.manager.id' />" ><s:property value="contest.manager.username" /></a></td>
		</tr>
	</table>


	<s:if test="dataList != null">
	<div id="contest_opt" style="text-align:center;margin-top:10px">
		<s:if test="curDate.compareTo(contest.endTime) > 0 || #session.visitor.sup == 1 || #session.visitor.id == contest.manager.id">
			<a id="clone_contest" href="contest/toAddContest.action?cid=${cid}" title="Create a contest using the same problems, in which you can see the original score board.">Clone this contest</a>
		</s:if>
		<s:if test="#session.visitor.sup == 1 || #session.visitor.id == contest.manager.id">
			<a href="contest/toEditContest.action?cid=${cid}">Edit</a>
			<a href='javascript:void(0)' onclick='comfirmDeleteContest(${cid})'>Delete</a>
			<s:if test="contestOver == 1">
				<a href="contest/exportSource.action?cid=${cid}">Export source code</a>
			</s:if>
		</s:if>
	</div>
	<table style="width:960px;margin-top:20px" cellpadding="0" cellspacing="0" border="0" class="display" id="viewContest">
		<thead>
			<tr>
				<th style="text-align: right;width:20px"></th>
				<th style="text-align: left;width:90px"></th>
				<th style="text-align: right;width:90px">ID</th>
				<s:if test="curDate.compareTo(contest.endTime) > 0 || #session.visitor.sup == 1 || #session.visitor.id == contest.manager.id">
					<th style="width:150px">Origin</th>
				</s:if>
				<th style="text-align: left;padding-left:50px;">Title</th>
			</tr>
		</thead>
		
		<s:iterator value="dataList" status="stat">
			<tr>
				<td style="text-align: right"></td>
				<td style="text-align: center"></td>
				<td style="text-align: right">Problem <s:property value="dataList[#stat.index][0]" /></td>
				<s:if test="curDate.compareTo(contest.endTime) > 0 || #session.visitor.sup == 1 || #session.visitor.id == contest.manager.id">
					<td class="center">
						<a target="_blank" href="<s:property value="dataList[#stat.index][3]" />"><s:property value="dataList[#stat.index][1]" /> <s:property value="dataList[#stat.index][2]" /></a>
					</td>
				</s:if>
				<td style="padding-left:50px;">
					<a href="contest/view.action?cid=${cid}#problem/<s:property value='dataList[#stat.index][0]' />">
						<s:property value="dataList[#stat.index][4]" escape="false" />
					</a>
				</td>
			</tr>
		</s:iterator>
	</table>
	</s:if>
	
	<div class="description">
		${contest.description}
	</div>
</div>



	
