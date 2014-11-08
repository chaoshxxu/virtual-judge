<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ include file="/contextPath.jsp" %>

<div id="status" style="font-size:14px;padding:12px 0">
<%--     <form id="form_status" style="font-size:12px">
        Username:<input type="text" name="un" value="${un}" />&nbsp;&nbsp;
        Problem:<s:select name="num" list="%{numList}" cssStyle="width:160px" />&nbsp;&nbsp;
        Result:<s:select name="res" cssStyle="width:160px" list="#{'0':'All','1':'Accepted','2':'Presentation Error','3':'Wrong Answer','4':'Time Limit Exceed','5':'Memory Limit Exceed','6':'Output Limit Exceed','7':'Runtime Error','8':'Compile Error','9':'Unknown Error','10':'Submit Error','11':'Queuing && Judging'}" />&nbsp;&nbsp;
        <input type="submit" value="Filter" id="filter"/>&nbsp;&nbsp;
        <input type="button" value="Reset" id="reset" />
    </form>
 --%>

    <table cellpadding="0" cellspacing="0" border="0" class="display" id="table_status">
        <thead>
            <tr>
                <th class="run_id">RunID</th>
                <th class="username">User<br /><input type="search" id="un" name="un" class="search_text" style="width:100%" /></th>
                <th class="prob_num">Problem<br /><s:select name="num" list="%{numList}" cssStyle="width:100%" /></th>
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
                <th class="language">
                    Language
                    <br />
                    <select name="lang" id="lang" style="width: 100%;">
                        <option value="">All</option>
                        <option value="CPP">C++</option>
                        <option value="C">C</option>
                        <option value="JAVA">Java</option>
                        <option value="PASCAL">Pascal</option>
                        <option value="PYTHON">Python</option>
                        <option value="CSHARP">C#</option>
                        <option value="RUBY">Ruby</option>
                        <option value="OTHER">Other</option>
                    </select>
                </th>
                <th class="length">Length<br />(Bytes)</th>
                <th class="date">Submit Time</th>
            </tr>
        </thead>
        <tbody>
        </tbody>
    </table>

    <s:hidden name="isSup" />
    <input type="button" id="filter" value="Filter" />
    <input type="button" id="reset" value="Reset" />

</div>
