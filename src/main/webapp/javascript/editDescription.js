$(document).ready(function() {
	var form = document.getElementById("editorsForm");
	$("#remarks").focus();

	$("#editorsForm").submit(function(){
		if ($("[name='description.remarks']").val().length > 450){
			$( "#tabs" ).tabs("select", 0);
			$("#remarks").focus();
			alert("Remarks should be shorter than 450 characters !");
			return false;
		}
	});
	
	$( "#tabs" ).tabs({
		cookie: { expires: 30 }
	});
	$( "#tabs" ).removeClass("ui-widget-content");
	$( "#tabs" ).addClass("ui-widget-content-custom");


	CKEDITOR.replace( 'description',
		{
			sharedSpaces :
			{
				top : 'topSpace',
				bottom : 'bottomSpace'
			}
		} );
	
	CKEDITOR.replace( 'input',
			{
				sharedSpaces :
				{
					top : 'topSpace',
					bottom : 'bottomSpace'
				},
				removePlugins : 'maximize,resize'
			} );
	
	CKEDITOR.replace( 'output',
			{
				sharedSpaces :
				{
					top : 'topSpace',
					bottom : 'bottomSpace'
				},
				removePlugins : 'maximize,resize'
			} );
	CKEDITOR.replace( 'sampleInput',
			{
				sharedSpaces :
				{
					top : 'topSpace',
					bottom : 'bottomSpace'
				},
				removePlugins : 'maximize,resize'
			} );
	
	CKEDITOR.replace( 'sampleOutput',
			{
				sharedSpaces :
				{
					top : 'topSpace',
					bottom : 'bottomSpace'
				},
				removePlugins : 'maximize,resize'
			} );
			
	CKEDITOR.replace( 'hint',
			{
				sharedSpaces :
				{
					top : 'topSpace',
					bottom : 'bottomSpace'
				},
				removePlugins : 'maximize,resize'
			} );

});
