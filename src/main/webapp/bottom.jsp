<%@ page language="java" import="java.util.Date" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ include file="/contextPath.jsp" %>

<div style="text-align:center;margin-top:30px;clear:both;font-size:80%">
	<hr />FAQ | About Virtual Judge | <a href="https://groups.google.com/group/virtual-judge" target="_blank">Forum</a> | <a href="${contextPath}/discuss.action" target="_blank">Discuss</a> | <a href="https://github.com/trcnkq/virtual-judge" target="_blank">Open Source Project</a><br />
	All Copyright Reserved ©2010-2014 <a href="http://acm.hust.edu.cn">HUST ACM/ICPC</a> TEAM
	<s:if test="#session.visitor.sup == 1"><a href="${contextPath}/stat/listOL.action"><img style="text-decoration: none;" height="15px" border="0" src="${contextPath}/images/statistics.gif" /></a></s:if>
	<br>Anything about the OJ, please ask in the <a href="https://groups.google.com/group/virtual-judge" target="_blank">forum</a>, or contact author:<a href="mailto:is.un@qq.com">Isun</a><br>
	Server Time: <span class="currentTime"></span>
</div>

<input id="serverTime" name="serverTime" type="hidden" value="<%= new Date().getTime()%>" />
<s:if test="#session.visitor != null">
    <input id="username" name="username" type="hidden" value="<s:property value='#session.visitor.username' />" />
    <input id="userId" name="userId" type="hidden" value="<s:property value='#session.visitor.id' />" />
    <input id="sup" name="sup" type="hidden" value="<s:property value='#session.visitor.sup' />" />
</s:if>
