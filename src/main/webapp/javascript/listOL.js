$(document).ready(function() {
	
	jQuery.fn.dataTableExt.oSort['custom-time-asc']  = function(x,y) {
		var a = x.split(/\D+/), b = y.split(/\D+/), v1, v2;
		if (a.length != b.length) return a.length < b.length ? -1 : 1;
		for (var i = 0; i < a.length && i < b.length; i++){
			v1 = parseInt(a[i], 10);
			v2 = parseInt(b[i], 10);
			if (v1 != v2){
				return v1 < v2 ? -1 : 1;
			}
		}
		return 0;
	};
	
	jQuery.fn.dataTableExt.oSort['custom-time-desc'] = function(x,y) {
		var a = x.split(/\D+/), b = y.split(/\D+/), v1, v2;
		if (a.length != b.length) return a.length > b.length ? -1 : 1;
		for (var i = 0; i < a.length && i < b.length; i++){
			v1 = parseInt(a[i], 10);
			v2 = parseInt(b[i], 10);
			if (v1 != v2){
				return v1 > v2 ? -1 : 1;
			}
		}
		return 0;
	};
	
	$('#listOL').dataTable({
		"aaSorting": [[ 6, "asc" ]],
		"bPaginate": false,
		"bLengthChange": false,
		"bFilter": true,
		"bSort": true,
		"bInfo": false,
		"bAutoWidth": false,
		"bStateSave": true,
		"aoColumns": [{"sType": "html"},
		              {"sType": "html"},
		              {"sType": "html"},
		              {"sType": "html"},
		              {"sType": "custom-time"},
		              {"sType": "custom-time"},
		              {"sType": "custom-time"},
		              {"sType": "string"},
		              {"sType": "string"},
		              {"sType": "string"}
					]
	});
});
