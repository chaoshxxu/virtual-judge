<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="org.apache.struts2.ServletActionContext" %>
<%@ taglib prefix="s" uri="/struts-tags"%>

<%
String langFile = "shjs/lang/" + request.getAttribute("language") + ".min.js";
%>


<!DOCTYPE html>
<html>
	<head>
		<%@ include file="/header.jsp" %>
		<title>Source code - Virtual Judge</title>
<!-- 		<link rel="stylesheet" href="shjsx/shx_main.min.css" type="text/css" media="all"/>
		<link rel="stylesheet" href="shjsx/css/sh_typical.min.css" type="text/css" media="all"/>
		<link rel="stylesheet" href="shjsx/css/sh_print.min.css" type="text/css" media="print" /> -->
<%-- 		<script type="text/javascript" src="shjsx/sh_main.min.js"></script> --%>
<%--  		<script type="text/javascript" src="${contextPath}/javascript/viewSource.js?<%=application.getAttribute("version")%>"></script> --%>
	</head>

	<body>
        <s:hidden id="js_require_viewSource" />
		<s:include value="/top.jsp" />
		<div class="ptt" style="color:black;font-weight:normal;margin-bottom:12px"><a href="${contextPath}/user/profile.action?uid=${uid}">${un}</a> 's source code for <a href="${contextPath}/problem/viewProblem.action?id=${submission.problem.id}">${problem.originOJ} ${problem.originProb}</a></div>
		<div class="plm" style="text-align:left">
			<table align="center" style="font-size:10pt">
				<tr>
					<td>
						<b>Memory: </b>${submission.memory} KB
					</td>
					<td width=10px></td>
					<td>
						<b>Time: </b>${submission.time} MS
					</td>
				</tr>
				<tr>
					<td>
						<b>Language: </b>${submission.language}
					</td>
					<td width=10px></td>
					<td>
						<b>Result: </b>
						<font color=blue>${submission.status}</font>
					</td>
				</tr>
				<tr>
					<td>
						<b>VJ RunId: </b>${submission.id}
					</td>
					<td width=10px></td>
					<td>
						<b>Real RunId: </b>${submission.realRunId}
					</td>
				</tr>
				<s:if test="#session.visitor.id == uid || #session.visitor.sup != 0">
					<tr>
						<td>
							<b>Public: </b>
						</td>
                        <td width=10px></td>
						<td>
                            <s:radio name="open" list="#{'0':'No', '1':'Yes'}" value="%{submission.isOpen}" onclick="this.blur()" ></s:radio>
						</td>
					</tr>
				</s:if>
			</table>
			<s:hidden name="sid" value="%{submission.id}" />
		</div>
		<s:if test="submission.isOpen == 0">
			<p id="info" style="text-align:center;font-size:15pt;color:green;visibility:hidden">This source is shared by <b>${un}</b></p>
		</s:if>
		<s:else>
			<p id="info" style="text-align:center;font-size:15pt;color:green;">This source is shared by <b>${un}</b></p>
		</s:else>	
		<pre class="${language}" style="font-family:Courier New,Courier,monospace">${submission.source}</pre>

		<div id="disqus_thread" style="width:900px;margin-top:100px"></div>
		<script type="text/javascript">
		    /* * * CONFIGURATION VARIABLES: EDIT BEFORE PASTING INTO YOUR WEBPAGE * * */
		    var disqus_shortname = '<%=application.getAttribute("disqusShortname")%>'; // required: replace example with your forum shortname
		    var disqus_developer = <%=application.getAttribute("disqusDeveloper")%>;
			var disqus_identifier = "source/${submission.id}";
			var disqus_title = "Source Code - ${un}'s code for ${problem.originOJ} ${problem.originProb}";
			
		    /* * * DON'T EDIT BELOW THIS LINE * * */
		    var _showDiscuss = function() {
		        var dsq = document.createElement('script'); dsq.type = 'text/javascript'; dsq.async = true;
		        dsq.src = 'http://' + disqus_shortname + '.disqus.com/embed.js';
		        (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(dsq);
		    };
		</script>
		<noscript>Please enable JavaScript to view the <a href="http://disqus.com/?ref_noscript">comments powered by Disqus.</a></noscript>

		<s:include value="/bottom.jsp" />
	</body>
</html>
