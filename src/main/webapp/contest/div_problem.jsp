<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<div id="problem" style="font-size:14px">
	<div id="problem_number_container" style="margin:auto;text-align:center;display:none">
		<s:radio id="problem_number" name="problem_number" value="'A'" list="%{numList}" onclick="this.blur()" />
	</div>

	<div class="plm" style="clear:both">
		<div class="ptt" id="problem_title"></div>
		<span id="crawling" style="display:none" class="crawlInfo">
			<b><font color="green">Crawling in process...</font></b>
		</span>
		<span id="crawlFailed" style="display:none" class="crawlInfo">
			<b><font color="red">Crawling failed</font></b>
		</span>
		<span id="crawlSuccess" style="display:none" class="crawlInfo">
			<b>Time Limit:</b><span id="timeLimit"></span>MS&nbsp;&nbsp;&nbsp;&nbsp;
			<b>Memory Limit:</b><span id="memoryLimit"></span>KB&nbsp;&nbsp;&nbsp;&nbsp;
			<b>64bit IO Format:</b><span id="_64IOFormat"></span>
		</span>
		<div id="problem_opt" style="font-size:12px;margin-top:10px">
			<a id="submit" href="javascript:void(0)">Submit</a>
			<a id="problem_status" href="javascript:void(0)">Status</a>
			<s:if test="contestOver == 1 || #session.visitor.sup == 1 || #session.visitor.id == contest.manager.id">
				<a id="problem_practice" target="_blank" href="">Practice</a>
				<a id="problem_origin" target="_blank" href="">_</a>
			</s:if>
		</div>
	</div>

	<div style="width:960px;margin:auto">
		<s:if test="#session.visitor.sup == 1 || #session.visitor.id == contest.manager.id">
			<div style="margin:auto;padding-top:10px;text-align:center;color:green">
				Appoint description: <select id="desc_index" name="desc_index"></select>
			</div>
		</s:if>
		<div class="hiddable" id="vj_description"><p class="pst">Description</p><div class="textBG"></div></div>
		<div class="hiddable" id="vj_input"><p class="pst">Input</p><div class="textBG"></div></div>
		<div class="hiddable" id="vj_output"><p class="pst">Output</p><div class="textBG"></div></div>
		<div class="hiddable" id="vj_sampleInput"><p class="pst">Sample Input</p><div class="textBG"></div></div>
		<div class="hiddable" id="vj_sampleOutput"><p class="pst">Sample Output</p><div class="textBG"></div></div>
		<div class="hiddable" id="vj_hint"><p class="pst">Hint</p><div class="textBG"></div></div>
	</div>
	
	<div id="dialog-form-submit" style="display:none" title="Submit">
		<p class="validateTips"></p>
		<table>
			<tr>
				<td style="text-align:right;padding:5px">Problem:</td>
				<td><span id="submit_num" style="color:green"></span></td>
			</tr>
			<tr>
				<td style="text-align:right;padding:5px">Language:</td>
				<td><select id="language" name="language"></select></td>
			</tr>
			<tr>
				<td style="text-align:right;padding:5px">Public:</td>
				<td><s:radio name="isOpen" list="#{'0':'No', '1':'Yes'}" value="#session.visitor.share"></s:radio></td>
			</tr>
		</table>
		<textarea name="source" class="text ui-widget-content ui-corner-all" style="width:100%;height:360px;background:#F4F4F4;font-family: Courier New,Courier,monospace"></textarea>
	</div>
</div>
