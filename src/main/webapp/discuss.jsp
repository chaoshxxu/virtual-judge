<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html>
	<head>
		<title>Discuss - Virtual Judge</title>
		<%@ include file="/header.jsp" %>
	</head>

	<body>
		<s:include value="/top.jsp" />

		<div id="disqus_thread" style="width:900px;"></div>
        <script type="text/javascript">
            /* * * CONFIGURATION VARIABLES: EDIT BEFORE PASTING INTO YOUR WEBPAGE * * */
			var disqus_shortname = '<%=application.getAttribute("disqusShortname")%>';
			var disqus_developer = <%=application.getAttribute("disqusDeveloper")%>;
			var disqus_identifier = '<%=application.getAttribute("disqusShortname")%>';
			var disqus_title = "General Topic";

            /* * * DON'T EDIT BELOW THIS LINE * * */
            (function() {
                var dsq = document.createElement('script'); dsq.type = 'text/javascript'; dsq.async = true;
                dsq.src = 'http://' + disqus_shortname + '.disqus.com/embed.js';
                (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(dsq);
            })();
        </script>
        <noscript>Please enable JavaScript to view the <a href="http://disqus.com/?ref_noscript">comments powered by Disqus.</a></noscript>
        <a href="http://disqus.com" class="dsq-brlink">comments powered by <span class="logo-disqus">Disqus</span></a>

		<script type="text/javascript" src="http://<%=application.getAttribute("disqusShortname")%>.disqus.com/combination_widget.js?num_items=200&hide_mods=1&color=blue&default_tab=recent&excerpt_length=200"></script><a href="http://disqus.com/">Powered by Disqus</a>

		<s:include value="/bottom.jsp" />
	</body>
</html>
