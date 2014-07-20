var cid;	//current contest
var cids;	//cid concerned in rank
var startTime;	//page loaded time locally
var selectedTime;	//slider
var ti;	//Time Info
var tabs;
var slider;
var problemSet = {};
var statusTable;
var hash;
var oldProblemHash = "#problem/A";
var oldStatusHash;
var rankTable;
var ranks = {};
var lastRankUpdateTime = -99999999;
var sliderUpdater;
var statusTimeoutInstance = {};	//status fetch
var oFH;
var exportRankHtml;

$(function(){
	
	///////////////////// miscellaneous ///////////////////////
	
	cid = $("#cid").val();

	DWREngine.setAsync(false);
	judgeService.getContestTimeInfo(cid, function(res){
		startTime = new Date().valueOf();
		ti = res;
		$("#time_total span").text(dateFormat(ti[0]));
	});
	DWREngine.setAsync(true);

	/////////////////////   Slider    //////////////////////

	var curTime, exceedMax;
	slider = $( "#time_controller" ).slider({
		range: "min",
		min: 0,
		max: ti[0],
		value: 0,
		start: function() {
			clearInterval(sliderUpdater);
			curTime = new Date().valueOf();
			sliderUpdater = 0;
		},
		slide: function( event, ui ) {
			selectedTime = parseInt(ui.value);
			displayTime();
			if (ui.value > ti[1] + curTime - startTime) {
				exceedMax = true;
				return false;
			}
		},
		stop: function( event, ui ) {
			if (ui.value > ti[1] + curTime - startTime) {
				exceedMax = true;
			}
			if (exceedMax) {
				resetTimeSlider();
				exceedMax = false;
				if (location.hash.indexOf("#rank") == 0) {
					location.hash = "#rank";
					$.scrollTo( {top: '0px',left:'0px'}, 0 );
				}
			} else {
				selectedTime = parseInt(ui.value);
				displayTime();
				if (location.hash.indexOf("#rank") == 0) {
					location.hash = "#rank/" + selectedTime;
				}
				updateRankInfo();
			}
		}
	});
	
	/////////////////////   Tabs    //////////////////////

	tabs = $("#contest_tabs").tabs({
		select: function(event, ui) {
			if (location.hash.indexOf(ui.tab.rel) != 0) {
				if (ui.tab.rel == "#problem") {
					location.hash = oldProblemHash;
				} else {
					location.hash = ui.tab.rel;
				}
			}
			//deal with rank update
			if (location.hash.indexOf("#rank") == 0) {
				$("#contest_tabs").css("min-width", 400 + $("table#viewContest tr").length * 80 + "px");
			} else {
				$("#contest_tabs").css("min-width", 0);
			}
		}
	});
	
	/////////////////////   Overview    //////////////////////
	$('#viewContest').dataTable({
		"bPaginate": false,
		"bLengthChange": false,
		"bFilter": false,
		"bSort": false,
		"bInfo": false,
		"bAutoWidth": false
	});
	
	$("span.plainDate").each(function(){
		$(this).html(new Date(parseInt($(this).html())).format("yyyy-MM-dd hh:mm:ss"));
	});
	
	$("#contest_opt a").button();
	
	$("#clone_contest").click(function(){
		var url = this.href;
		doIfLoggedIn(function(){
			location.href = url;
		}, url);
		return false;
	});
	
	/////////////////////    Problem    //////////////////////

	$("#problem_opt").find("a").button();

	$(":radio[name=problem_number]").each(function(){
		$(this).next().html($(this).val());
	});
	
	$("input[name=problem_number]").change(function(){
		location.hash = "#problem/" + $(this).val();
	});
	
	$("#problem_number_container").buttonset().show();
	
	$("#desc_index").change(function(){
		var num = $(":input[name=problem_number]:checked").val();
		var did = $("#desc_index > option:eq(" + this.value + ")").attr("did");
		showDescription(num, this.value);
		$.post("contest/appointDescription.action?cid=" + cid + "&num=" + num + "&id=" + did);
	});
	
	$("#submit").click(function(){
		doIfLoggedIn(function(){
			var num = $(":input[name=problem_number]:checked").val();
			var problem = problemSet[num];
			if (!!problem) {
				$( "#submit_num" ).html(num + " - " + problem.title);
				var languageSelect = $("select#language");
				languageSelect.html("");
				for (i in problem.languageList) {
					languageSelect.append("<option value='" + i + "'>" + problem.languageList[i] + "</option>");
				}
				if ($.cookie("lang_" + problem.oj)) {
					$("select#language").val($.cookie("lang_" + problem.oj));
				}
				$( "#dialog-form-submit" ).dialog("open");
			}
		});
		return false;
	});
	
	$( "#dialog-form-submit" ).dialog({
		autoOpen: false,
		height: 600,
		width: 600,
		position: ['top', 50],
		modal: true,
		buttons: {
			"Submit": function() {
				updateTips("Submitting...");
				$( "#dialog-form-submit" ).parent().find("button:first").hide();

				var num = $(":input[name=problem_number]:checked").val();
				var data = {
					cid: cid,
					num: num,
					language: $("[name=language]").val(),
					isOpen: $(":input[name=isOpen]:checked").val(),
					source: Base64.encode($("[name=source]").val())
				};
				$.cookie("lang_" + problemSet[num].oj, $("select#language").val(), {expires:30, path:'/'});
				$.ajax({
					type : "POST",
					url : "contest/submit.action",
					data : data,
					success : function(res){
						if (res == "success") {
							$( "#dialog-form-submit" ).dialog( "close" );
							showStatus();
							$("#reset").trigger("click");
						} else {
							updateTips(res);
							$( "#dialog-form-submit" ).parent().find("button:first").show();
						}
					},
					error : function(){
						updateTips("Connection timeout.");
						$( "#dialog-form-submit" ).parent().find("button:first").show();
					}
				});
			},
			"Cancel": function() {
				$( this ).dialog( "close" );
			}
		},
		close: function() {
			$( "#dialog-form-submit" ).parent().find("button:first").show();
			$("p.validateTips").html("");
			$( this ).find("textarea").val("");
		}
	});

	/////////////////////    Status     //////////////////////
	
	$("#num").prepend("<option value='-'>All</option>");
	$("[name=num]").val("-");
	$("#reset, #filter").button();
	
	$("#form_status").submit(function(){
		var oldHash = location.hash;
		$("[name='un']").val($("[name='un']").val().replace(/%\d\d|\s+/g, ''));
		location.hash = "#status/" + $("[name='un']").val() + "/" + $("[name='num']").val() + "/" + $("[name='res']").val();
		if (location.hash == oldHash) {
			statusTable.fnPageChange( 'first' );
		}
		return false;
	});
	
	$("#reset").click(function(){
		var oldHash = location.hash;
		location.hash = "#status//-/0";
		if (location.hash == oldHash) {
			statusTable.fnPageChange( 'first' );
		}
		return false;
	});
	
	$("a.rejudge").live("click", function(){
		var $row = $(this).parent().parent();
		var id = $row.attr("id");
		$row.removeClass("no");
		$row.removeClass("yes");
		$row.addClass("pending");
		$.post("problem/rejudge.action", {id: id}, function() {
			getResult(id);
		});
		return false;
	});
	
	/////////////////////     Rank      //////////////////////
	
	$("#rank").css("width", 400 + $("table#viewContest tr").length * 80 + "px");

	if (!$.browser.msie) {
		$("div.meta_td").live({
			mouseenter: function() {
				var curCid = $(this).parent().attr("cid");
				$("div[cid=" + curCid + "] div.meta_td").addClass("same_td");
			},
			mouseleave:	function() {
				var curCid = $(this).parent().attr("cid");
				$("div[cid=" + curCid + "] div.meta_td").removeClass("same_td");
			}
		});
	}

	$("div.meta_td").live("click", function() {
		var curCid = $(this).parent().attr("cid");
		var rank = ranks[curCid];
		var end = new Date().valueOf() > rank.endTime;
		$.facebox("<table><tr><td style='font-weight:bold;padding:5px'>Title</td><td>" + (end && rank.cid != cid ? "<a href='contest/view.action?cid=" + rank.cid + "#overview' target='_blank'>" : "") + (rank.isReplay ? "<img src='images/replay.png' height='18' /> " : "") + rank.title + (end && rank.cid != cid ? "</a>" : "") + "</td></tr><tr><td style='font-weight:bold;padding:5px'>Begin Time</td><td>" + new Date(rank.beginTime).format("yyyy-MM-dd hh:mm:ss") + "</td></tr><tr><td style='font-weight:bold;padding:5px'>Length</td><td>" + dateFormat(rank.length) + "</td></tr><tr><td style='font-weight:bold;padding:5px'>Manager</td><td><a href='user/profile.action?uid=" + rank.managerId + "' target='_blank'>" + rank.managerName + "</a></td></tr></table>");
		$("#facebox").css({
			"z-index": "1000000",
			"position": "fixed",
			"top": "200px",
			"left": $(window).width() / 2 - 250 + "px"
		});
		$("#facebox .content").css("width", "500px");
	});
	
	$("div.penalty_td").live({
		mouseenter: function() {
			$(this).text($(this).attr("v0"));
		},
		mouseleave:	function() {
			$(this).text($(this).attr("v1"));
		}
	});
	
//	$("div.disp").live({
//		mouseenter: function() {
//			$(this).css("border", "1px solid black")
//		},
//		mouseleave:	function() {
//			$(this).css("border", "");
//		}
//	});

	$( "#dialog-form-rank-setting" ).dialog({
		autoOpen: false,
		width: 950,
		position: ['top', 50],
		modal: false,
		buttons: {
			"Save": function() {
				var ids = cid;
				$("[name=ids]:checked").each(function(){
					ids += "_" + $(this).val();
				});
				var showTeams = $.browser.msie ? 0 : $("[name=showTeams]:checked").val();
				var showNick = $("[name=showNick]:checked").val() == 'on';
				var showUsername = $("[name=showUsername]:checked").val() == 'on';
				var showAnimation = $("[name=showAnimation]:checked").val();
				
				$.cookie("contest_" + cid, ids, { expires: 3 });
				$.cookie("show_all_teams", showTeams, { expires: 30 });
				$.cookie("show_nick", showNick, { expires: 30 });
				$.cookie("show_username", showUsername, { expires: 30 });
				$.cookie("show_animation", showAnimation, { expires: 30 });
				$( this ).dialog( "close" );
				lastRankUpdateTime = -99999999;
				updateRankInfo();
			},
			"Cancel": function() {
				$( this ).dialog( "close" );
			}
		}
	});	
	
	$("#rank_setting").click(function(){
		var $inst = $( "#dialog-form-rank-setting" )
		$inst.dialog('open');
		if (!$inst.html()) {
			$inst.load("contest/showRankSetting.action?cid=" + cid);
		}
	});

	$(window).scroll(adjustRankTool);

	$("#img_find_me").click(function(){
		$.scrollTo( $("div.my_tr")[0], 800, {offset: {top:120-$(window).height(), left:-100} } );
		return false;
	});

	$("#img_go_top").click(function(){
		$.scrollTo( {top: '0px',left:'0px'}, 800 );
		return false;
	});
	
	$("#div_rank_tool > img").mouseover(function() {
		$(this).css("background", "#CCEEFF");
	}).mouseout(function() {
		$(this).css("background", "transparent");
	});
	
	//////////////////////////////////////////////////////////

	resetTimeSlider();
	updateRankInfo();
	
	$(window).hashchange( function(){
		hash = location.hash.split("/");
		if (hash[0] == "#problem") {
			showProblem();
		} else if (hash[0] == "#status") {
			showStatus();
		} else if (hash[0] == "#rank") {
			showRank();
		} else if (hash[0] == "#discuss") {
			showDiscuss();
		} else {
			showOverview();
		}
	}).hashchange();
	
	$("#contest_tabs").show();

});

