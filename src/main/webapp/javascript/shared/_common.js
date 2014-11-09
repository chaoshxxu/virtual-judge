$(function(){

	Vjudge.init();
    
    /////////////////////////////////////////////////////////////////////////////////
    
    $("a.login").click(function(){
        var url = this.href;
        Vjudge.doIfLoggedIn(function(){
            if (url.indexOf("void(0)") >= 0) {
                location.reload();
            } else {
                location.href = url;
            }
        });
        return false;
    });

    $("a.register").click(function(){
        $( "#dialog-form-register" ).dialog( "open" );
        return false;
    });

    $("#logout").click(function(){
        $.post(basePath + "/user/logout.action", function(res){
            window.location.reload();
        });
        return false;
    });
    
    if (location.href.indexOf("/contest/") >= 0) {
        $("#nav_contest").addClass("active");
    } else if (location.href.indexOf("/status") >= 0) {
        $("#nav_status").addClass("active");
    } else if (location.href.indexOf("/problem") >= 0) {
        $("#nav_problem").addClass("active");
    } else if (location.href.indexOf("/toIndex") >= 0) {
        $("#nav_home").addClass("active");
    }
    
});

Vjudge = new function() {
    var timeDiff;
    var nextFunc;    // to do after logging in
    var loginDialog;
    var registerDialog;
    
    //////////////////////////////////////////////////////////////

    /**
     * Call it on document ready
     */
    this.init = function() {
    	Vjudge.enhance();
        Vjudge.initDialogs();
        Vjudge.renderCurrentTime();
    };

    //////////////////////////////////////////////////////////////
    
    this.enhance = function() {
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
        };
        
        $.widget( "custom.selectmenu", $.ui.selectmenu, {
            _renderItem: function( ul, item ) {
                var li = $( "<li>", { text: item.label } );
                if ( item.disabled ) {
                    li.addClass( "ui-state-disabled" );
                }
                $( "<span>", {
                    style: item.element.attr( "data-style" ),
                    "class": "ui-icon " + item.element.attr( "data-class" )
                })
                .appendTo( li );
                return li.appendTo( ul );
            }
        });    	
    };

    this.getServerTime = function() {
        if (timeDiff === undefined) {
            timeDiff = $("#serverTime").val() - new Date().valueOf();
        }
        return new Date(new Date().valueOf() + timeDiff);
    };

    this.renderCurrentTime = function() {
        var update = function() {
            $(".currentTime").html(Vjudge.getServerTime().format("yyyy-MM-dd hh:mm:ss"));
        };
        update();
        setInterval(update, 1000);
    };

    /**
     * @param {function} getHoverNode.
     * @param {function} getWidthNode.
     */
    this.renderLocalizedTime = function(getHoverNode, getWidthNode) {
        var isMouseIn = false;
        $(".localizedTime").each(function(){
            var $this = $(this);
            var mills = $this.text();
            var absolute = new Date(parseInt(mills)).format("yyyy-MM-dd hh:mm:ss")
            var renderScheduler;
            var $widthNode = getWidthNode ? getWidthNode.call($this) : $();
            
            var getRelativeSeconds = function() {
                return (Vjudge.getServerTime().valueOf() - mills) / 1000;
            }
            var getRelative = function() {
                var relativeSeconds = getRelativeSeconds();
                var relativeSecondsAbs = Math.abs(relativeSeconds);
                var prep = relativeSeconds > 0 ? "ago" : "later";
                
                return relativeSecondsAbs < 60 ? Math.round(relativeSecondsAbs) + " sec " + prep :
                    relativeSecondsAbs < 3600 ? Math.round(relativeSecondsAbs / 60) + " min " + prep :
                        relativeSecondsAbs < 86400 ? Math.round(relativeSecondsAbs / 3600) + " hr " + prep :
                            relativeSecondsAbs < 2592000 ? Math.round(relativeSecondsAbs / 86400) + " days " + prep :
                                relativeSecondsAbs < 31536000 ? Math.round(relativeSecondsAbs / 2592000) + " months " + prep :
                                        Math.round(relativeSecondsAbs / 31536000) + " years " + prep;
            };
            var getDefaultDisplay = function() {
                return getRelative();
            }
            var getHoverDisplay = function() {
                return absolute;
            }
            var updateTime = function() {
                $this.text(isMouseIn ? getHoverDisplay() : getDefaultDisplay());
                if (Math.abs(getRelativeSeconds()) < 61) {
                    clearTimeout(renderScheduler);
                    renderScheduler = setTimeout(updateTime, 1000);
                }
            }

            updateTime();
            getHoverNode.call($this).mouseenter(function(){
                isMouseIn = true;
                $(".localizedTime").trigger("updateTime");
                $widthNode.trigger("updateTime");
            }).mouseleave(function(){
                isMouseIn = false;
                $(".localizedTime").trigger("updateTime");
                $widthNode.trigger("updateTime");
            });
            $this.bind("updateTime", updateTime);
            
            $widthNode.unbind("updateTime");
            $widthNode.bind("updateTime", function(){
                if (isMouseIn) {
                    $(this).animate({width: "160px"}, {
                        queue: false,
                        duration: 200
                    });
                } else {
                    $(this).animate({width: "110px"}, {
                        queue: false,
                        duration: 200
                    });
                }
            });
        });
    };

    this.parseUrlParameter = function() {
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
    };
    
    this.doIfLoggedIn = function(func) {
        $.post(basePath + "/user/checkLogInStatus.action", function(logInStatus){
            if (logInStatus == "true") {
                func();
            } else {
                nextFunc = func;
                $("#dialog-form-login").dialog('open');
            }
        });
    };
    
    this.initDialogs = function() {
        var updateTips = function(t) {
            var tips = $( "p.validateTips" );
            tips.text( t ).addClass( "ui-state-highlight" );
            setTimeout(function() {
                tips.removeClass( "ui-state-highlight", 1500 );
            }, 500 );
        };

        loginDialog = $( "#dialog-form-login" ).dialog({
            autoOpen: false,
            height: 300,
            width: 400,
            position: { my: "center", at: "center center-150px", of: window },
            modal: true,
            buttons: {
                "Login": function() {
                    var info = {username: $("#username").val(), password: $("#password").val()};
                    $("#login_form").submit();
                    $.post(basePath + '/user/login.action', info, function(data) {
                        if (data == "success") {
                            loginDialog.dialog( "close" );
                            nextFunc();
                        } else {
                            updateTips(data);                        
                        }
                    });
                },
                "Cancel": function() {
                    loginDialog.dialog( "close" );
                }
            },
            close: function() {
                $("p.validateTips").html("");
                loginDialog.find("input").val("");
            }
        }).keyup(function(e){
            if (e.keyCode == 13) {
                loginDialog.dialog('option', 'buttons')['Login']();
            }
        });

        registerDialog = $( "#dialog-form-register" ).dialog({
            autoOpen: false,
            height: 650,
            width: 550,
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
                    $.post(basePath + '/user/register.action', info, function(data) {
                        if (data == "success") {
                            registerDialog.dialog( "close" );
                            window.location.reload();
                        } else {
                            updateTips(data);                        
                        }
                    });
                },
                "Cancel": function() {
                    registerDialog.dialog( "close" );
                }
            },
            close: function() {
                $("p.validateTips").html("");
                registerDialog.find(":input").val("");
                registerDialog.find("textarea").val("");
            },
            create: function( event, ui ) {
            }
        });        
    };
    
    this.isScrolledIntoView = function(elem) {
        var docViewTop = $(window).scrollTop();
        var docViewBottom = docViewTop + $(window).height();

        var elemOffset = $(elem).offset();
        if (elemOffset == undefined) {
            return false;
        }
        var elemTop = elemOffset.top;
        var elemBottom = elemTop + $(elem).height();

        return ((elemBottom >= docViewTop) && (elemTop <= docViewBottom));
    };

    this.sendGaPageview = function() {
        var _page = window.location.pathname + window.location.hash;
//        console.log(_page);
        if (typeof ga == 'function') {
            ga('send', 'pageview', _page);
        }
    };

    this.storage = new function(){
        var cache = {};
		this.set = function(key, value, temp) {
			try {
			    (temp ? cache : localStorage)[key] = (typeof(value) == 'object') ? JSON.stringify(value) : value;
			} catch (e) {
				console.error(e);
			}
		};
		this.get = function(key, defaultValue, temp) {
		    var value = (temp ? cache : localStorage)[key];
			if (!value) {
				return defaultValue;
			}
			try {
				return JSON.parse(value);
			} catch (e) {
				console.error(e);
				return value;
			}
		};
	};
    
};
