$(function() {
	if ($("#js_require_submit").length == 0) {
		return;
	}

	
	var oj = $("input[name=oj]").val();
	
	if ($.cookie("lang_" + oj)) {
		$("select#language").val($.cookie("lang_" + oj));
	}

	$("select#language").change(function(){
		$.cookie("lang_" + oj, $(this).val(), {expires:30, path:'/'});
	});
	
	$("#form").submit(function(){
		console.log($("textarea[name='tmp_source']").val());
		$("#submit").attr("disabled", true);
		$("textarea[name='tmp_source']").attr("disabled", true);
		$("input[name='source']").val(Base64.encode($("textarea[name='tmp_source']").val()));
		return true;
	});
});