function showOverview() {
	tabs.tabs( "select" , "overview" );
}

function showProblem() {
	tabs.tabs( "select" , "problem" );

	oldProblemHash = location.hash;
	var $numRadio = $("#problem_number_container > input[value=" + hash[1] + "]");
	if ($numRadio.length) {
		$numRadio.prop("checked", "checked");
		$("#problem_number_container").buttonset("refresh");
	}
	var num = $("#problem_number_container input:checked").val();
	if (!problemSet[num]) {
		$.ajax({
			url: "contest/showProblem.action?cid=" + cid + "&num=" + num,
			type: 'get',
			async: false,
			success: function(data) {
				problemSet[num] = data;
			}
		});
	}
	var problem = problemSet[num];
	$("#problem_title").html("<span style='color:green'>" + num + " - </span>" + problem.title);
	$("span.crawlInfo").hide();
	if (problem.timeLimit == 1) {
		$("#crawling").show();
	} else if (problem.timeLimit == 2) {
		$("#crawlFailed").show();
	} else {
		$("#timeLimit").html(problem.timeLimit);
		$("#memoryLimit").html(problem.memoryLimit);
		$("#_64IOFormat").html(problem._64IOFormat);
		$("#problem_status").attr("href", "contest/view.action?cid=" + cid + "#status//" + num + "/0");
		if (problem.pid) {
			$("#problem_practice").attr("href", "problem/viewProblem.action?id=" + problem.pid);
		}
		if (problem.originProblemNumber) {
			$("#problem_origin").button("destroy");
			$("#problem_origin").attr("href", problem.originURL).text(problem.originProblemNumber);
			$("#problem_origin").button();
		}
		$("#crawlSuccess").show();
	}
	$("#desc_index").html("");
	for (i in problem.descriptions) {
		if (problem.descriptions[i].author == '0') {
			problem.descriptions[i].author = "System Crawler";
		}
		$("#desc_index").append("<option did='" + problem.descriptions[i].id + "' value='" + i + "'>" + problem.descriptions[i].author + "  (" + problem.descriptions[i].updateTime + ")" + "</option>");
	}
	showDescription(num, problem.desc_index);
}

