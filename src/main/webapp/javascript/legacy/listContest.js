$(function() {
	if ($("#js_require_listContest").length == 0) {
		return;
	}

	$("input[type='checkbox']").each(function(){
		if ($.cookie("checked_" + $(this).attr("name")) == 'false') {
			$(this).removeAttr("checked");
		}
	});

	if ($.cookie("contestType") == undefined) {
		$.cookie("contestType", 0, {expires:7});
	}
	$("input[name='contestType']").get($.cookie("contestType")).checked = 1;
	
	var oTable = $('#listContest').dataTable({
		"bProcessing": true,
		"bServerSide": true,
		"sAjaxSource": basePath + "/contest/listContest.action",
		"iDisplayLength": 25,
		"bAutoWidth": false,
		"bStateSave": true,
		"sDom": '<"H"pfr>t<"F"il>',
		"oLanguage": {
			"sInfo": "_START_ to _END_ of _TOTAL_ contests",
			"sInfoEmpty": "No contests",
			"sInfoFiltered": " (filtering from _MAX_ total contests)"
		},
		"aaSorting": [[ 2, "desc" ]],
		"aoColumnDefs": [
			{ "asSorting": [ "desc", "asc" ], "aTargets": [ "_all" ] }
		],

		"aoColumns": [
					{ 
						"sClass": "id"
					},
					{
						"fnRender": function ( oObj ) {
							return "<div class='title'><a cid='" + oObj.aData[0] + "' class='contest_entry' href='" + basePath + "/contest/view.action?cid=" + oObj.aData[0] + "#overview'>" + oObj.aData[1] + "</a></div>";
						},
						"sClass": "title"
					},
					{
						"fnRender": function ( oObj ) {
							return new Date(parseInt(oObj.aData[2])).format("yyyy-MM-dd hh:mm:ss");
						},
						"sClass": "date"
					},
					{ 
						"bSortable": false,
						"sClass": "time"
					},
					{ 
						"bSortable": false,
						"sClass": "center type"
					},
					{ 
						"fnRender": function ( oObj ) {
							return "<a href='" + basePath + "/user/profile.action?uid=" + oObj.aData[6] + "'>" + oObj.aData[5] + "</a>";
						},
						"sClass": "manager center"
					},
					{ 
						"fnRender": function ( oObj ) {
							if (oObj.aData[7] == 1) {
								return "<a href='" + basePath + "/contest/toEditContest.action?cid=" + oObj.aData[0] + "'><img height='15px' border='0' src='" + basePath + "/images/wrench.gif' /></a>&nbsp;<a href='javascript:void(0)' onclick='comfirmDeleteContest(\"" + oObj.aData[0] + "\")'><img height='15px' border='0' src='" + basePath + "/images/recycle.gif' /></a>";
							} else return "";
						},
						"bUseRendered": false, 
						"bSearchable": false,
						"bSortable": false,
						"sClass": "id icon"
					},
					{
						"bVisible": false
					},
					{
						"bVisible": false
					}
				],
		"fnServerData": function ( sSource, aoData, fnCallback ) {
			var s = $("[name='scheduled']").prop("checked");
			var r = $("[name='running']").prop("checked");
			var e = $("[name='ended']").prop("checked");
			var contestType = $("[name='contestType']:checked").val();
		
			aoData.push( { "name": "s", "value": s } );
			aoData.push( { "name": "r", "value": r } );
			aoData.push( { "name": "e", "value": e } );
			aoData.push( { "name": "contestType", "value": contestType } );
			$.ajax({
				"dataType": 'json', 
				"type": "POST", 
				"url": sSource, 
				"data": aoData, 
				"success": fnCallback
			});
		},
		"fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$('td:eq(4)', nRow).addClass(aData[4]);
			nRow.className += " " + aData[8];
			return nRow;
		},
		"bJQueryUI": true,
		"sPaginationType": "full_numbers"
	});
	
	$("#head_status").insertBefore("div#listContest_processing").show();
	$("div.dataTables_filter").css("width", "250px");
	
	$("#add_contest, #statistic_contest").button();

	$("input[type='checkbox']").change(function() {
		$.cookie("checked_" + $(this).attr("name"), $(this).prop("checked"), {expires:7});
		oTable.fnDraw();
	});
	
	$("input[name='contestType']").change(function(){
		$.cookie("contestType", $(this).val(), {expires:7});
		oTable.fnDraw();
	});
	
	$( "#dialog-form-contest-login" ).dialog({
		autoOpen: false,
		height: 200,
		width: 350,
		position: ['top', 50],
		modal: true,
		buttons: {
			"Login": function() {
				var cid = $("#cid").val(), info = {password: $("#contest_password").val(), cid: cid};
				$.post(basePath + '/contest/loginContest.action', info, function(data) {
					if (data == "success") {
						$( this ).dialog( "close" );
						if (location.hash) {
							location.hash = "";
						}
						window.location.href = basePath + "/contest/view.action?cid=" + cid + "#overview";
					} else {
						updateTips(data);						
					}
				});
			},
			"Cancel": function() {
				$( this ).dialog( "close" );
			}
		},
		close: function() {
			$("p.validateTips").html("");
			$( this ).find(":input").val("");
			if (location.hash) {
				location.hash = "";
			}
		}
	}).keyup(function(e){
		if (e.keyCode == 13) {
			$(this).dialog('option', 'buttons')['Login']();
		}
	});

	$("a.contest_entry").live('click', function(){
		attemptLoginContest($(this).attr("cid"));
		return false;
	});
	
	var para = parseUrlParameter();
	var query = para['q'];
	if (query) {
		setTimeout(function(){
			oTable.fnFilter(query);
		}, 300);
	}
	if (location.hash.match(/#\d+$/)) {
		attemptLoginContest(location.hash.substring(1));
	}
	
	
	function comfirmDeleteContest(cid){
		if (confirm("Sure to delete this contest?")){
			location = basePath + '/contest/deleteContest.action?cid=' + cid;
		}
	}

	function attemptLoginContest(cid) {
		$("#cid").val(cid);
		$.post(basePath + "/contest/checkAuthorizeStatus.action?cid=" + cid, function(authorizeStatus){
			if (authorizeStatus == "success") {
				if (location.hash) {
					location.hash = "";
				}
				window.location.href = basePath + "/contest/view.action?cid=" + cid + "#overview";
			} else {
				$("#dialog-form-contest-login").dialog('open');
			}
		});
	}	
	
});
