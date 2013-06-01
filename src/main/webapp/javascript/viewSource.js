$(document).ready(function() {
	
	var showInfo = function() {
		if ($("input[name=open]:checked").val() == 1){
			$("p#info").css("visibility", "visible");
		} else {
			$("p#info").css("visibility", "hidden");
		}
	};
	showInfo();
	$("input[name=open]").change(function(){
		$.post("problem/toggleOpen.action?id=" + $("[name=sid]").val(), showInfo);
	});
	
	sh_highlightDocument('shjsx/', '.min.js');
	_showDiscuss();
});