function showStatus() {
	tabs.tabs( "select" , "status" );

	if (hash.length >= 4) {
		$("[name='un']").val(hash[1]);
		$("[name='num']").val(hash[2]);
		$("[name='res']").val(hash[3]);
	}
	
	if (!statusTable) {
		statusTable = $('#table_status').dataTable({
			"bProcessing": true,
			"bServerSide": true,
			"sAjaxSource": "contest/fetchStatus.action?cid=" + cid,
			"iDisplayLength": 20,
			"bLengthChange": false,
			"bFilter": false,
			"bSort": false,
			"bInfo": false,
			"bAutoWidth": false,
			"bStateSave": true,
			"sPaginationType": "full_numbers",
	
			"aoColumns": [
				{},
				{
					"fnRender": function ( oObj ) {
						return "<a href='user/profile.action?uid=" + oObj.aData[9] + "'>" + oObj.aData[1] + "</a>";
					}
				},
				{
					"fnRender": function ( oObj ) {
						return "<a href='contest/view.action?cid=" + cid + "#problem/" + oObj.aData[2] + "'>" + oObj.aData[2] + "</a>";
					}
				},
				{
					"fnRender": function ( oObj ) {
						var info = oObj.aData[3] == 'Judging Error 1' || oObj.aData[3] == 'Judging Error 2' && $("[name='isSup']").val() != 0 ? oObj.aData[3] + " <a href='#' class='rejudge' ><img border=0 height='15' src='images/refresh.png'/></a>" : oObj.aData[3];
						if (oObj.aData[12]) {
							info = "<a href='contest/fetchSubmissionInfo.action?id=" + oObj.aData[0] + "' rel='facebox'>" + info + "</a>";
						}
						return info;
					},
					"sClass": "result"
				},
				{
					"fnRender": function ( oObj ) {
						return oObj.aData[3] == 'Accepted' ? oObj.aData[4] + " KB" : "";
					},
					"sClass": "memory"
				},
				{ 
					"fnRender": function ( oObj ) {
						return oObj.aData[3] == 'Accepted' ? oObj.aData[5] + " ms" : "";
					},
					"sClass": "time"
				},
				{ 
					"fnRender": function ( oObj ) {
						return oObj.aData[10] ? "<a " + (oObj.aData[10] == 2 ? "class='shared'" : "") + " target='_blank' href='contest/viewSource.action?id=" + oObj.aData[0] + "'>" + oObj.aData[6] + "</a>" : oObj.aData[6];
					},
					"sClass": "language"
				},
				{
					"fnRender": function ( oObj ) {
						return oObj.aData[7] + " B";
					},
					"sClass": "length"
				},
				{
					"fnRender": function ( oObj ) {
						return new Date(parseInt(oObj.aData[8])).format("yyyy-MM-dd hh:mm:ss");
					},
					"sClass": "date"
				},
				{"bVisible": false},
				{"bVisible": false},
				{"bVisible": false},
				{"bVisible": false}
			],
			"fnServerData": function ( sSource, aoData, fnCallback ) {
				var un = $("[name='un']").val();
				var num = $("[name='num']").val();
				var res = $("[name='res']").val();
			
				aoData.push( { "name": "un", "value": un } );
				aoData.push( { "name": "num", "value": num } );
				aoData.push( { "name": "res", "value": res } );

				$.ajax( {
					"dataType": 'json', 
					"type": "POST", 
					"url": sSource, 
					"data": aoData, 
					"success": fnCallback
				} );
			},
			"fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
				$(nRow).addClass(aData[3]=="Accepted" ? "yes" : aData[3].indexOf("ing") < 0 || aData[3].indexOf("rror") >= 0 ? "no" : "pending");
				$(nRow).attr("id", aData[0]);
				$('a[rel=facebox]', $(nRow)).facebox({
					loadingImage : 'facebox/loading.gif',
					closeImage   : 'facebox/closelabel.png'
				});
				if ($(nRow).hasClass("pending")){
					getResult(aData[0]);
				}
				return nRow;
			}
		});
		$("#table_status_last").remove();
	} else if (location.hash != oldStatusHash || statusTable.fnSettings()._iDisplayStart == 0) {
		statusTable.fnPageChange( 'first' );
		oldStatusHash = location.hash;
	}
}

