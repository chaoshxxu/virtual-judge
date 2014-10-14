$(function() {
	if ($("#js_require_viewSource").length == 0) {
		return;
	}
	
	var showInfo = function() {
		if ($("input[name=open]:checked").val() == 1){
			$("p#info").css("visibility", "visible");
		} else {
			$("p#info").css("visibility", "hidden");
		}
	};
	showInfo();
	$("input[name=open]").change(function(){
		$.post(basePath + "/problem/toggleOpen.action?id=" + $("[name=sid]").val(), showInfo);
	});
	
	sh_highlightDocument(basePath + '/shjsx/', '.min.js');
	_showDiscuss();
});
