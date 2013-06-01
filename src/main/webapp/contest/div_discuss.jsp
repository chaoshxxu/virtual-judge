<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<div id="discuss" style="font-size:14px;padding:12px 0">
	<div id="disqus_thread" style="width:900px;margin:auto"></div>
	<script type="text/javascript">
	    /* * * CONFIGURATION VARIABLES: EDIT BEFORE PASTING INTO YOUR WEBPAGE * * */
		var disqus_shortname = '<%=application.getAttribute("disqusShortname")%>'; // required: replace example with your forum shortname
		var disqus_developer = <%=application.getAttribute("disqusDeveloper")%>;
		var disqus_identifier = "contest/${cid}";
		var disqus_url = basePath + "contest/view.action?cid=${cid}#discuss";
		var disqus_title = "Contest - <s:property value="contest.title" escape="false" />";
		
	    /* * * DON'T EDIT BELOW THIS LINE * * */
	    var _showDiscuss = function() {
	        var dsq = document.createElement('script'); dsq.type = 'text/javascript'; dsq.async = true;
	        dsq.src = 'http://' + disqus_shortname + '.disqus.com/embed.js';
	        (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(dsq);
	    };
	</script>
	<noscript>Please enable JavaScript to view the <a href="http://disqus.com/?ref_noscript">comments powered by Disqus.</a></noscript>
</div>