function showRank() {
	tabs.tabs( "select" , "rank" );
	
	$("#contest_tabs").css("min-width", 400 + $("table#viewContest tr").length * 80 + "px");

	if (!isNaN(hash[1]) && hash[1] >= 0) {
		if (hash[1] <= Math.min(ti[1] + new Date().valueOf() - startTime, ti[0])) {
			selectedTime = parseInt(hash[1]);
			slider.slider("value", selectedTime);
			displayTime();
			clearInterval(sliderUpdater);
			sliderUpdater = 0;
		}
	}
}

function showDiscuss() {
	tabs.tabs( "select" , "discuss" );

	if (!$("#disqus_thread").html()) {
		_showDiscuss();
	}
}

function displayTime() {
	var ratio = Math.max(selectedTime, 0) / ti[0];
	$("#time_index").css("width", (2 + 100 * ratio) + "%");
	if (ratio > 0.93) {
		$("#time_total span").text("");
		$("#time_index span").text(dateFormat(selectedTime == ti[0] ? ti[0] : selectedTime - ti[0]));
	} else {
		$("#time_total span").text(dateFormat(ti[0]));
		$("#time_index span").text(dateFormat(selectedTime));
	}
};

function updateRankInfo() {
	if (selectedTime < 0) {
		return;
	}
	if (Math.abs(selectedTime - lastRankUpdateTime) < 10000) {
		return;
	}
	
	if ($.cookie("contest_" + cid) == undefined){
		$.cookie("contest_" + cid, cid, { expires: 3 });
	}
	if ($.cookie("show_all_teams") == undefined){
		$.cookie("show_all_teams", 0, { expires: 30 });
	}
	if ($.cookie("show_nick") == undefined){
		$.cookie("show_nick", true, { expires: 30 });
	}
	if ($.cookie("show_username") == undefined){
		$.cookie("show_username", true, { expires: 30 });
	}
	if ($.cookie("show_animation") == undefined){
		$.cookie("show_animation", 1, { expires: 30 });
	}

	cids = $.cookie("contest_" + cid).split("_");

	var cnt = 0;
	for (var i = 0; i < cids.length; i++) {
		if (ranks[cids[i]] == undefined) {
			judgeService.getRankInfo(cids[i], function(res){
				ranks[res.cid] = {
					cid: res.cid,
					dataURL: res.dataURL,
					isReplay: res.isReplay,
					title: res.title,
					managerId: res.managerId,
					managerName: res.managerName,
					endTime: new Date().valueOf() + res.remainingLength,	//locally
					beginTime: res.beginTime,	//server side
					length: parseInt(res.length),
					lastFetchTime: 0
				};
				if (++cnt == cids.length) {
					updateRankData();
				}
			});
		} else if (++cnt == cids.length) {
			updateRankData();
		}
	}
}

