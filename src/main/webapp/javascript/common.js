$(function(){
	$( "#dialog-form-login" ).dialog({
		autoOpen: false,
		height: 270,
		width: 350,
		position: ['top', 50],
		modal: true,
		buttons: {
			"Login": function() {
				var info = {username: $("#username").val(), password: $("#password").val()};
				$("#login_form").submit();
				$.post('user/login.action', info, function(data) {
					if (data == "success") {
						$( this ).dialog( "close" );
						if (nextUrl == "javascript:void(0)") {
							window.location.reload();
						} else {
							window.location.href = nextUrl;
						}
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
			$( this ).find("input").val("");
		}
	}).keyup(function(e){
		if (e.keyCode == 13) {
			$(this).dialog('option', 'buttons')['Login']();
		}
	});
	
	$( "#dialog-form-register" ).dialog({
		autoOpen: false,
		height: 570,
		width: 500,
		position: ['top', 50],
		modal: true,
		buttons: {
			"Register": function() {
				var info = {
					username: $("#username1").val(),
					password: $("#password1").val(),
					repassword: $("#repassword").val(),
					nickname: $("#nickname").val(),
					school: $("#school").val(),
					qq: $("#qq").val(),
					email: $("#email").val(),
					blog: $("#blog").val(),
					share: $("#share").val()
				};
				$.post('user/register.action', info, function(data) {
					if (data == "success") {
						$( this ).dialog( "close" );
						window.location.reload();
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
			$( this ).find("textarea").val("");
		}
	});

	$("a.login").click(function(){
		var url = this.href;
		doIfLoggedIn(function(){
			location.href = url;
		}, url);
		return false;
	});

	$("a.register").click(function(){
		$( "#dialog-form-register" ).dialog( "open" );
		return false;
	});

	$("#logout").click(function(){
		$.post("user/logout.action", function(res){
			window.location.reload();
		});
		return false;
	});

});

function parseUrlParameter() {
	var params = new Object();
	var paramString = window.location.href.replace(/#.+$/, "");
	var startpos = paramString.indexOf("?");
	var pieces = paramString.substring(startpos + 1).split("&");
	for(var i = 0; i < pieces.length; i++) {
		try {
			var keyvalue = pieces[i].split("=");
			params[keyvalue[0]] = keyvalue[1];
		} catch(e){}
	}
	return params;
}

Date.prototype.format = function(format){
	var o = {
		"M+": this.getMonth() + 1, //month
		"d+": this.getDate(), //day
		"h+": this.getHours(), //hour
		"m+": this.getMinutes(), //minute
		"s+": this.getSeconds(), //second
		"q+": Math.floor((this.getMonth() + 3) / 3), //quarter
		"S": this.getMilliseconds() //millisecond
	}
	if (/(y+)/.test(format))
		format = format.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
	for (var k in o)
		if (new RegExp("(" + k + ")").test(format))
			format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k] : ("00" + o[k]).substr(("" + o[k]).length));
	return format;
}

var nextUrl;	//to go when just logged in
function doIfLoggedIn(func, url) {
	$.post("user/checkLogInStatus.action", function(logInStatus){
		if (logInStatus == "true") {
			func();
		} else {
			nextUrl = !url ? "javascript:void(0)" : url;
			$("#dialog-form-login").dialog('open');
		}
	});
}

function updateTips ( t ) {
	var tips = $( "p.validateTips" );
	tips.text( t ).addClass( "ui-state-highlight" );
	setTimeout(function() {
		tips.removeClass( "ui-state-highlight", 1500 );
	}, 500 );
}
