<%@ include file="/contextPath.jsp" %>
<%@ taglib prefix="s" uri="/struts-tags"%>

<div id='cssmenu'>
<ul>
    <li id="nav_home"><a href="${contextPath}/toIndex.action">Home</a></li>
    <li id="nav_problem"><a href="${contextPath}/problem/toListProblem.action">Problem</a></li>
    <li id="nav_status"><a href="${contextPath}/problem/status.action">Status</a></li>
    <li id="nav_contest" class='has-sub'><a href="${contextPath}/contest/toListContest.action">Contest</a>
       <ul>
          <li><a href='${contextPath}/contest/toAddContest.action' class="login">Add Contest</a></li>
          <li><a href='${contextPath}/contest/statistic.action'>Statistic</a></li>
       </ul>
    </li>
	<li style="float:right"><s:if test="#session.visitor != null"><a id="logout" href="javascript:void(0)">LOGOUT</a></s:if><s:else><a class="login" href="javascript:void(0)">LOGIN</a></s:else></li>
	<li style="float:right"><s:if test="#session.visitor != null"><a id="my_account" uid="<s:property value='#session.visitor.id' />" href="${contextPath}/user/toUpdate.action?uid=<s:property value="#session.visitor.id" />"><s:property value="#session.visitor.username" /></a></s:if><s:else><a class="register" href="javascript:void(0)">REGISTER</a></s:else></li>
</ul>
</div>

<s:if test="#session.visitor == null">
	<div id="dialog-form-login" style="display: none" title="Login">
		<p class="validateTips"></p><form id="login_form" action="javascript:void(0)"><fieldset><label for="username">Username *</label><input type="text" id="username" name="username" class="text ui-widget-content ui-corner-all" style="ime-mode:disabled" /><label for="password">Password *</label><input type="password" id="password" name="password" class="text ui-widget-content ui-corner-all" /></fieldset></form>
	</div>
	<div id="dialog-form-register" style="display: none" title="Register">
		<p class="validateTips"></p><fieldset><div style="width:200px;float:left"><label for="username1">Username *</label><input type="text" id="username1" class="text ui-widget-content ui-corner-all" style="ime-mode:disabled" /><label for="password1">Password *</label><input type="password" id="password1" class="text ui-widget-content ui-corner-all" /><label for="repassword">Repeat *</label><input type="password" id="repassword" class="text ui-widget-content ui-corner-all" /><label for="nickname">Nickname</label><input type="text" id="nickname" class="text ui-widget-content ui-corner-all" /><label for="school">School</label><input type="text" id="school" class="text ui-widget-content ui-corner-all" /><label for="qq">QQ</label><input type="text" id="qq" class="text ui-widget-content ui-corner-all" /><label for="email">Email</label><input type="text" id="email" class="text ui-widget-content ui-corner-all" /><label for="share">Share code by default</label><br /><s:radio id="share" name="share" list="#{'0':'No', '1':'Yes'}" value="1" theme="simple" /></div><div style="width:200px;margin-left:20px;float:left"><label for="blog">Blog & Introduction</label><s:textarea id="blog" rows="25" cols="35" cssClass="text ui-widget-content ui-corner-all" /></div></fieldset>
	</div>
</s:if>