function updateRankData() {
	var cnt = 0;
	for (var i = 0; i < cids.length; i++) {
		if (ranks[cids[i]].length && (!ranks[cids[i]].data || ranks[cids[i]].lastFetchTime < Math.min(startTime + selectedTime - ti[1], ranks[cids[i]].endTime))) {
			var curTime = new Date().valueOf();
			var url = ranks[cids[i]].dataURL + (ranks[cids[i]].endTime < curTime ? "" : "?" + curTime);
			//var url = ranks[cids[i]].dataURL;
			$.getJSON(url, function(rankData) {
				var curCid = rankData[0];
				ranks[curCid].data = rankData;
				ranks[curCid].lastFetchTime = new Date().valueOf();
				if (++cnt == cids.length){
					calcRankTable();
				}
			});
		} else if (++cnt == cids.length){
			calcRankTable();
		}
	}
}

function calcRankTable() {
	var pnum = $("table#viewContest tr").length - 1;
	var sb = {}
	var firstSolveTime = [];
	var totalSubmission = [];
	var correctSubmission = [];
	var myStatus = [];
	var username = {};
	var nickname = {};
	var my_uid_cid = $("#my_account").attr("uid") + "_" + cid;
	
	for (var j = 0; j < pnum; ++j) {
		totalSubmission[j] = correctSubmission[j] = 0;
	}
	$.each(cids, function(i, curCid) {
		if (isNaN(curCid) || !ranks[curCid].length) {
			return;
		}
		$.each(ranks[curCid].data, function(key, s) {
			if (key == 0) {
				return;
			} else if (key == 1) {
				for (uid in s) {
					var name = s[uid];
					username[uid] = name[0];
					nickname[uid] = name[1];
				}
				return;
			}
			if (s[3] * 1000 > selectedTime)return;
			var name = s[0] + "_" + curCid;
			if (!sb[name]){
				sb[name] = [];
			}
			if (sb[name][s[1]] == undefined){
				sb[name][s[1]] = [-1, 0];
			}
			if (sb[name][s[1]][0] < 0){
				totalSubmission[s[1]]++;
				if (s[2]) {
					sb[name][s[1]][0] = s[3];
					if (firstSolveTime[s[1]] == undefined || s[3] < firstSolveTime[s[1]]) {
						firstSolveTime[s[1]] = s[3];
					}
					correctSubmission[s[1]]++;
				} else {
					sb[name][s[1]][1]++;
				}
			}
		});
	});

	var result = [];
	for (name in sb){
		var solve = 0, penalty = 0;
		for (i in sb[name]){
			if (sb[name][i]) {
				if (sb[name][i][0] >= 0) {
					if (name == my_uid_cid) {
						myStatus[i] = 2;
					}
					solve++;
					penalty += sb[name][i][0] + 1200 * sb[name][i][1];
				} else if (name == my_uid_cid) {
					myStatus[i] = 1;
				}
			}
		}
		result.push([name, solve, penalty]);
	}
	result.sort(function(a, b){
		return b[1] - a[1] || a[2] - b[2];
	});
	
	var showNick = $.cookie("show_nick") == 'true';
	var showUsername = $.cookie("show_username") == 'true';
	var showAllTeams = $.cookie("show_all_teams");
	var sbHtml = [];
	
	for (var i = 0; i < result.length; ++i) {
		var curInfo = result[i];
		var splitIdx = curInfo[0].lastIndexOf("_");
		var uid = curInfo[0].substr(0, splitIdx);
		var curCid = curInfo[0].substr(splitIdx + 1);
		if (showAllTeams == 0 && i >= 50 && (cid != curCid || !username[uid])) {
			continue;
		}
		sbHtml.push("<div data-id='" + escape(curInfo[0]).replace(/\W/g, "_") + "' class='disp");
		if (cid == curCid) {
			sbHtml.push(" cur_tr");
			if (my_uid_cid == curInfo[0]) {
				sbHtml.push(" my_tr");
			}
		}
		sbHtml.push("' cid='" + curCid + "'><div class='meta_td rank'>" + (i + 1) + "</div><div class='meta_td id");
		if (username[uid]) {
			var displayTitle =
				showNick == showUsername ?
						nickname[uid] && username[uid] != nickname[uid] ?
								username[uid] + " (" + nickname[uid] + ")" :
								username[uid] :
				showNick && nickname[uid] ?
						nickname[uid] :
						username[uid];
			var displayName =
				showNick == showUsername ?
						nickname[uid] && username[uid] != nickname[uid] ?
								username[uid] + "<span style='color:gray'>(" + nickname[uid] + ")</span>" :
								username[uid] :
				showNick && nickname[uid] ?
						nickname[uid] :
						username[uid];
			sbHtml.push("' title='" + displayTitle + "'><a target='_blank' href='user/profile.action?uid=" + uid + "'>" + displayName +  "</a></div>");
		} else {
			sbHtml.push(" replay' title='" + uid + "'>" + uid + "</div>");
		}
		var penaltyInHMS = dateFormat(curInfo[2], 0, 1);
		var penaltyInMinute = dateFormat(curInfo[2], 1, 1);
		sbHtml.push("<div class='meta_td solve'>" + curInfo[1] + "</div><div class='meta_td penalty_td standing_time' v0='" + penaltyInHMS + "' v1='" + penaltyInMinute + "'>" + penaltyInMinute + "</div>");

		var thisSb = sb[curInfo[0]];
		for (var j = 0; j <= pnum; ++j) {
			var probInfo = thisSb[j];
			if (!probInfo) {
				sbHtml.push("<div class='standing_time'/>");
			} else {
				sbHtml.push("<div ");
				if (probInfo[0] < 0) {
					sbHtml.push("class='red");
				} else if (firstSolveTime[j] == probInfo[0]) {
					sbHtml.push("class='solvedfirst");
				} else {
					sbHtml.push("class='green");
				}
				sbHtml.push(" standing_time'>" + dateFormat(probInfo[0], 0, 1) + "<br />" + (probInfo[1] ? "<span>(-" + probInfo[1] + ")</span>" : "　") + "</div>");
			}
		}
		sbHtml.push("</div>");
	}
	
	exportRankHtml = ["<tr><th>Rank</th><th>Id</th><th>Solve</th><th>Penalty</th>"];
	for (var i = 0; i < pnum; i++) {
		exportRankHtml.push("<th>" + String.fromCharCode(65 + i) + "</th>")
	}
	exportRankHtml.push("</tr>");
	for (var i = 0; i < result.length; ++i) {
		var curInfo = result[i];
		var splitIdx = curInfo[0].lastIndexOf("_");
		var uid = curInfo[0].substr(0, splitIdx);
		var curCid = curInfo[0].substr(splitIdx + 1);
		if (showAllTeams == 0 && i >= 50 && (cid != curCid || !username[uid])) {
			continue;
		}
		exportRankHtml.push("<tr><td>" + (i + 1) + "</td><td>");
		if (username[uid]) {
			exportRankHtml.push(
				(
					showNick == showUsername ?
							nickname[uid] && username[uid] != nickname[uid] ?
									username[uid] + "(" + nickname[uid] + ")" :
									username[uid] :
					showNick && nickname[uid] ?
							nickname[uid] :
							username[uid]
				) + "</td>");
		} else {
			exportRankHtml.push(uid + "</td>");
		}
		var penaltyInHMS = dateFormat(curInfo[2], 0, 1);
		exportRankHtml.push("<td>" + curInfo[1] + "</td><td>" + penaltyInHMS + "</td>");

		var thisSb = sb[curInfo[0]];
		for (var j = 0; j <= pnum; ++j) {
			var probInfo = thisSb[j];
			if (!probInfo) {
				exportRankHtml.push("<td/>");
			} else {
				exportRankHtml.push("<td>" + dateFormat(probInfo[0], 0, 1) + (probInfo[1] ? "(-" + probInfo[1] + ")" : "") + "</td>");
			}
		}
		exportRankHtml.push("</tr>");
	}
	exportRankHtml = ("<table>" + exportRankHtml.join("") + "</table>").replace("　", "");

	var maxCorrectNumber = 0, totalNumber = 0, totalCorrectNumber = 0;
	for (var j = 0; j < pnum; ++j) {
		totalNumber += totalSubmission[j];
		totalCorrectNumber += correctSubmission[j];
		if (maxCorrectNumber < correctSubmission[j]) {
			maxCorrectNumber = correctSubmission[j];
		}
	}
	for (var j = 0; j < pnum; ++j) {
		if (totalSubmission[j]) {
			var ratio = maxCorrectNumber ? correctSubmission[j] / maxCorrectNumber : 0.0;
			$("#rank_foot div").eq(j + 4).css("background-color", grayDepth(ratio)).css("color", ratio < .5 ? "black" : "white").html((myStatus[j] == 2 ? "<img src='images/yes.png' height='20'/>" : myStatus[j] == 1 ? "<img src='images/no.png' height='20'/>" : "　") + 	"<br />" + correctSubmission[j] + "/" + totalSubmission[j] + "<br />" + Math.floor(100 * correctSubmission[j] / totalSubmission[j]) + "%");
		} else {
			$("#rank_foot div").eq(j + 4).css("background", "transparent").html("");
		}
		if (myStatus[j] == 2) {
			$("#viewContest tbody tr:eq(" + j + ") td:eq(0)").html("<img src='images/yes.png' height='15'/>");
		} else if (myStatus[j] == 1) {
			$("#viewContest tbody tr:eq(" + j + ") td:eq(0)").html("<img src='images/no.png' height='15'/>");
		} else {
			$("#viewContest tbody tr:eq(" + j + ") td:eq(0)").html("");
		}
		if (totalSubmission[j] > 0) {
			$("#viewContest tbody tr:eq(" + j + ") td:eq(1)").html("<a href='contest/view.action?cid=" + cid + "#status//" + String.fromCharCode(65 + j)+ "/1'>" + correctSubmission[j] + "</a> / <a href='contest/view.action?cid=" + cid + "#status//" + String.fromCharCode(65 + j)+ "/0'>" + totalSubmission[j] + "</a>");
		} else {
			$("#viewContest tbody tr:eq(" + j + ") td:eq(1)").html("");
		}
	}
	if (totalNumber) {
		$("#rank_foot div").eq(pnum + 4).css("background-color", "#D3D6FF").html("　<br />" + totalCorrectNumber + "/" + totalNumber + "<br />" + Math.floor(100 * totalCorrectNumber / totalNumber) + "%");
	} else {
		$("#rank_foot div").eq(pnum + 4).css("background-color", "transparent").html("");
	}
	
	$("#rank_header").width($("#rank").css("width"));
	$("#rank_foot").width($("#rank").css("width"));

	$("#rank_data_destination").html(sbHtml.join(""))
	.prepend($("#rank_header").clone().css({"position": "", "top": "", "z-index": ""}).attr("id", "rank_header_1").show())
	.append($("#rank_foot").clone().css({"position": "", "bottom": "", "z-index": ""}).attr("id", "rank_foot_1").show());
	
	if (location.hash.indexOf("#rank") == 0 && $("#rank_data_source > div").length > 2 && $.cookie("show_animation") > 0 && $("#rank_data_destination > div").length <= ($.browser.msie ? $.browser.version > 6 ? 35 : 0 : 70)) {
		$('#rank_data_destination > div').each(function(){
			$("#rank_data_source > div[data-id=" + $(this).attr("data-id") + "]").html(this.innerHTML);
		});
		$('#rank_data_source').quicksand( $('#rank_data_destination > div'), {
			"duration": 1000
		}, function() {
			$("#rank_data_destination").html("");
			adjustRankTool();
		});
	} else {
		$('#rank_data_source').removeAttr("style").html($('#rank_data_destination').html());
		$("#rank_data_destination").html("");
		adjustRankTool();
	}
	
	lastRankUpdateTime = selectedTime;
}

