var first = 1;
var timeoutHandle;

$(document).ready(function() {

	var id = "0";
	if (location.href.indexOf("id=") >= 0){
		id = location.href.match(/id=\d+/g).toString().substring(3);
	}
	
	isSup = $("#isSup").val() == 1;

	oTable = $('#status').dataTable({
		"bProcessing": true,
		"bServerSide": true,
		"sAjaxSource": "problem/fetchStatus.action",
		"iDisplayLength": 20,
		"bLengthChange": false,
		"bFilter": false,
		"bSort": false,
		"bInfo": false,
		"bAutoWidth": false,
		"bStateSave": true,
//		"sDom": '<pfr>t<il>',
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
							return "<a href='problem/viewProblem.action?id=" + oObj.aData[2] + "'>" + oObj.aData[11] + " " + oObj.aData[12] + "</a>";
						},
						"sClass": "prob_num"
					},
					{
						"fnRender": function ( oObj ) {
							var info = oObj.aData[3];
							if (oObj.aData[14]) {
								info = "<a href='problem/fetchSubmissionInfo.action?id=" + oObj.aData[0] + "' rel='facebox'>" + info + "</a>";
							}
							return info;
						},
						"sClass": "result"
					},
					{
						"fnRender": function ( oObj ) {
							return oObj.aData[15] == 0 ? oObj.aData[4] + " KB" : "";
						},
						"sClass": "memory"
					},
					{ 
						"fnRender": function ( oObj ) {
							return oObj.aData[15] == 0 ? oObj.aData[5] + " ms" : "";
						},
						"sClass": "time"
					},
					{ 
						"fnRender": function ( oObj ) {
							return oObj.aData[10] ? "<a " + (oObj.aData[10] == 2 ? "class='shared'" : "") + " target='_blank' href='problem/viewSource.action?id=" + oObj.aData[0] + "'>" + oObj.aData[6] + "</a>" : oObj.aData[6];
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
					{
						"fnRender": function ( oObj ) {
							return oObj.aData[13] > 0 ? "<a href='contest/view.action?cid=" + oObj.aData[13] + "#overview'>*</a>" : "";
						}
					},
					{"bVisible": false},
					{"bVisible": false},
					{"bVisible": false},
					{"bVisible": false},
					{"bVisible": false},
					{"bVisible": false},
					{"bVisible": false}
				],
		"fnServerData": function ( sSource, aoData, fnCallback ) {
			var un = $("[name='un']").val();
			var OJId = $("[name='OJId']").val();
			var probNum = $("[name='probNum']").val();
			var res = $("[name='res']").val();
		
			aoData.push( { "name": "un", "value": un } );
			if (first){
				aoData.push( { "name": "id", "value": id } );
			}
			aoData.push( { "name": "OJId", "value": OJId } );
			aoData.push( { "name": "probNum", "value": probNum } );
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
			$(nRow).addClass(aData[15] == 0 ? "yes" : aData[15] == 1 ? "no" : "pending");
			if (aData[16]) {
				$(nRow).addClass("working");
			}
			$(nRow).attr("id", aData[0]);
			$('a[rel=facebox]', $(nRow)).facebox({
				loadingImage : 'facebox/loading.gif',
				closeImage   : 'facebox/closelabel.png'
			});
			if (aData[15] == 2 && aData[16] == 0) {
				$(nRow).addClass("rejudge");
			} else if (isSup) {
				$("td:eq(8)", $(nRow)).addClass("rejudge");
			}
			return nRow;
		},
		
		"fnDrawCallback": function( oSettings ) {
			getResult();
		}
		
	});
	
	$("#status_last").remove();

	$("#form_status").submit(function(){
		$(".errorMessage").remove();
		var id = $("[name='id']").val();
		if (!id || parseInt(id)) {
			oTable.fnPageChange( 'first' );
		}
		return false;
	});
	
	$("#reset").click(function(){
		if (location.href.indexOf("id=") >= 0){
			location.href = "problem/status.action";
		} else {
			$(".errorMessage").remove();
			$("[name='un']").val("");
			$("[name='OJId']").val("All");
			$("[name='probNum']").val("");
			$("[name='res']").val(0);
			oTable.fnPageChange( 'first' );
		}
	});
	
	$(".rejudge").live("click", function(){
		var $this = $(this);
		var $row = $this.is("td") ? $this.parent() : $this;
		var id = $row.attr("id");
		$row.removeClass("no");
		$row.removeClass("yes");
		$row.addClass("working");
		$row.removeClass("rejudge");
		$.post("problem/rejudge.action", {id: id}, function() {
			clearTimeout(timeoutHandle);
			timeoutHandle = setTimeout(getResult, 1000);
		});
	});
	
	$(".rejudge a").live("click", function(event){
		event.stopPropagation();
	});

	if (location.href.indexOf("reset") >= 0 || location.href.indexOf("id=") >= 0){
		oTable.fnPageChange( 'first' );
	}
	
	first = 0;
	
});

function getResult(){
	var workingRunIds = [];
	$("tr.working").each(function(){
		workingRunIds.push($(this).attr("id"));
	});
	if (!workingRunIds.length) {
		return;
	}

	judgeService.getResult(workingRunIds, function(back){
		for (var rowBack in back) {
			cbRow(back[rowBack]);
		}
		clearTimeout(timeoutHandle);
		timeoutHandle = setTimeout(getResult, 2000);
	});
}

function cbRow(back){
	var id = back[0];
	var result = back[1];
	var memory = back[2];
	var time = back[3];
	var info = back[4];
	var color = back[5];
	var working = back[6];
	
	var $row = $("#" + id);
	if (!$row.length){
		return;
	}
	if (info) {
		result = "<a href='problem/fetchSubmissionInfo.action?id=" + id + "' rel='facebox'>" + result + "</a>";
	}
	$row.removeClass("pending");
	$row.removeClass("no");
	$row.removeClass("yes");
	$row.removeClass("working");
	if (color == 0) {
		$row.addClass("yes");
		$(".memory", $row).html(memory + " KB");
		$(".time", $row).html(time + " ms");
	} else if (color == 1) {
		$row.addClass("no");
	} else {
		$row.addClass("pending");
	}
	if (working) {
		$row.addClass("working");
	} else if (color == 2) {
		$row.addClass("rejudge");
	}
	
	$(".result", $row).html(result);
	$('a[rel=facebox]', $row).facebox({
		loadingImage : 'facebox/loading.gif',
		closeImage   : 'facebox/closelabel.png'
	});
}
