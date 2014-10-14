<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html>
	<head>
        <%@ include file="/header.jsp" %>
		<title>Status - Virtual Judge</title>


<%--  		<link type="text/css" href="${contextPath}/css/problem_status.css" media="screen" rel="stylesheet" /> --%>
<%--  		<script type="text/javascript" src="${contextPath}/javascript/problem_status.js?<%=application.getAttribute("version")%>"></script> --%>
	</head>

	<body>
        <s:hidden id="js_require_problem_status" />
		<s:include value="/top.jsp" />
		<s:actionerror/>
		
<%-- 		<form id="form_status">
			Username:<input type="text" name="un" value="${un}" />&nbsp;&nbsp;
			OJ:<s:select id="OJId" name="OJId" value="%{OJId}" list="OJListAll" />&nbsp;&nbsp;
			Problem ID:<s:textfield name="probNum" />&nbsp;&nbsp;
			Result:<s:select name="res" list="#{'0':'All','1':'Accepted','2':'Presentation Error','3':'Wrong Answer','4':'Time Limit Exceed','5':'Memory Limit Exceed','6':'Output Limit Exceed','7':'Runtime Error','8':'Compile Error','9':'Unknown Error','10':'Submit Error','11':'Queuing && Judging'}" />&nbsp;&nbsp;
			<input type="submit" value="Filter"/>&nbsp;&nbsp;
			<input type="button" value="Reset" id="reset" />
		</form> --%>
		
		<table cellpadding="0" cellspacing="0" border="0" class="display" id="status">
			<thead>
				<tr>
					<th class="run_id">RunID</th>
					<th class="username">User<br /><input type="search" id="un" name="un" class="search_text" style="width:100%" /></th>
                    <th class="oj">
                        OJ<br />
                        <select name="OJId" id="OJId" style="width: 100%;">
                            <option value="All">All</option>
                        <s:iterator value="OJList" var="oj">
                            <option value="<s:property />" data-class="avatar" data-style="background-image: url(&apos;${contextPath}/<s:property value='#oj.faviconUrl'/>&apos;);background-size: 16px;"><s:property /></option>
                        </s:iterator>
                        </select>
                    </th>
                    <th class="prob_num">Prob ID<br /><input type="search" id="probNum" name="probNum" class="search_text" style="width:100%" /></th>
					<th class="result">
					    Result
					    <br />
                        <select name="res" id="res" style="width: 100%;">
                            <option value="0">All</option>
                            <option value="1" data-class="ui-icon-check">Accepted</option>
                            <option value="2" data-class="ui-icon-lightbulb">Presentation Error</option>
                            <option value="3" data-class="ui-icon-closethick">Wrong Answer</option>
                            <option value="4" data-class="ui-icon-clock">Time Limit Exceed</option>
                            <option value="5" data-class="ui-icon-notice">Memory Limit Exceed</option>
                            <option value="6" data-class="ui-icon-notice">Output Limit Exceed</option>
                            <option value="7" data-class="ui-icon-notice">Runtime Error</option>
                            <option value="8" data-class="ui-icon-notice">Compile Error</option>
                            <option value="9" data-class="ui-icon-notice">Unknown Error</option>
                            <option value="10" data-class="ui-icon-notice">Submit Error</option>
                            <option value="11" data-class="ui-icon-arrowrefresh-1-s">Queuing && Judging</option>
                        </select>
					</th>
					<th class="memory">Memory<br />(KB)</th>
					<th class="time">Time<br />(ms)</th>
					<th class="language">Language</th>
					<th class="length">Length<br />(Bytes)</th>
					<th class="date">Submit Time</th>
					<th></th>
					<th></th>
				</tr>
			</thead>
			<tbody>
			</tbody>
		</table>
		
        <input type="button" id="filter" value="Filter" />
        <input type="button" id="reset" value="Reset" />
		
        <s:hidden name="orderBy" />
        <s:hidden name="isSup" />
		<s:include value="/bottom.jsp" />
	</body>

</html>
