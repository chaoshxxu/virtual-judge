<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />  

<!DOCTYPE html>
<html>
	<head>
		<title>Virtual Judge</title>
		<%@ include file="/header.jsp" %>
	</head>

	<body>
		<s:include value="/top.jsp" />
		<div id="title">Virtual Judge</div>
		<div style="width:900px;MARGIN-RIGHT:auto;MARGIN-LEFT:auto;">
			<p>
				Virtual Judge is not a real online judge. It can grab problems from other regular online judges and simulate submissions to other online judges.
				It aims to enable holding contests when you don't have the test data.<br /><br />
				Currently, this system supports the following online judges:<br />
			</p>
			
			<div id="ojs">
				<span><a href="http://poj.org/" target="_blank"><img src="${contextPath}/images/remote_oj/poj.ico" />POJ</a></span>
				<span><a href="http://acm.zju.edu.cn/onlinejudge/" target="_blank"><img src="${contextPath}/images/remote_oj/ZOJ_favicon.ico" />ZOJ</a></span>
				<span><a href="http://livearchive.onlinejudge.org/index.php" target="_blank"><img src="${contextPath}/images/remote_oj/UVA_favicon.ico" />UVALive</a></span>
				<span><a href="http://acm.sgu.ru/" target="_blank"><img src="${contextPath}/images/remote_oj/SGU_favicon.ico" />SGU</a></span>
				<span><a href="http://acm.timus.ru/" target="_blank"><img src="${contextPath}/images/remote_oj/URAL_favicon.ico" />URAL</a></span>
				<span><a href="http://acm.hust.edu.cn/" target="_blank"><img src="${contextPath}/images/remote_oj/HUST_icon.jpg" />HUST</a></span>
				<span><a href="http://www.spoj.com/" target="_blank"><img src="${contextPath}/images/remote_oj/SPOJ_favicon.png" />SPOJ</a></span>
				<span><a href="http://acm.hdu.edu.cn" target="_blank"><img src="${contextPath}/images/remote_oj/HDU_icon.png" />HDU</a></span>
				<span><a href="http://www.lydsy.com/JudgeOnline/" target="_blank"><img src="${contextPath}/images/remote_oj/HYSBZ_icon.png" />HYSBZ</a></span>
				<span><a href="http://uva.onlinejudge.org/" target="_blank"><img src="${contextPath}/images/remote_oj/UVA_favicon.ico" />UVA</a></span>
				<span><a href="http://codeforces.com/" target="_blank"><img src="${contextPath}/images/remote_oj/CodeForces_favicon.png" />CodeForces</a></span>
				<span><a href="http://www.z-trening.com/" target="_blank"><img src="${contextPath}/images/remote_oj/icon-icpc-small.gif" />Z-Trening</a></span>
				<span><a href="http://judge.u-aizu.ac.jp/" target="_blank"><img src="${contextPath}/images/remote_oj/Aizu_favicon.ico" />Aizu</a></span>
				<span><a href="http://lightoj.com/" target="_blank"><img src="${contextPath}/images/remote_oj/icon-icpc-small.gif" />LightOJ</a></span>
				<span><a href="http://acm.uestc.edu.cn/" target="_blank"><img src="${contextPath}/images/remote_oj/UESTC_favicon.png" />UESTC</a></span>
				<span><a href="http://cdn.ac.nbutoj.com/" target="_blank"><img src="${contextPath}/images/remote_oj/NBUT_icon.jpg" />NBUT</a></span>
				<span><a href="http://acm.fzu.edu.cn/" target="_blank"><img src="${contextPath}/images/remote_oj/FZU_favicon.gif" />FZU</a></span>
                <span><a href="http://acm.csu.edu.cn/OnlineJudge/" target="_blank"><img src="${contextPath}/images/remote_oj/CSU_favicon.ico" />CSU</a></span>
                <span><a href="http://cstest.scu.edu.cn/" target="_blank"><img src="${contextPath}/images/remote_oj/SCU_favicon.ico" />SCU</a></span>
			</div>
			<div style="line-height:20px;padding-top:5px;clear:both;">
				<p>What's new:</p>
				<ul>
                    <li>2014-10-15  UI update. Please <a href="mailto:is.un@qq.com">contact me</a> if you find any bugs. Thanks very much!</li>
					<li style="color:red;">
					    2014-09-20  Currently Virtual Judge has two access address:<br />
		                <ul>
	                        <li><a href="http://acm.hust.edu.cn/vjudge" target="_blank">http://acm.hust.edu.cn/vjudge</a> : Since it was redirected to <a href="http://vjudge.net" target="_blank">vjudge.net</a> permanently incorrectly, you need clear your browser cache to use it. Recommended for Chinese visitors. </li>
	                        <li><a href="http://vjudge.net" target="_blank">http://vjudge.net</a> : Reverse proxy hosted overseas. Recommended for visitors who can't use the above entrance.</li>
	                    </ul>
					</li>
					<li>2010-10-07  Refer to <a href="https://github.com/trcnkq/virtual-judge/commits/master">this page</a> for recent change of Virtual Judge.</li>
				</ul>
			</div>
		</div>
		<s:include value="/bottom.jsp" />
	</body>
</html>
