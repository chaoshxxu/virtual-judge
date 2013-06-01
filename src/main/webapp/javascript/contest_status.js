var timeoutInstance = {};

$(document).ready(function() {
	
	var href = location.href.match(/cid=\d+/g).toString();
	var cid = href.substring(4);
	
	oTable = $('#status').dataTable({
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
							return "<a href='contest/viewProblem.action?pid=" + oObj.aData[11] + "'>" + oObj.aData[2] + "</a>";
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
		$(".errorMessage").remove();
		$("[name='un']").val("");
		$("[name='num']").val("-");
		$("[name='res']").val(0);
		oTable.fnPageChange( 'first' );
	});
	
	$(".rejudge").live("click", function(){
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

	
	if (location.href.indexOf("reset") >= 0){
		oTable.fnPageChange( 'first' );
	}
});

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
			clearTimeout(timeoutInstance[id]);
			timeoutInstance[id] = setTimeout("getResult(" + id + ")", 3000);
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