function showDescription(num, desc_index) {
	var problem = problemSet[num];
	var description = problem.descriptions[desc_index];
	$("[name=desc_index]").val(desc_index);
	$("div.hiddable").hide();
	for (elem in description){
		if (description[elem]){
			$("#vj_" + elem).show();
			$("#vj_" + elem + " div").html(description[elem]);
		}
	}
	problem.desc_index = desc_index;
}

function getResult(id){
	judgeService.getResult(id, cb);
}

function cb(back){
	var id = back[0];
	var result = back[1];
	var memory = back[2];
	var time = back[3];
	var info = back[4];
	var $row = $("#" + id);
	if ($row.length){
		if (info) {
			result = "<a href='problem/fetchSubmissionInfo.action?id=" + id + "' rel='facebox'>" + result + "</a>";
		}
		$(".result", $row).html(result);
		$('a[rel=facebox]', $row).facebox({
			loadingImage : 'facebox/loading.gif',
			closeImage   : 'facebox/closelabel.png'
		});
		if (result.indexOf("ing") >= 0 && result.indexOf("rror") < 0){
			clearTimeout(statusTimeoutInstance[id]);
			statusTimeoutInstance[id] = setTimeout("getResult(" + id + ")", 3000);
		} else if (result == "Accepted"){
			$row.removeClass("pending");
			$row.addClass("yes");
			$(".memory", $row).html(memory + " KB");
			$(".time", $row).html(time + " ms");
		} else {
			$row.removeClass("pending");
			$row.addClass("no");
		}
	}
}

