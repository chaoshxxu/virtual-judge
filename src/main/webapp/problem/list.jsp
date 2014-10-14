<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html>
    <head>
        <%@ include file="/header.jsp" %>
        <title>Problems - Virtual Judge</title>


<%--         <link type="text/css" href="${contextPath}/css/problem_list.css" media="screen" rel="stylesheet" /> --%>
<%--         <script type="text/javascript" src="${contextPath}/javascript/problem_list.js?<%=application.getAttribute("version")%>"></script> --%>
    </head>

    <body>
        <s:hidden id="js_require_listProblem" />
        <s:include value="/top.jsp" />
        <s:actionerror />

        <table cellpadding="0" cellspacing="0" border="0" class="display" id="listProblem">
            <thead>
                <tr>
                    <th class="oj">
                        OJ<br />
                        <select name="OJId" id="OJId" style="width: 100%; display: none;">
                            <option value="All">All</option>
                        <s:iterator value="OJList" var="oj">
                            <option value="<s:property />" data-class="avatar" data-style="background-image: url(&apos;${contextPath}/<s:property value='#oj.faviconUrl'/>&apos;);background-size: 16px;"><s:property /></option>
                        </s:iterator>
                        </select>
                    </th>
                    <th class="prob_num">Prob ID<br /><input type="search" id="probNum" name="probNum" class="search_text" style="width:95%" /></th>
                    <th class="title">Title<br /><input type="search" id="title" name="title" class="search_text" style="width:95%" /></th>
                    <th class="date">Update Time</th>
                    <th class="source">Source<br /><input type="search" id="source" name="source" class="search_text" style="width:95%" /></th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
        
        <input type="button" id="reset" value="Reset" />
        <input type="button" id="filter" value="Filter" />
        
        <s:include value="/bottom.jsp" />
    </body>

</html>
