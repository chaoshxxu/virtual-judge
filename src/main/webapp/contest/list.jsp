<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />  

<!DOCTYPE html>
<html>
    <head>
        <%@ include file="/header.jsp" %>
        <title>Contests - Virtual Judge</title>
        
<%--         <link type="text/css" href="${contextPath}/css/contest_list.css" media="screen" rel="stylesheet" /> --%>
<%--         <script type="text/javascript" src="${contextPath}/javascript/contest_list.js?<%=application.getAttribute("version")%>"></script> --%>
    </head>

    <body>
        <s:hidden id="js_require_listContest" />
        <s:include value="/top.jsp" />
        <s:actionerror />
        
<!--         <div class="ptt" style="margin-bottom:12px;position:relative;">
            Contest List
            <div style="float:right;position:absolute;right:0px;top:0">
                <a href="${contextPath}/contest/statistic.action" id="statistic_contest" target="_blank" style="color:black">Statistic</a>
                <a href="${contextPath}/contest/toAddContest.action" class="login" id="add_contest" style="color:black">Add a contest</a>
            </div>
        </div> -->
        
        <table cellpadding="0" cellspacing="0" border="0" class="display" id="listContest">
            <thead>
                <tr>
                    <th class="contest_id">ID</th>
                    <th class="title">
                        Title
                        <br />
                        <input type="search" id="title" name="title" class="search_text" style="width:100%" />
                    </th>
                    <th class="date">Begin Time</th>
                    <th class="length">Length</th>
                    <th class="openness">
                        Openness
                        <br />
                        <select name="contestOpenness" id="contestOpenness" style="width:100%">
                            <option value="0" selected="selected">All</option>
                            <option value="1" data-class="ui-icon-check">Public</option>
                            <option value="2" data-class="ui-icon-lightbulb">Private</option>
                        </select>
                    </th>
                    <th class="manager">
                        Manager
                        <br />
                        <input type="search" id="manager" name="manager" class="search_text" style="width:100%" />
                    </th>
                </tr>
            </thead>
            <tbody></tbody>
        </table>

        <input type="button" id="filter" value="Filter" />
        <input type="button" id="reset" value="Reset" />

        <div id="contestType">
            <input type="radio" id="contestType0" name="contestType" value="0" checked="checked" />
            <label for="contestType0">
                <img src="${contextPath}/images/contest.jpg" height="20" title="Real contest" />
            </label>
            
            <input type="radio" id="contestType1" name="contestType" value="1" />
            <label for="contestType1">
                <img src="${contextPath}/images/replay.png" height="20" title="Replay" />
            </label>
        </div>
    
        <div id="contestRunningStatus">
            <input type="radio" id="contestRunningStatus0" name="contestRunningStatus" value="0" checked="checked" />
            <label for="contestRunningStatus0">All</label>

            <input type="radio" id="contestRunningStatus1" name="contestRunningStatus" value="1" />
            <label for="contestRunningStatus1" style="color:blue;">Scheduled</label>
            
            <input type="radio" id="contestRunningStatus2" name="contestRunningStatus" value="2" />
            <label for="contestRunningStatus2" style="color:red;">Running</label>
            
            <input type="radio" id="contestRunningStatus3" name="contestRunningStatus" value="3" />
            <label for="contestRunningStatus3" style="color:green;">Ended</label>
        </div>
    
<%--         <div id="head_status" style="float:right;display:none">
            <input name="contestType" id="contestType0" checked="checked" value="0" onclick="this.blur()" type="radio" /><label for="contestType0"><img src="${contextPath}/images/contest.jpg" height="20" title="Real contest" /></label>
            <input name="contestType" id="contestType1" value="1" onclick="this.blur()" type="radio" /><label for="contestType1"><img src="${contextPath}/images/replay.png" height="20" title="Replay" /></label>
            <span class="Scheduled">&nbsp;&nbsp;&nbsp;&nbsp;Scheduled:</span><input type="checkbox" name="scheduled" checked="checked" value="1" />  
            <span class="Running">&nbsp;&nbsp;Running:</span><input type="checkbox" name="running" checked="checked" value="2" />  
            <span class="Ended">&nbsp;&nbsp;Ended:</span><input type="checkbox" name="ended" checked="checked" value="3" />  
        </div> --%>
        
        <s:hidden name="cid" id="cid" />

        <s:include value="/bottom.jsp" />
    </body>
</html>