function grayDepth(ratio) {
	var res = (Math.floor((1 - ratio) * 0xff) * 0x010101).toString(16);
	while (res.length < 6) res = '0' + res;
	return "#" + res;
}

function dateFormat(time, formatIdx, inSeconds){
	var sign = "";
	if (inSeconds != 1)	time /= 1000;
	if (time == -1)return "　";
	if (time < 0) {
		time = -time;
		sign = "-";
	}
	if (formatIdx == 1){
		return sign + Math.floor(time / 60);
	} else {
		var h = Math.floor(time / 3600);
		var m = Math.floor(time % 3600 / 60);
		var s = Math.floor(time % 60 + 0.5);
		return sign + h + ":" + (m<10?"0":"") + m + ":" + (s<10?"0":"") + s;
	}
}

function comfirmDeleteContest(cid){
	if (confirm("Sure to delete this contest?")){
		location = 'contest/deleteContest.action?cid=' + cid;
	}
}

function resetTimeSlider () {
	clearInterval(sliderUpdater);
	var temp = function(){
		selectedTime = Math.min(ti[1] + new Date().valueOf() - startTime, ti[0]);
		slider.slider("value", Math.max(selectedTime, 0));
		displayTime();
		if (selectedTime > 0 && selectedTime < 1000 && ti[0] > 0) {
			window.location.reload();
		}
		updateRankInfo();
	}
	temp();
	sliderUpdater = setInterval(temp, 1000);
};

