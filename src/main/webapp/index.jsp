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
				Virtual Judge is not a real online judge. It can grabs problems from other regular online judges and simulate submissions to other online judges.
				It aims to enable holding contests when you don't have the test data.<br /><br />
				Currently, this system supports the following online judges:<br />
			</p>
			<p id="ojs" style="line-height:30px;">
				<a href="http://acm.pku.edu.cn/JudgeOnline/" target="_blank">POJ</a>&nbsp;&nbsp;&nbsp;
				<a href="http://acm.zju.edu.cn/onlinejudge/" target="_blank">ZOJ</a>&nbsp;&nbsp;&nbsp;
				<a href="http://livearchive.onlinejudge.org/index.php" target="_blank">UVALive</a>&nbsp;&nbsp;&nbsp;
				<a href="http://acm.sgu.ru/" target="_blank">SGU</a>&nbsp;&nbsp;&nbsp;
				<a href="http://acm.timus.ru/" target="_blank">URAL</a>&nbsp;&nbsp;&nbsp;
				<a href="http://acm.hust.edu.cn/thx/" target="_blank">HUST</a>&nbsp;&nbsp;&nbsp;
				<a href="http://www.spoj.pl" target="_blank">SPOJ</a>&nbsp;&nbsp;&nbsp;
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
			<b>Change Log:</b>
			<ul>
				<li>2010-05-04 : Put into use.</li>
				<li>2010-05-10 : Remember the last language selected.</li>
				<li>2010-05-10 : Remember the OJ of the last problem selected.</li>
				<li>2010-05-10 : Optimize the crawling of <a href="http://acm.zju.edu.cn/onlinejudge/">ZOJ</a>.</li>
				<li>2010-05-10 : <a href="http://acm.hdu.edu.cn">HDU OJ</a> is added.</li>
				<li>2010-06-01 : Host Virtual Judge on <a href="http://code.google.com/p/virtual-judge">Google Code</a>.</li>
				<li>2010-07-18 : Use <a href="http://ckeditor.com">CKEditor</a> in problem editing.</li>
				<li>2010-07-20 : Implement syntax highlighting in source viewing by using <a href="http://shjs.sourceforge.net">SHJS</a>.</li>
				<li>2010-08-17 : Use AJAX pagination in problem list to avoid overload.</li>
				<li>2010-08-17 : Post-contest submitting is enabled. Try it yourself :)</li>
				<li>2010-10-07 : Refer to <a href="http://code.google.com/p/virtual-judge/updates/list">this page</a>, and this section will not update.</li>
			</ul>
		</div>
		
		<s:include value="/bottom.jsp" />
	</body>
</html>
