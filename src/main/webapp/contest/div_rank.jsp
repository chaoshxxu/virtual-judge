<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ include file="/contextPath.jsp" %>

<div id="rank" style="padding:0;min-height:300px">

	<div id="rank_header" style="background-color:#F2FCF7;display:none;position:fixed;top:0;z-index:100" data-id="rank_header">
		<div class="rank">Rank</div>
		<div class="id">ID</div>
		<div class="solve">Solve</div>
		<div class="standing_time">Penalty</div>
		<s:iterator value="numList" status="stat">
			<div class="standing_time"><a href="${contextPath}/contest/view.action?cid=${cid}#problem/${key}">${key}</a></div>
		</s:iterator>
		<div style="text-align:right"></div>
	</div>
	
	<div id="rank_data_destination" style="display:none"></div>
	<div id="rank_data_source"></div>

	<div id="rank_foot" style="background-color:transparent;display:none;position:fixed;bottom:0;z-index:100" class="disp" data-id="rank_foot">
		<div class="rank"></div>
		<div class="id"></div>
		<div class="solve"></div>
		<div class="standing_time"></div>
		<s:iterator value="numList" status="stat">
			<div class="standing_time"></div>
		</s:iterator>
		<div class="standing_time" style='background-color:#D3D6FF'></div>
	</div>

	<div id="div_rank_tool" style="text-align:right;position:fixed;bottom:80px;right:15px;z-index:999999">
		<img src="${contextPath}/images/find_me.png" id="img_find_me" title="Find me" height="55" style="visibility:hidden;cursor: pointer" />
		<img src="${contextPath}/images/go_top.png" id="img_go_top" title="Go to top" height="60" style="visibility:hidden;cursor: pointer" />
		<img src="${contextPath}/images/config.png" id="rank_setting" title="Rank config" height="60" style="cursor: pointer" />
	</div>
	
	<div id="dialog-form-rank-setting" style="display:none" title="Rank Setting"></div>
	
</div>
