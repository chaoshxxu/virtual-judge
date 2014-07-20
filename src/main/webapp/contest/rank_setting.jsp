<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<s:if test="sameContests.size() > 0">
<span><b>Time Machine</b>: (Check them to include their standings to your score board)</span>
<s:if test="contest.getEnableTimeMachine() == 0">
<br><span style="color:red">Time machine is disabled until the contest ends.</span>
</s:if>
<table id="same_contests_table" style="min-height:50px;margin:10px 0 30px 0" class="display" cellpadding="0" cellspacing="0" border="0">
	<thead>
		<tr>
			<th style="text-align:left;padding-left:3px"><s:checkbox id="checkAll" name="checkAll" /></th>
			<th></th>
			<th>Title</th>
			<th>Begin Time</th>
			<th>Length</th>
			<th>Manager</th>
		</tr>
	</thead>
	<tbody>
		<s:iterator value="sameContests" status="stat">
		<tr class="<s:property value='sameContests[#stat.index][7]' />">
			<td><s:checkbox fieldValue="%{sameContests[#stat.index][0]}" name="ids" /></td>
			<td><s:if test="sameContests[#stat.index][1] != null"><img height="20" title="Replay" src="images/replay.png"></s:if></td>
			<td><div style="white-space:normal;word-break:break-all;word-wrap:break-word;width:300px;">
				<s:if test="sameContests[#stat.index][0] == cid || contest.endTime.compareTo(curDate) > 0">
					<s:property value="sameContests[#stat.index][2]" escape="false" />
				</s:if>
				<s:else>
					<a href="contest/view.action?cid=<s:property value='sameContests[#stat.index][0]' escape="false" />#overview" target="_blank"><s:property value="sameContests[#stat.index][2]" escape="false" /></a>
				</s:else>
			</div></td>
			<td class="date"><s:date name="sameContests[#stat.index][3]" format="yyyy-MM-dd HH:mm:ss" /></td>
			<td class="date"><s:property value='sameContests[#stat.index][4]' /></td>
			<td class="center"><a href="user/profile.action?uid=<s:property value='sameContests[#stat.index][6]' />"><s:property value="sameContests[#stat.index][5]" /></a></td>
		</tr>
		</s:iterator>	
	</tbody>
</table>
</s:if>

<table class="blue_border" style="width:100%;border:1px solid #A6C9E2;border-collapse:collapse;" border="1">
	<tr>
		<td style="width:150px;text-align:center"><b>Show Teams</b>:</td>
		<td style="width:280px">
			<input type="radio" onclick="this.blur()" value="0" id="showTeams0" name="showTeams"><label for="showTeams0">Show only top 50 teams</label><br>
			<input type="radio" onclick="this.blur()" value="1" id="showTeams1" name="showTeams"><label for="showTeams1">Show all teams (not available for IE)</label>
		</td>
		<td style="width:150px;text-align:center"><b>Team Name</b>:</td>
		<td>
			<input name="showUsername" id="showUsername" onclick="this.blur()" type="checkbox"><label for="showUsername">Username</label><br>
			<input name="showNick" id="showNick" onclick="this.blur()" type="checkbox"><label for="showNick">Nickname</label>
		</td>
	</tr>
	<tr>
		<td style="width:150px;text-align:center"><b>Show Animation</b>:</td>
		<td style="width:280px">
			<input name="showAnimation" id="showAnimation0" value="0" onclick="this.blur()" type="radio"><label for="showAnimation0">No</label><br>
			<input name="showAnimation" id="showAnimation1" value="1" onclick="this.blur()" type="radio"><label for="showAnimation1">Yes <span style="color:gray">(when not too many teams)</span></label>
		</td>
		<td style="width:150px;text-align:center"><b>Export Rank</b>:</td>
		<td>
			<input type="button" value="Export" id="exportRank" />
		</td>
    </tr>
</table>

<script type="text/javascript">
$("#same_contests_table").dataTable({
	"bPaginate": false,
	"bLengthChange": false,
	"bFilter": false,
	"bSort": false,
	"bInfo": false,
	"bAutoWidth": false
});
$("#same_contests_table_wrapper").css("min-height", "100px");

var	ids = $.cookie("contest_" + cid).split("_");
for (var i = 0; i < ids.length; i++){
	$("input[type=checkbox][value=" + ids[i] + "]").prop("checked",'true');
}

$("[name=showTeams]").eq($.cookie("show_all_teams")).prop("checked", "checked");
$("[name=showNick]").prop("checked", $.cookie("show_nick") == 'true' ? "checked" : "");
$("[name=showUsername]").prop("checked", $.cookie("show_username") == 'true' ? "checked" : "");
$("[name=showAnimation]").eq($.cookie("show_animation")).prop("checked", "checked");

$("[name=showTeams]").change(function(){
	if ($.browser.msie && $("[name=showTeams]:checked").val() == 1) {
		$("[name=showTeams]").eq(0).prop("checked", "checked");
	}
});

$("[name=ids]").click(function(){
	updateCheckAll();
});

$("#checkAll").click(function(){
	$("[name=ids]").prop("checked", $(this).prop("checked"));
});

$("#exportRank").button();
$("#exportRank").click(function(){
	var exportRankWindow = window.open('about:blank'); 
	exportRankWindow.document.write(exportRankHtml); 
});

updateCheckAll();

function updateCheckAll() {
	if ($("[name=ids]:checked").length == 0){
		$("#checkAll").attr("checked", false);
	} else if ($("[name=ids]:not(:checked)").length == 0){
		$("#checkAll").attr("checked", true);
	}
}
</script>

