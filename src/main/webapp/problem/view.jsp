<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html>
	<head>
		<%@ include file="/header.jsp" %>
		<title>${problem.title} - ${problem.originOJ} ${problem.originProb} - Virtual Judge</title>

<%--  		<script type="text/javascript" src="${contextPath}/javascript/problem_view.js?<%=application.getAttribute("version")%>"></script> --%>
	</head>

	<body>
        <s:hidden id="js_require_viewProblem" />
		<s:include value="/top.jsp" />
		
		<table width="100%"><tr>
		<td id="left_view">
			<span style="text-align:left;font-family:Verdana;color:green">${problem.originOJ} - ${problem.originProb}</span>
			<div class="ptt">
				<a href="${problem.url}" target="_blank">${problem.title}</a>
			</div>
			<div class="plm">
				<table align="center">
					<tr>
						<s:if test="problem.timeLimit == 1">
							<td><b><font color="green">Crawling in process...</font></b></td>
						</s:if>
						<s:elseif test="problem.timeLimit == 2">
							<td><b><font color="red">Crawling failed</font></b></td>
						</s:elseif>
						<s:else>
							<td>
								<b>Time Limit:</b>
								<s:if test="problem.timeLimit == 0">Unknown</s:if>
								<s:else>${problem.timeLimit}MS</s:else> 
							</td>
							<td width="10px"></td>
							<td>
								<b>Memory Limit:</b>
								<s:if test="problem.memoryLimit == 0">Unknown</s:if>
								<s:else>${problem.memoryLimit}KB</s:else>
							</td>
							<td width="10px"></td>
							<td>
								<b>64bit IO Format:</b>
								${_64Format}
							</td>
						</s:else>
					</tr>
				</table>
			</div>
			
			<s:if test="problem.timeLimit != 1 && problem.timeLimit != 2">
				<p class="opt_btn" align="center">
					<font size="3" color="#333399">
						<a href="${contextPath}/problem/toSubmit.action?id=${problem.id}" class="login">Submit</a>
						<a href="${contextPath}/problem/status.action#OJId=${problem.originOJ}&probNum=${problem.originProb}">Status</a> 
					</font>
				</p>
			</s:if>
			
			<div class="hiddable" id="vj_description"><p class="pst">Description</p><div class="textBG"></div></div>
			<div class="hiddable" id="vj_input"><p class="pst">Input</p><div class="textBG"></div></div>
			<div class="hiddable" id="vj_output"><p class="pst">Output</p><div class="textBG"></div></div>
			<div class="hiddable" id="vj_sampleInput"><p class="pst">Sample Input</p><div class="textBG"></div></div>
			<div class="hiddable" id="vj_sampleOutput"><p class="pst">Sample Output</p><div class="textBG"></div></div>
			<div class="hiddable" id="vj_hint"><p class="pst">Hint</p><div class="textBG"></div></div>
			<s:if test="problem.source != null"><p class="pst">Source</p><div class="textBG">${problem.source}</div></s:if>

			<s:if test="problem.timeLimit != 1 && problem.timeLimit != 2">
				<p class="opt_btn" align="center">
					<font size="3" color="#333399">
						<a href="${contextPath}/problem/toSubmit.action?id=${problem.id}" class="login">Submit</a>
                        <a href="${contextPath}/problem/status.action#OJId=${problem.originOJ}&probNum=${problem.originProb}">Status</a> 
					</font>
				</p>
			</s:if>
		</td>
		
		<td id=mid_view>
			<img id="bt" style="position:fixed" src="${contextPath}/images/to_right.png" />
		</td>
		
		<td id="right_view">
			<div style="background-color:#EAEBFF;border-radius: 8px 8px 8px 8px;text-align:left;padding:10px;font-family:Verdana;">Problem descriptions:</div>
			<s:iterator value="problem.descriptions" status="stat">
				<div class="desc_info">
					<div id="info<s:property value="#stat.index" />">
						<b><s:if test='author.equals("0")'><font color="red">System Crawler</font></s:if><s:else><s:property value="author" /></s:else></b>
						<s:date name="updateTime" format="yyyy-MM-dd" />
						<span class="hiddable opt">
							<s:hidden name="vote" />
							<s:if test="#session.votePids.contains(problem.id)">
								<span style="float:right"><img height="18" src="${contextPath}/images/thumb_up.png" border="0"/><span>${vote}</span></span>
							</s:if>
							<s:else>
								<span style="float:right" ><a class="vote" id="vote_${id}" href="javascript:void(0)" style="text-decoration:none"><img height="18" src="${contextPath}/images/thumb_up.png" border="0"/>${vote}</a></span>
								<span style="float:right;display:none"><img height="18" src="${contextPath}/images/thumb_up.png" border="0"/><span>${vote}</span></span>
							</s:else>
							<a style="padding-right:10px;float:right" href="${contextPath}/problem/toEditDescription.action?id=${id}" class="login"><img height="18" src="${contextPath}/images/ico_edit.gif" border="0"/></a>
							<s:if test='author.equals("0")'>
								<a style="padding-right:10px;float:right" href="${contextPath}/problem/recrawlProblem.action?OJId=${problem.originOJ}&probNum=${problem.originProb}"><img height="18" src="${contextPath}/images/refresh.png" border="0"/></a>
							</s:if>
							<s:elseif test="#session.visitor.sup == 1 || #session.visitor.username == author">
								<a style="padding-right:10px;float:right" id="del_${id}" class="delete_desc" href="javascript:void(0)"><img height="18" src="${contextPath}/images/ico_delete.gif" border="0"/></a>
							</s:elseif>
						</span>
					</div>
					<div class="hiddable remark">
						<img height="20" src="${contextPath}/images/icon_quote_s.gif" /><s:property value="remarks" /><img height="20" src="${contextPath}/images/icon_quote_e.gif" />
					</div>
				</div>
			</s:iterator>
			
			<img id='show_discuss' title='Discuss' src="${contextPath}/images/btn_discuss_1.png" style="float:right" onmouseover='javascript:this.src="${contextPath}/images/btn_discuss_2.png"' onmouseout='javascript:this.src="${contextPath}/images/btn_discuss_1.png"' />
			<div id="disqus_thread"></div>

			<script type="text/javascript">
		    /* * * CONFIGURATION VARIABLES: EDIT BEFORE PASTING INTO YOUR WEBPAGE * * */
		    var disqus_shortname = '<%=application.getAttribute("disqusShortname")%>'; // required: replace example with your forum shortname
		    var disqus_developer = <%=application.getAttribute("disqusDeveloper")%>;
			var disqus_identifier = "problem/${problem.id}";
			var disqus_title = "Problem - ${problem.originOJ} ${problem.originProb} - ${problem.title}";
			
		    /* * * DON'T EDIT BELOW THIS LINE * * */
		    $("#show_discuss").click(function(){
		    	$(this).remove();
			    (function() {
			        var dsq = document.createElement('script'); dsq.type = 'text/javascript'; dsq.async = true;
			        dsq.src = 'http://' + disqus_shortname + '.disqus.com/embed.js';
			        (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(dsq);
			    })();
		    }); 
			</script>
			<noscript>Please enable JavaScript to view the <a href="http://disqus.com/?ref_noscript">comments powered by Disqus.</a></noscript>
			
		</td>
		
		</tr></table>

        <s:hidden name="pid" value="%{problem.id}" />
        <s:hidden name="timeLimit" value="%{problem.timeLimit}" />
		<s:include value="/bottom.jsp" />
	</body>
	
</html>
