$(document).ready(function() {
	$("#OJId").val($.cookie("OJId") || "All");

	var oTable = $('#listProblem').dataTable({
		"bProcessing": true,
		"bServerSide": true,
		"sAjaxSource": "problem/listProblem.action",
		"iDisplayLength": 25,
		"sDom": '<"H"pfr>t<"F"il>',
		"bAutoWidth": false,
		"bStateSave": true,
		"oLanguage": {
			"sInfo": "_START_ to _END_ of _TOTAL_ problems",
			"sInfoEmpty": "No problems",
			"sInfoFiltered": " (filtering from _MAX_ total problems)"
		},
		"aaSorting": [[ 1, "asc" ]],
		"aoColumns": [
					{
						"sClass": "center oj",
						"bSortable": false
					},
					{
						"fnRender": function ( oObj ) {
							return "<a href='" + oObj.aData[6] + "'>" + oObj.aData[1] + "</a>";
						},
						"sClass": "center prob_num"
					},
					{
						"fnRender": function ( oObj ) {
							return "<a href='problem/viewProblem.action?id=" + oObj.aData[5] + "'>" + oObj.aData[2] + "</a>";
						},
						"sClass": ""
					},
					{
						"fnRender": function ( oObj ) {
							return new Date(parseInt(oObj.aData[3])).format("yyyy-MM-dd hh:mm:ss");
						},
						"sClass": "date",
						"bSortable": false
					},
					{
						"sClass": "source"
					},
					{"bVisible": false},
					{"bVisible": false},
					{"bVisible": false}
			],
		"fnServerData": function ( sSource, aoData, fnCallback ) {
			var OJId = $("#OJId").val();
			aoData.push( { "name": "OJId", "value": OJId } );
			$.ajax( {
				"dataType": 'json', 
				"type": "POST", 
				"url": sSource, 
				"data": aoData, 
				"success": fnCallback
			});
		},
		"fnRowCallback": function( nRow, aData ) {
			if (aData[7] == 1 || aData[7] == 2){
				nRow.className += " Running"; 
			}
			return nRow;
		},
		"bJQueryUI": true,
		"sPaginationType": "full_numbers"
	});
	
	$("#addBtn").css("visibility", $("#OJId").val() == 'All' ? "hidden" : "visible");
	
	$("#OJId").change(function(){
		$.cookie("OJId", $(this).val(), {expires:7});
		$("#addBtn").css("visibility", $("#OJId").val() == 'All' ? "hidden" : "visible");
		oTable.fnDraw();
	});
	
	$("#addProblem").submit(function(){
		if ($("select[name='OJId']").val() == 'All') {
			return false;
		}
	});
	
});