function isScrolledIntoView(elem) {
	var docViewTop = $(window).scrollTop();
	var docViewBottom = docViewTop + $(window).height();

	var elemTop = $(elem).offset().top;
	var elemBottom = elemTop + $(elem).height();

	return ((elemBottom >= docViewTop) && (elemTop <= docViewBottom));
}

function adjustRankTool() {
	if (hash[0] != "#rank") {
		return;
	}
	var scrollLeft = $(window).scrollLeft();
	var $myRow = $("div.my_tr");
	if ($myRow.length && !isScrolledIntoView($myRow[0])) {
		$("#img_find_me").css("visibility", "visible");
	} else {
		$("#img_find_me").css("visibility", "hidden");
	}

	if (scrollLeft > 0 || !isScrolledIntoView("#contest_title")) {
		$("#img_go_top").css("visibility", "visible");
	} else {
		$("#img_go_top").css("visibility", "hidden");
	}
	
	var marginLeft = $("#contest_tabs").offset().left;
	$("#rank_header").css("left", 3+marginLeft-scrollLeft + "px");
	$("#rank_foot").css("left", 3+marginLeft-scrollLeft + "px");
	if (!isScrolledIntoView("#rank_header_1")) {
		$("#rank_header").show();
	} else {
		$("#rank_header").hide();
	}
	if (!isScrolledIntoView("#rank_foot_1")) {
		$("#rank_foot").show();
	} else {
		$("#rank_foot").hide();
	}
}
