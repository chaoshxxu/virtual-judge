<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Virtual Judge</title>
		<s:include value="/header.jsp" />
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
			<p id="ojs" style="line-height:30px;">
				<a href="http://poj.org/" target="_blank">POJ</a>&nbsp;&nbsp;&nbsp;
				<a href="http://acm.zju.edu.cn/onlinejudge/" target="_blank">ZOJ</a>&nbsp;&nbsp;&nbsp;
				<a href="http://livearchive.onlinejudge.org/index.php" target="_blank">UVALive</a>&nbsp;&nbsp;&nbsp;
				<a href="http://acm.sgu.ru/" target="_blank">SGU</a>&nbsp;&nbsp;&nbsp;
				<a href="http://acm.timus.ru/" target="_blank">URAL</a>&nbsp;&nbsp;&nbsp;
				<a href="http://acm.hust.edu.cn/" target="_blank">HUST</a>&nbsp;&nbsp;&nbsp;
				<a href="http://www.spoj.com/" target="_blank">SPOJ</a>&nbsp;&nbsp;&nbsp;
				<a href="http://acm.hdu.edu.cn" target="_blank">HDU</a>&nbsp;&nbsp;&nbsp;
				<a href="http://www.lydsy.com/JudgeOnline/" target="_blank">HYSBZ</a>&nbsp;&nbsp;&nbsp;
				<a href="http://uva.onlinejudge.org/" target="_blank">UVA</a>&nbsp;&nbsp;&nbsp;
				<a href="http://codeforces.com/" target="_blank">CodeForces</a>&nbsp;&nbsp;&nbsp;
				<a href="http://www.z-trening.com/" target="_blank">Z-Trening</a>&nbsp;&nbsp;&nbsp;
				<a href="http://judge.u-aizu.ac.jp/" target="_blank">Aizu</a>&nbsp;&nbsp;&nbsp;
				<a href="http://lightoj.com/" target="_blank">LightOJ</a>&nbsp;&nbsp;&nbsp;
				<a href="http://acm.uestc.edu.cn/" target="_blank">UESTC</a>&nbsp;&nbsp;&nbsp;
				<a href="http://cdn.ac.nbutoj.com/" target="_blank">NBUT</a>&nbsp;&nbsp;&nbsp;
				<a href="http://acm.fzu.edu.cn/" target="_blank">FZU</a>&nbsp;&nbsp;&nbsp;
                <a href="http://acm.csu.edu.cn/OnlineJudge/" target="_blank">CSU</a>&nbsp;&nbsp;&nbsp;
                <a href="http://cstest.scu.edu.cn/" target="_blank">SCU</a>&nbsp;&nbsp;&nbsp;
			</p>
			<br />
			<b>What's new:</b>
			<ul style="line-height:20px;">
				<li style="color:red;">
				    2014-09-20  Currently Virtual Judge has two access address:<br />
	                <ul>
                        <li><a href="http://acm.hust.edu.cn/vjudge" target="_blank">http://acm.hust.edu.cn/vjudge</a> : Since it was redirected to <a href="http://vjudge.net" target="_blank">vjudge.net</a> permanently incorrectly, you need clear your browser cache to use it. Recommended for Chinese visitors. </li>
                        <li><a href="http://vjudge.net" target="_blank">http://vjudge.net</a> : Reverse proxy hosted oversea. Recommended for visitors who can't use the above entrance.</li>
                    </ul>
				</li>
				<li>2010-10-07  Refer to <a href="https://github.com/trcnkq/virtual-judge/commits/master">this page</a> for recent change of Virtual Judge.</li>
			</ul>
		</div>
		<s:include value="/bottom.jsp" />
	</body>
</html>
