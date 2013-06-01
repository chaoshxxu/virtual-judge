<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<s:include value="/header.jsp" />
		<title><s:property value="contest.title" escape="false" /> - Virtual Judge</title>
		<link href="facebox/facebox.css" media="screen" rel="stylesheet" type="text/css" />
		<style type="text/css" media="screen">
			.hiddable {font-size:17px}
			.dataTables_paginate {float: right;text-align: right;}
			table.blue_border td {border:1px solid #A6C9E2;}
		</style>

		<script type="text/javascript" src="javascript/base64.js?<%=application.getAttribute("version")%>"></script>
		<script type="text/javascript" src="javascript/jquery.quicksand.js"></script>
		<script type="text/javascript" src="javascript/jquery.ba-hashchange.min.js"></script>
		<script type="text/javascript" src='javascript/jquery.scrollTo-min.js'></script>
		<script type="text/javascript" src="javascript/jquery.cookie.js"></script>
		<script type="text/javascript" src="facebox/facebox.js"></script>

		<script type="text/javascript" src="dwr/interface/judgeService.js"></script>
		<script type='text/javascript' src='javascript/engine.js'></script>
		<script type='text/javascript' src='dwr/util.js'></script>

		<script type="text/javascript" src="javascript/viewContest.js?<%=application.getAttribute("version")%>"></script>
	</head>

	<body>
		<s:include value="/top.jsp" />
		<s:if test="contest.announcement != null && !contest.announcement.isEmpty()"><marquee id="contest_announcement" height="25" style="text-align:center;color:red;font-weight:bold" onmouseout="this.start()" onmouseover="this.stop()" scrollamount="2" scrolldelay="1" behavior="alternate">${contest.announcement}</marquee><script type="text/javascript">$("table.banner").css("margin-bottom", "5px");if ($.browser.safari)$("#contest_announcement").removeAttr("behavior");</script></s:if>
		<s:hidden id="cid" name="cid" />
		
		<div id="contest_title" class="ptt">
			<s:if test="contest.replayStatus != null"><img height="25" title="Replay" src="images/replay.png" /></s:if>
			<s:property value="contest.title" escape="false" />
		</div>
		<div id="time_container" style="width:96%;height:40px;margin:auto;font-family: Lucida Grande,Lucida Sans,Arial,sans-serif;font-size: 12px;">
			<div id="time_index" style="text-align:right;float:left">
				<span></span>
			</div>
			<div id="time_total" style="text-align:right;">
				<span></span>
			</div>
			<div id="time_controller" style="clear:both"></div>
		</div>
		
		<div id="contest_tabs" style="background:transparent;display:none">
			<ul>
				<li><a href="#overview" rel="#overview">Overview</a></li>
				<s:if test="dataList != null">
					<li><a href="#problem" rel="#problem">Problem</a></li>
					<li><a href="#status" rel="#status">Status</a></li>
					<li><a href="#rank" rel="#rank">Rank</a></li>
				</s:if>
				<s:if test="contestOver == 1 || #session.visitor.sup == 1 || #session.visitor.id == contest.id">
					<li><a href="#discuss" rel="#discuss">Discuss</a></li>
				</s:if>
			</ul>
			<s:include value="/contest/div_overview.jsp" />
			<s:if test="dataList != null">
				<s:include value="/contest/div_problem.jsp" />
				<s:include value="/contest/div_status.jsp" />
				<s:include value="/contest/div_rank.jsp" />
			</s:if>
			<s:if test="contestOver == 1 || #session.visitor.sup == 1 || #session.visitor.id == contest.id">
				<s:include value="/contest/div_discuss.jsp" />
			</s:if>
		</div>		
		<s:include value="/bottom.jsp" />
	</body>
</html>
