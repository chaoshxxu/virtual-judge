$(function () {
	if ($("#js_require_view_contest").length == 0) {
		return;
	}


    "use strict";

    var cid,    //current contest
        cids,    //cid concerned in rank
        startTime,    //page loaded time locally
        selectedTime,    //slider
        ti,    //Time Info
        tabs,
        slider,
        problemSet = {},
        statusTable,
        hash,
        oldProblemHash = "#problem/A",
        rankTable,
        ranks = {},
        lastRankUpdateTime = -99999999,
        sliderUpdater,
        statusTimeoutInstance = {},    //status fetch
        oFH,
        exportRankHtml,
        timeoutHandle,
        lastClickTime,
        blankChar = decodeURIComponent("%E3%80%80");
    
    ///////////////////// miscellaneous ///////////////////////

    cid = $("#cid").val();

    DWREngine.setAsync(false);
    judgeService.getContestTimeInfo(cid, function (res) {
        startTime = new Date().valueOf();
        ti = res;
        $("#time_total span").text(dateFormat(ti[0]));
    });
    DWREngine.setAsync(true);

    /////////////////////   Slider    //////////////////////

    var curTime, exceedMax;
    slider = $("#time_controller").slider({
        range: "min",
        min: 0,
        max: ti[0],
        value: 0,
        start: function () {
            clearInterval(sliderUpdater);
            curTime = new Date().valueOf();
            sliderUpdater = 0;
        },
        slide: function ( event, ui ) {
            selectedTime = parseInt(ui.value);
            displayTime();
            if (ui.value > ti[1] + curTime - startTime) {
                exceedMax = true;
                return false;
            }
        },
        stop: function ( event, ui ) {
            if (ui.value > ti[1] + curTime - startTime) {
                exceedMax = true;
            }
            if (exceedMax) {
                resetTimeSlider();
                exceedMax = false;
                if (location.hash.indexOf("#rank") == 0) {
                    location.hash = "#rank";
                    $.scrollTo( {top: '0px',left:'0px'}, 0 );
                }
            } else {
                selectedTime = parseInt(ui.value);
                displayTime();
                if (location.hash.indexOf("#rank") == 0) {
                    location.hash = "#rank/" + selectedTime;
                }
                updateRankInfo();
            }
        }
    });

    /////////////////////   Tabs    //////////////////////

    tabs = $("#contest_tabs").tabs({
        activate: function (event, ui) {
            var rel = ui.newTab.find("a").attr("rel");
//            alert(ui.newTab.find("a").attr("href"));
            lastClickTime = new Date().valueOf();

            if (location.hash.indexOf(rel) != 0) {
                if (rel == "#problem") {
                    location.hash = oldProblemHash;
                } else {
                    location.hash = rel;
                }
            }
            //deal with rank update
            if (location.hash.indexOf("#rank") == 0) {
                $("#contest_tabs").css("min-width", 400 + $("table#viewContest tr").length * 80 + "px");
            } else {
                $("#contest_tabs").css("min-width", 0);
            }

            $.scrollTo( {top: '0px',left:'0px'}, 0 );
        }
    });

    /////////////////////   Overview    //////////////////////
    $('#viewContest').dataTable({
        "paging": false,
        "lengthChange": false,
        "searching": false,
        "ordering": false,
        "info": false,
        "autoWidth": false
    });

    $("span.plainDate").each(function () {
        $(this).html(new Date(parseInt($(this).html())).format("yyyy-MM-dd hh:mm:ss"));
    });

    $("#contest_opt a").button();

    $("#delete_contest").click(function(){
        if (confirm("Sure to delete this contest?")) {
            $.post(basePath + '/contest/deleteContest.action?cid=' + cid, function(data) {
                location.reload();
            });
        }
    });

    $( "#dialog-form-contest-login" ).dialog({
        autoOpen: true,
        height: 250,
        width: 350,
        position: { my: "center", at: "center", of: window },
        modal: true,
        buttons: {
            "Login": function() {
                var cid = $("#cid").val(), info = {password: $("#contest_password").val(), cid: cid};
                $.post(basePath + '/contest/loginContest.action', info, function(data) {
                    if (data == "success") {
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
            window.location = basePath + "/contest/toListContest.action";
        }
    }).keyup(function(e){
        if (e.keyCode == 13) {
            $(this).dialog('option', 'buttons')['Login']();
        }
    });
    
    /////////////////////    Problem    //////////////////////

    $("#problem_opt").find("a").button();

    $(":radio[name=problem_number]").each(function () {
        $(this).next().html($(this).val());
    });

    $("input[name=problem_number]").change(function () {
        location.hash = "#problem/" + $(this).val();
    });

    $("#problem_number_container").buttonset().show();

    $("#desc_index").change(function () {
        var num = $(":input[name=problem_number]:checked").val();
        var did = $("#desc_index > option:eq(" + this.value + ")").attr("did");
        showDescription(num, this.value);
        $.post(basePath + "/contest/appointDescription.action?cid=" + cid + "&num=" + num + "&id=" + did);
    });

    $("#submit").click(function () {
        Vjudge.doIfLoggedIn(function () {
            var num = $(":input[name=problem_number]:checked").val();
            var problem = problemSet[num];
            if (!!problem) {
                $("#submit_num").html(num + " - " + problem.title);
                var languageSelect = $("select#language");
                languageSelect.html("");
                for (i in problem.languageList) {
                    languageSelect.append("<option value='" + i + "'>" + problem.languageList[i] + "</option>");
                }
                if (Vjudge.storage.get("lang_" + problem.oj, null)) {
                    $("select#language").val(Vjudge.storage.get("lang_" + problem.oj));
                }
                $("#dialog-form-submit").dialog("open");
            }
        });
        return false;
    });

    var updateTips = function(t) {
        var tips = $( "p.validateTips" );
        tips.text( t ).addClass( "ui-state-highlight" );
        setTimeout(function() {
            tips.removeClass( "ui-state-highlight", 1500 );
        }, 500 );
    };
    
    $("#dialog-form-submit").dialog({
        autoOpen: false,
        height: 650,
        width: 600,
        position: { my: "center", at: "center center-50px", of: window },
        modal: true,
        buttons: {
            "Submit": function () {
                updateTips("Submitting...");
                $("#dialog-form-submit").parent().find("button:first").hide();

                var num = $(":input[name=problem_number]:checked").val();
                var data = {
                    cid: cid,
                    num: num,
                    language: $("[name=language]").val(),
                    isOpen: $(":input[name=isOpen]:checked").val(),
                    source: Base64.encode($("[name=source]").val())
                };
                Vjudge.storage.set("lang_" + problemSet[num].oj, $("select#language").val());
                $.ajax({
                    type : "POST",
                    url : basePath + "/contest/submit.action",
                    data : data,
                    success : function (res) {
                        if (res == "success") {
                            $("#dialog-form-submit").dialog( "close" );
                            showStatus();
                            $("#reset").trigger("click");
                        } else {
                            updateTips(res);
                            $("#dialog-form-submit").parent().find("button:first").show();
                        }
                    },
                    error : function () {
                        updateTips("Connection timeout.");
                        $("#dialog-form-submit").parent().find("button:first").show();
                    }
                });
            },
            "Cancel": function () {
                $(this).dialog( "close" );
            }
        },
        close: function () {
            $("#dialog-form-submit").parent().find("button:first").show();
            $("p.validateTips").html("");
            $(this).find("textarea").val("");
        }
    });

    /////////////////////    Status     //////////////////////

    $("#num").prepend("<option value='-'>All</option>");

    $("#num").selectmenu({
        change : function(){
            updateHash("num");
        }
    }).selectmenu( "menuWidget").addClass( "ui-menu-icons avatar" );

    $("#res").selectmenu({
        change : function(){
            updateHash("res");
        }
    }).selectmenu( "menuWidget").addClass( "ui-menu-icons avatar" );

    $("#lang").selectmenu({
        change : function(){
            updateHash("lang");
        }
    }).selectmenu( "menuWidget").addClass( "ui-menu-icons avatar" );

    $(["#num", "#res", "#lang"]).each(function(_, id){
        var buttonId = id + "-button";
        var menuId = id + "-menu";
        $(buttonId).mouseenter(function(){
            $(id).selectmenu('open');
        }).mouseleave(function(){
            setTimeout(function(){
                if (!$(menuId).is(":hover") && !$(buttonId).is(":hover")) {
                    $(id).selectmenu('close');
                }
            }, 100);
        });
        $(menuId).mouseleave(function(){
            $(id).selectmenu('close');
        });
    });
    
    $(".search_text").click(function(event){
        event.stopPropagation();
    }).keypress(function(event){
        event.stopPropagation();
    }).keyup(function(event){
//      if (event.which == 13) {
//          updateHash($(this).attr("name"));
//      }
    }).change(function(){
        updateHash($(this).attr("name"));
    });


//    $("#form_status").submit(function () {
//        var oldHash = location.hash;
//        $("[name='un']").val($("[name='un']").val().replace(/%\d\d|\s+/g, ''));
//        location.hash = "#status/" + $("[name='un']").val() + "/" + $("[name='num']").val() + "/" + $("[name='res']").val();
//        if (location.hash == oldHash) {
//            statusTable.draw();
//        }
//        return false;
//    });

//    $("#reset").click(function () {
//        var oldHash = location.hash;
//        location.hash = "#status//-/0";
//        if (location.hash == oldHash) {
//            statusTable.fnPageChange( 'first' );
//        }
//        return false;
//    });

    $(document).on("click", ".rejudge", function () {
        var $this = $(this);
        var $row = $this.is("td") ? $this.parent() : $this;
        var id = $row.attr("id");
        $row.removeClass("no");
        $row.removeClass("yes");
        $row.addClass("working");
        $row.removeClass("rejudge");
        $.post(basePath + "/problem/rejudge.action", {id: id}, function () {
            clearTimeout(timeoutHandle);
            timeoutHandle = setTimeout(getResult, 1000);
        });
    });

    $(document).on("click", ".rejudge a", function (event) {
        event.stopPropagation();
    });

    /////////////////////     Rank      //////////////////////

    $("#rank").css("width", 400 + $("table#viewContest tr").length * 80 + "px");

    $(document).on("mouseenter", "div.meta_td", function () {
        var curCid = $(this).parent().attr("cid");
        $("div[cid=" + curCid + "] div.meta_td").addClass("same_td");
    }).on("mouseleave", "div.meta_td", function () {
        var curCid = $(this).parent().attr("cid");
        $("div[cid=" + curCid + "] div.meta_td").removeClass("same_td");
    });

    $(document).on("click", "div.meta_td.rank, div.meta_td.id", function () {
        var curCid = $(this).parent().attr("cid");
        var rank = ranks[curCid];
        var end = new Date().valueOf() > rank.endTime;
        $.facebox("<table><tr><td style='font-weight:bold;padding:5px'>Title</td><td>" + (end && rank.cid != cid ? "<a href='" + basePath + "/contest/view.action?cid=" + rank.cid + "#overview' target='_blank'>" : "") + (rank.isReplay ? "<img src='" + basePath + "/images/replay.png' height='18' /> " : "") + rank.title + (end && rank.cid != cid ? "</a>" : "") + "</td></tr><tr><td style='font-weight:bold;padding:5px'>Begin Time</td><td>" + new Date(rank.beginTime).format("yyyy-MM-dd hh:mm:ss") + "</td></tr><tr><td style='font-weight:bold;padding:5px'>Length</td><td>" + dateFormat(rank.length) + "</td></tr><tr><td style='font-weight:bold;padding:5px'>Manager</td><td><a href='" + basePath + "/user/profile.action?uid=" + rank.managerId + "' target='_blank'>" + rank.managerName + "</a></td></tr></table>");
        $("#facebox").css({
            "z-index": "1000000",
            "position": "fixed",
            "top": "200px",
            "left": $(window).width() / 2 - 250 + "px"
        });
        $("#facebox .content").css("width", "500px");
    });

    $(document).on("click", "div.meta_td a", function (event) {
        event.stopPropagation();
    });

    $(document).on("mouseenter", "div.penalty_td", function () {
        $(this).text($(this).attr("v0"));
    }).on("mouseleave", "div.penalty_td", function () {
        $(this).text($(this).attr("v1"));
    });

    $(document).on("click", "div.standing_time, div.solve", function () {
        var $this = $(this);
        if (
                !$this.hasClass("red") &&
                !$this.hasClass("green") &&
                !$this.hasClass("solvedfirst") &&
                !$this.hasClass("penalty_td") &&
                !$this.hasClass("solve")) {
            return;
        }
        var $parent = $this.parent();
        var _cid = $parent.attr("cid");
        if (_cid == cid) {
            var pid = parseInt($this.attr("pid"));
            var username = $parent.attr("u");
            location.hash = "#status/" + username + "/" + (isNaN(pid) ? "-" : String.fromCharCode(65 + pid)) + "/0";
        }
    });

    $("#dialog-form-rank-setting").dialog({
        autoOpen: false,
        width: 950,
        position: { my: "top", at: "center top", of: window },
        modal: false,
        buttons: {
            "Save": function () {
                var ids = [cid];
                $("[name=ids]:checked").each(function () {
                    ids.push($(this).val());
                });
                var showTeams = $("[name=showTeams]:checked").val();
                var showNick = $("[name=showNick]:checked").length > 0;
                var showUsername = $("[name=showUsername]:checked").length > 0;
                var showAnimation = $("[name=showAnimation]:checked").val();

                Vjudge.storage.set("contest_" + cid, ids);
                Vjudge.storage.set("show_all_teams", showTeams);
                Vjudge.storage.set("show_nick", showNick);
                Vjudge.storage.set("show_username", showUsername);
                Vjudge.storage.set("show_animation", showAnimation);
                $(this).dialog( "close" );
                lastRankUpdateTime = -99999999;
                updateRankInfo();
            },
            "Cancel": function () {
                $(this).dialog( "close" );
            }
        }
    });    

    $("#rank_setting").click(function () {
        var $inst = $("#dialog-form-rank-setting")
        $inst.dialog('open');
        if (!$inst.html()) {
            $inst.load(basePath + "/contest/showRankSetting.action?cid=" + cid);
        }
    });

    $(window).scroll(adjustRankTool);

    $("#img_find_me").click(function () {
        $.scrollTo( $("div.my_tr")[0], 800, {offset: {top:120-$(window).height(), left:-100} } );
        return false;
    });

    $("#img_go_top").click(function () {
        $.scrollTo( {top: '0px',left:'0px'}, 800 );
        return false;
    });

    $("#div_rank_tool > img").mouseover(function () {
        $(this).css("background", "#CCEEFF");
    }).mouseout(function () {
        $(this).css("background", "transparent");
    });

    //////////////////////////////////////////////////////////

    resetTimeSlider();
    updateRankInfo();

    $(window).hashchange( function () {
        var tabsLength = $("#contest_tabs > ul > li").length;
        hash = location.hash.split("/");
        if (hash[0] == "#problem" && tabsLength > 1) {
            showProblem();
        } else if (hash[0] == "#status" && tabsLength > 2) {
            showStatus();
        } else if (hash[0] == "#rank" && tabsLength > 3) {
            showRank();
        } else if (hash[0] == "#discuss" && tabsLength > 4) {
            showDiscuss();
        } else {
            showOverview();
        }
        Vjudge.sendGaPageview();
    }).hashchange();

    $("#contest_tabs").show();

    //////////////////////////////////////////////////////////////////////

    function showOverview() {
        tabs.tabs( "option", "active" , 0 );
    }

    function showProblem() {
        tabs.tabs( "option", "active" , 1 );

        oldProblemHash = location.hash;
        var $numRadio = $("#problem_number_container > input[value=" + hash[1] + "]");
        if ($numRadio.length) {
            $numRadio.prop("checked", "checked");
            $("#problem_number_container").buttonset("refresh");
        }
        var num = $("#problem_number_container input:checked").val();
        if (!problemSet[num]) {
            $.ajax({
                url: basePath + "/contest/showProblem.action?cid=" + cid + "&num=" + num,
                type: 'get',
                async: false,
                success: function (data) {
                    problemSet[num] = data;
                }
            });
        }
        var problem = problemSet[num];
        $("#problem_title").html("<span style='color:green'>" + num + " - </span>" + problem.title);
        $("span.crawlInfo").hide();
        if (problem.timeLimit == 1) {
            $("#crawling").show();
        } else if (problem.timeLimit == 2) {
            $("#crawlFailed").show();
        } else {
            $("#timeLimit").html(problem.timeLimit);
            $("#memoryLimit").html(problem.memoryLimit);
            $("#_64IOFormat").html(problem._64IOFormat);
            $("#crawlSuccess").show();
        }
        $("#problem_status").attr("href", basePath + "/contest/view.action?cid=" + cid + "#status//" + num + "/0");
        if (problem.pid) {
            $("#problem_practice").attr("href", basePath + "/problem/viewProblem.action?id=" + problem.pid);
        }
        if (problem.originProblemNumber) {
            $("#problem_origin").button("destroy");
            $("#problem_origin").attr("href", basePath + "/problem/visitOriginUrl.action?id=" + problem.pid).text(problem.originProblemNumber);
            $("#problem_origin").button();
        }

        $("#desc_index").html("");
        for (i in problem.descriptions) {
            if (problem.descriptions[i].author == '0') {
                problem.descriptions[i].author = "System Crawler";
            }
            $("#desc_index").append("<option did='" + problem.descriptions[i].id + "' value='" + i + "'>" + problem.descriptions[i].author + "  (" + problem.descriptions[i].updateTime + ")" + "</option>");
        }
        showDescription(num, problem.desc_index);
    }

    function showStatus() {
        tabs.tabs( "option", "active" , 2 );

        var queryParam = getQueryParam();
        $.extend(queryParam, {
            un: hash[1],
            num: hash[2],
            res: hash[3],
            lang: hash[4]
        });
        setQueryParam(queryParam);
        
        $("[name='un']").val(queryParam.un);
        $("[name='num']").val(queryParam.num).selectmenu("refresh");
        $("[name='res']").val(queryParam.res).selectmenu("refresh");
        $("[name='lang']").val(queryParam.lang).selectmenu("refresh");
        if (statusTable) {
            statusTable.draw();
        } else {
            statusTable = $('#table_status').DataTable({
                "autoWidth": false,
                "processing": true,
                "serverSide": true,
                "pageLength": 20,
                "lengthChange": false,
                "searching": false,
                "ordering": false,
                "info": false,
                "stateSave": true,
                "pagingType": "simple_numbers",
                "dom": '<"H"pr<"#buttonContainer">>t<"F">',
                "jQueryUI": true,
                "ajax": {
                    "url": basePath + "/contest/fetchStatus.action?cid=" + cid,
                    "type": "POST",
                    "data": function ( data ) {
                        $.extend(data, getQueryParam());
                    },
                    "dataSrc": function ( json ) {
                        return json.data;
                    }     
                },
                "columns": [
                    { 
                        "className" : "run_id",
                        "data": function ( row ) {
                            return row[0];
                        }
                    },
                    { 
                        "className" : "username",
                        "data": function ( row ) {
                            return "<a href='" + basePath + "/user/profile.action?uid=" + row[9] + "'>" + row[1] + "</a>";
                        }
                    },
                    { 
                        "className" : "prob_num",
                        "data": function ( row ) {
                            return "<a href='" + basePath + "/contest/view.action?cid=" + cid + "#problem/" + row[2] + "'>" + row[2] + "</a>";
                        }
                    },
                    {
                        "className" : "result",
                        "data": function ( row ) {
                            var info = row[3];
                            if (row[12]) {
                                info = "<a href='" + basePath + "/problem/fetchSubmissionInfo.action?id=" + row[0] + "' rel='facebox'>" + info + "</a>";
                            }
                            return "<div>" + info + "</div>";
                        }
                    },
                    { 
                        "className" : "memory",
                        "data": function ( row ) {
                            return row[3] == 'Accepted' ? row[4] : "";
                        }
                    },
                    {
                        "className" : "time",
                        "data": function ( row ) {
                            return row[3] == 'Accepted' ? row[5] : "";
                        }
                    },
                    { 
                        "className" : "language",
                        "data": function ( row ) {
                            return "<div>" + (row[10] ? "<a " + (row[10] == 2 ? "class='shared'" : "") + " target='_blank' href='" + basePath + "/contest/viewSource.action?id=" + row[0] + "'>" + row[6] + "</a>" : row[6]) + "</div>";
                        }
                    },
                    {
                        "className" : "length",
                        "data": function ( row ) {
                            return row[7];
                        }    
                    },
                    {
                        "className" : "date",
                        "data": function ( row ) {
                            return "<div class='localizedTime'>" + row[8] + "</div>";
                        }    
                    }
                ],
                "preDrawCallback": function( settings ) {
                    $("[name='un']").val($("[name='un']").val().replace(/%\d\d|\s+/g, ''));
                },


//                "fnServerData": function ( sSource, aoData, fnCallback ) {
//                    var un = $("[name='un']").val();
//                    var num = $("[name='num']").val();
//                    var res = $("[name='res']").val();
//
//                    aoData.push( { "name": "un", "value": un } );
//                    aoData.push( { "name": "num", "value": num } );
//                    aoData.push( { "name": "res", "value": res } );
//
//                    $.ajax( {
//                        "dataType": 'json', 
//                        "type": "POST", 
//                        "url": sSource, 
//                        "data": aoData, 
//                        "success": fnCallback
//                    } );
//                },
                "createdRow": function ( row, data, dataIndex ) {
                    var $row = $(row);

                    $row.addClass(data[13] == 0 ? "yes" : data[13] == 1 ? "no" : "pending");
                    if (data[14]) {
                        $row.addClass("working");
                    }
                    $row.attr("id", data[0]);
                    $('a[rel=facebox]', $row).facebox({
                        loadingImage : basePath + '/facebox/loading.gif',
                        closeImage   : basePath + '/facebox/closelabel.png'
                    });
                    if (data[13] == 2 && data[14] == 0) {
                        $row.addClass("rejudge");
                    } else if (isSup) {
                        $("td:eq(8)", $row).addClass("rejudge");
                    }
                    return $row;
                },

                "fnDrawCallback": function ( oSettings ) {
                    Vjudge.renderLocalizedTime(function() {
                        return this.parent();
                    }, function() {
                        return $("th.date");
                    });
                    getResult();
                },
                "initComplete": function(settings, json) {
                    $("#filter").button().appendTo("#buttonContainer").click(function(){
                        if (!updateHash()) {
                            statusTable.draw();
                        }
                    });
                    $("#reset").button().appendTo("#buttonContainer").click(function(){
                        $("[name='un']").val("");
                        $("[name='num']").val("-").selectmenu("refresh");
                        $("[name='res']").val("0").selectmenu("refresh");
                        $("[name='lang']").val("").selectmenu("refresh");
                        if (!updateHash()) {
                            statusTable.draw();
                        }
                    });
                }
            });
        }
    }

    /**
     * For status tab only
     * @return {boolean} whether hash changed
     */
    function updateHash(triggerVar) {
        var un = $("[name='un']").val();
        var num = $("[name='num']").val();
        var res = $("[name='res']").val();
        var lang = $("[name='lang']").val();

        var oldHash = location.hash;
        location.hash = "#status/" + un + "/" + num + "/" + res + "/" + lang;
        return oldHash != location.hash;
    }

    function getQueryParam() {
        var defaultQueryParam = {
            un : "",
            num : "-",
            res : "0",
            lang : ""
        };
        try {
            return Vjudge.storage.get("contest.status.queryParam", defaultQueryParam);
        } catch (e) {
            console.error(e);
            return defaultQueryParam;
        }
    }
    
    function setQueryParam(queryParam) {
        Vjudge.storage.set("contest.status.queryParam", queryParam);
    }
    
    function showRank() {
        tabs.tabs( "option", "active" , 3 );

        $("#contest_tabs").css("min-width", 400 + $("table#viewContest tr").length * 80 + "px");

        if (!isNaN(hash[1]) && hash[1] >= 0) {
            if (hash[1] <= Math.min(ti[1] + new Date().valueOf() - startTime, ti[0])) {
                selectedTime = parseInt(hash[1]);
                slider.slider("value", selectedTime);
                displayTime();
                clearInterval(sliderUpdater);
                sliderUpdater = 0;
            }
        }
    }

    function showDiscuss() {
        tabs.tabs( "option", "active" , 4 );

        if (!$("#disqus_thread").html()) {
            _showDiscuss();
        }
    }

    function displayTime() {
        var ratio = Math.max(selectedTime, 0) / ti[0];
        $("#time_index").css("width", (2 + 100 * ratio) + "%");
        if (ratio > 0.93) {
            $("#time_total span").text("");
            $("#time_index span").text(dateFormat(selectedTime == ti[0] ? ti[0] : selectedTime - ti[0]));
        } else {
            $("#time_total span").text(dateFormat(ti[0]));
            $("#time_index span").text(dateFormat(selectedTime));
        }
    };

    function updateRankInfo() {
        if (selectedTime < 0) {
            return;
        }

        var now = new Date().valueOf();
        var updateInterval = 
            now - lastClickTime > 300000 ? 99999999 :
            hash && hash[0] == "#rank" ? 10000 : 60000;

        var rankUpdateCountDown = ti[1] >= ti[0] ? 0 :
            Math.round(
                (lastRankUpdateTime - ti[1] + startTime + updateInterval - now) / 1000
            );
        var newRankTitle = "Rank";
        if (rankUpdateCountDown > 0) {
            newRankTitle += " (" + rankUpdateCountDown + ")";
        }
        $("div#contest_tabs > ul:first > li:eq(3) > a").text(newRankTitle);

        if (selectedTime > lastRankUpdateTime && selectedTime - lastRankUpdateTime < updateInterval) {
            return;
        }

//        if (Vjudge.storage.get("contest_" + cid, []) == []) {
//            Vjudge.storage.set("contest_" + cid, [cid]);
//        }
//        if (Vjudge.storage.get("show_all_teams") == undefined) {
//            $.cookie("show_all_teams", 0, { expires: 30 });
//        }
//        if ($.cookie("show_nick") == undefined) {
//            $.cookie("show_nick", true, { expires: 30 });
//        }
//        if ($.cookie("show_username") == undefined) {
//            $.cookie("show_username", true, { expires: 30 });
//        }
//        if ($.cookie("show_animation") == undefined) {
//            $.cookie("show_animation", 1, { expires: 30 });
//        }

        cids = Vjudge.storage.get("contest_" + cid, [cid]);

        var cnt = 0;
        for (var i = 0; i < cids.length; i++) {
            if (ranks[cids[i]] == undefined) {
                judgeService.getRankInfo(cids[i], function (res) {
                    ranks[res.cid] = {
                        cid: res.cid,
                        dataURL: res.dataURL,
                        isReplay: res.isReplay,
                        title: res.title,
                        managerId: res.managerId,
                        managerName: res.managerName,
                        endTime: new Date().valueOf() + res.remainingLength,    //locally
                        beginTime: res.beginTime,    //server side
                        length: parseInt(res.length),
                        lastFetchTime: 0
                    };
                    if (++cnt == cids.length) {
                        updateRankData();
                    }
                });
            } else if (++cnt == cids.length) {
                updateRankData();
            }
        }
    }

    function updateRankData() {
        var cnt = 0;
        for (var i = 0; i < cids.length; i++) {
            if (ranks[cids[i]].length && (!ranks[cids[i]].data || ranks[cids[i]].lastFetchTime < Math.min(startTime + selectedTime - ti[1], ranks[cids[i]].endTime))) {
                var curTime = new Date().valueOf();
                var url = ranks[cids[i]].dataURL + (ranks[cids[i]].endTime < curTime ? "" : "?" + curTime);
                //var url = ranks[cids[i]].dataURL;
                $.getJSON(basePath + "/" + url, function (rankData) {
                    var curCid = rankData[0];
                    ranks[curCid].data = rankData;
                    ranks[curCid].lastFetchTime = new Date().valueOf();
                    if (++cnt == cids.length) {
                        calcRankTable();
                    }
                });
            } else if (++cnt == cids.length) {
                calcRankTable();
            }
        }
    }

    function calcRankTable() {
        var pnum = $("table#viewContest tr").length - 1;
        var sb = {}
        var firstSolveTime = [];
        var totalSubmission = [];
        var correctSubmission = [];
        var myStatus = [];
        var username = {};
        var nickname = {};
        var my_uid_cid = $("#my_account").attr("uid") + "_" + cid;

        for (var j = 0; j < pnum; ++j) {
            totalSubmission[j] = correctSubmission[j] = 0;
        }
        $.each(cids, function (i, curCid) {
            if (isNaN(curCid) || !ranks[curCid].length) {
                return;
            }
            $.each(ranks[curCid].data, function (key, s) {
                if (key == 0) {
                    return;
                } else if (key == 1) {
                    for (uid in s) {
                        var name = s[uid];
                        username[uid] = name[0];
                        nickname[uid] = name[1];
                    }
                    return;
                }
                if (s[3] * 1000 > selectedTime)return;
                var name = s[0] + "_" + curCid;
                if (!sb[name]) {
                    sb[name] = [];
                }
                if (sb[name][s[1]] == undefined) {
                    sb[name][s[1]] = [-1, 0];
                }
                if (sb[name][s[1]][0] < 0) {
                    totalSubmission[s[1]]++;
                    if (s[2]) {
                        sb[name][s[1]][0] = s[3];
                        if (firstSolveTime[s[1]] == undefined || s[3] < firstSolveTime[s[1]]) {
                            firstSolveTime[s[1]] = s[3];
                        }
                        correctSubmission[s[1]]++;
                    } else {
                        sb[name][s[1]][1]++;
                    }
                }
            });
        });

        var result = [];
        for (var name in sb) {
            var solve = 0, penalty = 0;
            for (i in sb[name]) {
                if (sb[name][i]) {
                    if (sb[name][i][0] >= 0) {
                        if (name == my_uid_cid) {
                            myStatus[i] = 2;
                        }
                        solve++;
                        penalty += sb[name][i][0] + 1200 * sb[name][i][1];
                    } else if (name == my_uid_cid) {
                        myStatus[i] = 1;
                    }
                }
            }
            result.push([name, solve, penalty]);
        }
        result.sort(function (a, b) {
            return b[1] - a[1] || a[2] - b[2];
        });

        var showNick = Vjudge.storage.get("show_nick", true) == true;
        var showUsername = Vjudge.storage.get("show_username", true) == true;
        var showAllTeams = Vjudge.storage.get("show_all_teams", 0);
        var sbHtml = [];

        for (var i = 0; i < result.length; ++i) {
            var curInfo = result[i];
            var splitIdx = curInfo[0].lastIndexOf("_");
            var uid = curInfo[0].substr(0, splitIdx);
            var curCid = curInfo[0].substr(splitIdx + 1);
            if (showAllTeams == 0 && i >= 50 && (cid != curCid || !username[uid])) {
                continue;
            }
            sbHtml.push("<div data-id='" + escape(curInfo[0]).replace(/\W/g, "_") + "' class='disp");
            if (cid == curCid) {
                sbHtml.push(" cur_tr");
                if (my_uid_cid == curInfo[0]) {
                    sbHtml.push(" my_tr");
                }
            }
            sbHtml.push("' cid='" + curCid + "' u='" + username[uid] + "'><div class='meta_td rank'>" + (i + 1) + "</div><div class='meta_td id");
            if (username[uid]) {
                var displayTitle =
                    showNick == showUsername ?
                            nickname[uid] && username[uid] != nickname[uid] ?
                                    username[uid] + " (" + nickname[uid] + ")" :
                                    username[uid] :
                    showNick && nickname[uid] ?
                            nickname[uid] :
                            username[uid];
                var displayName =
                    showNick == showUsername ?
                            nickname[uid] && username[uid] != nickname[uid] ?
                                    username[uid] + "<span style='color:gray'>(" + nickname[uid] + ")</span>" :
                                    username[uid] :
                    showNick && nickname[uid] ?
                            nickname[uid] :
                            username[uid];
                sbHtml.push("' title='" + displayTitle + "'><a target='_blank' href='" + basePath + "/user/profile.action?uid=" + uid + "'>" + displayName +  "</a></div>");
            } else {
                sbHtml.push(" replay' title='" + uid + "'>" + uid + "</div>");
            }
            var penaltyInHMS = dateFormat(curInfo[2], 0, 1);
            var penaltyInMinute = dateFormat(curInfo[2], 1, 1);
            sbHtml.push("<div class='meta_td solve'>" + curInfo[1] + "</div><div class='meta_td penalty_td standing_time' v0='" + penaltyInHMS + "' v1='" + penaltyInMinute + "'>" + penaltyInMinute + "</div>");

            var thisSb = sb[curInfo[0]];
            for (var j = 0; j <= pnum; ++j) {
                var probInfo = thisSb[j];
                if (!probInfo) {
                    sbHtml.push("<div class='standing_time'/>");
                } else {
                    sbHtml.push("<div ");
                    if (probInfo[0] < 0) {
                        sbHtml.push("class='red");
                    } else if (firstSolveTime[j] == probInfo[0]) {
                        sbHtml.push("class='solvedfirst");
                    } else {
                        sbHtml.push("class='green");
                    }
                    sbHtml.push(" standing_time' pid='" + j + "'>" + dateFormat(probInfo[0], 0, 1) + "<br />" + (probInfo[1] ? "<span>(-" + probInfo[1] + ")</span>" : blankChar) + "</div>");
                }
            }
            sbHtml.push("</div>");
        }

        exportRankHtml = ["<tr><th>Rank</th><th>Id</th><th>Solve</th><th>Penalty</th>"];
        for (var i = 0; i < pnum; i++) {
            exportRankHtml.push("<th>" + String.fromCharCode(65 + i) + "</th>")
        }
        exportRankHtml.push("</tr>");
        for (var i = 0; i < result.length; ++i) {
            var curInfo = result[i];
            var splitIdx = curInfo[0].lastIndexOf("_");
            var uid = curInfo[0].substr(0, splitIdx);
            var curCid = curInfo[0].substr(splitIdx + 1);
            if (showAllTeams == 0 && i >= 50 && (cid != curCid || !username[uid])) {
                continue;
            }
            exportRankHtml.push("<tr><td>" + (i + 1) + "</td><td>");
            if (username[uid]) {
                exportRankHtml.push(
                    (
                        showNick == showUsername ?
                                nickname[uid] && username[uid] != nickname[uid] ?
                                        username[uid] + "(" + nickname[uid] + ")" :
                                        username[uid] :
                        showNick && nickname[uid] ?
                                nickname[uid] :
                                username[uid]
                    ) + "</td>");
            } else {
                exportRankHtml.push(uid + "</td>");
            }
            var penaltyInHMS = dateFormat(curInfo[2], 0, 1);
            exportRankHtml.push("<td>" + curInfo[1] + "</td><td>" + penaltyInHMS + "</td>");

            var thisSb = sb[curInfo[0]];
            for (var j = 0; j <= pnum; ++j) {
                var probInfo = thisSb[j];
                if (!probInfo) {
                    exportRankHtml.push("<td/>");
                } else {
                    exportRankHtml.push("<td>" + dateFormat(probInfo[0], 0, 1) + (probInfo[1] ? "(-" + probInfo[1] + ")" : "") + "</td>");
                }
            }
            exportRankHtml.push("</tr>");
        }
        exportRankHtml = ("<table>" + exportRankHtml.join("") + "</table>").replace(blankChar, "");
        Vjudge.storage.set("exportRankHtml", exportRankHtml, true);

        var maxCorrectNumber = 0, totalNumber = 0, totalCorrectNumber = 0;
        for (var j = 0; j < pnum; ++j) {
            totalNumber += totalSubmission[j];
            totalCorrectNumber += correctSubmission[j];
            if (maxCorrectNumber < correctSubmission[j]) {
                maxCorrectNumber = correctSubmission[j];
            }
        }
        for (var j = 0; j < pnum; ++j) {
            if (totalSubmission[j]) {
                var ratio = maxCorrectNumber ? correctSubmission[j] / maxCorrectNumber : 0.0;
                $("#rank_foot div").eq(j + 4)
                    .css("background-color", grayDepth(ratio))
                    .removeClass("white-font")
                    .removeClass("black-font")
                    .addClass(ratio < .5 ? "black-font" : "white-font")
                    .html((myStatus[j] == 2 ? "<img src='" + basePath + "/images/yes.png' height='20'/>" : myStatus[j] == 1 ? "<img src='" + basePath + "/images/no.png' height='20'/>" : blankChar) +     "<br />" +
                        "<a href='" + basePath + "/contest/view.action?cid=" + cid + "#status//" + String.fromCharCode(65 + j) + "/1'>" + correctSubmission[j] + "</a>" +
                        "/" +
                        "<a href='" + basePath + "/contest/view.action?cid=" + cid + "#status//" + String.fromCharCode(65 + j) + "/0'>" + totalSubmission[j] + "</a>" +
                        "<br />" + Math.floor(100 * correctSubmission[j] / totalSubmission[j]) + "%");
            } else {
                $("#rank_foot div").eq(j + 4).css("background", "transparent").html("");
            }
            if (myStatus[j] == 2) {
                $("#viewContest tbody tr:eq(" + j + ") td:eq(0)").html("<img src='" + basePath + "/images/yes.png' height='15'/>");
            } else if (myStatus[j] == 1) {
                $("#viewContest tbody tr:eq(" + j + ") td:eq(0)").html("<img src='" + basePath + "/images/no.png' height='15'/>");
            } else {
                $("#viewContest tbody tr:eq(" + j + ") td:eq(0)").html("");
            }
            if (totalSubmission[j] > 0) {
                $("#viewContest tbody tr:eq(" + j + ") td:eq(1)").html("<a href='" + basePath + "/contest/view.action?cid=" + cid + "#status//" + String.fromCharCode(65 + j)+ "/1'>" + correctSubmission[j] + "</a> / <a href='" + basePath + "/contest/view.action?cid=" + cid + "#status//" + String.fromCharCode(65 + j)+ "/0'>" + totalSubmission[j] + "</a>");
            } else {
                $("#viewContest tbody tr:eq(" + j + ") td:eq(1)").html("");
            }
        }
        if (totalNumber) {
            $("#rank_foot div").eq(pnum + 4).css("background-color", "#D3D6FF").html(blankChar + "<br />" + totalCorrectNumber + "/" + totalNumber + "<br />" + Math.floor(100 * totalCorrectNumber / totalNumber) + "%");
        } else {
            $("#rank_foot div").eq(pnum + 4).css("background-color", "transparent").html("");
        }

        $("#rank_header").width($("#rank").css("width"));
        $("#rank_foot").width($("#rank").css("width"));

        $("#rank_data_destination").html(sbHtml.join(""))
        .prepend($("#rank_header").clone().css({"position": "", "top": "", "z-index": ""}).attr("id", "rank_header_1").show())
        .append($("#rank_foot").clone().css({"position": "", "bottom": "", "z-index": ""}).attr("id", "rank_foot_1").show());

        if (location.hash.indexOf("#rank") == 0 && $("#rank_data_source > div").length > 2 && Vjudge.storage.get("show_animation", "1") > 0 && $("#rank_data_destination > div").length <= 70) {
            $('#rank_data_destination > div').each(function () {
                $("#rank_data_source > div[data-id=" + $(this).attr("data-id") + "]").html(this.innerHTML);
            });
            $('#rank_data_source').quicksand( $('#rank_data_destination > div'), {
                "duration": 1000
            }, function () {
                $("#rank_data_destination").html("");
                adjustRankTool();
            });
        } else {
            $('#rank_data_source').removeAttr("style").html($('#rank_data_destination').html());
            $("#rank_data_destination").html("");
            adjustRankTool();
        }

        lastRankUpdateTime = selectedTime;
    }

    function showDescription(num, desc_index) {
        var problem = problemSet[num];
        var description = problem.descriptions[desc_index];
        $("[name=desc_index]").val(desc_index);
        $("div.hiddable").hide();
        for (var elem in description) {
            if (description[elem]) {
                $("#vj_" + elem).show();
                $("#vj_" + elem + " div").html(description[elem]);
            }
        }
        problem.desc_index = desc_index;
    }


    function getResult() {
        var workingRunIds = [];
        $("tr.working").each(function () {
            workingRunIds.push($(this).attr("id"));
        });
        if (!workingRunIds.length) {
            return;
        }

        judgeService.getResult(workingRunIds, function (back) {
            for (var rowBack in back) {
                cbRow(back[rowBack]);
            }
            clearTimeout(timeoutHandle);
            timeoutHandle = setTimeout(getResult, 2000);
        });
    }

    function cbRow(back) {
        var id = back[0];
        var result = back[1];
        var memory = back[2];
        var time = back[3];
        var info = back[4];
        var color = back[5];
        var working = back[6];

        var $row = $("#" + id);
        if (!$row.length) {
            return;
        }
        if (info) {
            result = "<a href='" + basePath + "/problem/fetchSubmissionInfo.action?id=" + id + "' rel='facebox'>" + result + "</a>";
        }
        $row.removeClass("pending");
        $row.removeClass("no");
        $row.removeClass("yes");
        $row.removeClass("working");
        if (color == 0) {
            $row.addClass("yes");
            $(".memory", $row).html(memory + " KB");
            $(".time", $row).html(time + " ms");
        } else if (color == 1) {
            $row.addClass("no");
        } else {
            $row.addClass("pending");
        }
        if (working) {
            $row.addClass("working");
        } else if (color == 2) {
            $row.addClass("rejudge");
        }

        $(".result", $row).html(result);
        $('a[rel=facebox]', $row).facebox({
            loadingImage : basePath + '/facebox/loading.gif',
            closeImage   : basePath + '/facebox/closelabel.png'
        });
    }

    function grayDepth(ratio) {
        var res = (Math.floor((1 - ratio) * 0xff) * 0x010101).toString(16);
        while (res.length < 6) res = '0' + res;
        return "#" + res;
    }

    function dateFormat(time, formatIdx, inSeconds) {
        var sign = "";
        if (inSeconds != 1)    time /= 1000;
        if (time == -1)return blankChar;
        if (time < 0) {
            time = -time;
            sign = "-";
        }
        if (formatIdx == 1) {
            return sign + Math.floor(time / 60);
        } else {
            var h = Math.floor(time / 3600);
            var m = Math.floor(time % 3600 / 60);
            var s = Math.floor(time % 60 + 0.5);
            return sign + h + ":" + (m<10?"0":"") + m + ":" + (s<10?"0":"") + s;
        }
    }

    function resetTimeSlider () {
        clearInterval(sliderUpdater);
        var temp = function () {
            selectedTime = Math.min(ti[1] + new Date().valueOf() - startTime, ti[0]);
            slider.slider("value", Math.max(selectedTime, 0));
            displayTime();
            if (selectedTime > 0 && selectedTime < 1000 && ti[0] > 0) {
                window.location.reload();
            }
            updateRankInfo();
        };
        temp();
        if (ti[1] < ti[0]) {
            sliderUpdater = setInterval(temp, 1000);
        }
    };

    function adjustRankTool() {
        if (hash[0] != "#rank") {
            return;
        }
        var scrollLeft = $(window).scrollLeft();
        var $myRow = $("div.my_tr");
        if ($myRow.length && !Vjudge.isScrolledIntoView($myRow[0])) {
            $("#img_find_me").css("visibility", "visible");
        } else {
            $("#img_find_me").css("visibility", "hidden");
        }

        if (scrollLeft > 0 || !Vjudge.isScrolledIntoView("#contest_title")) {
            $("#img_go_top").css("visibility", "visible");
        } else {
            $("#img_go_top").css("visibility", "hidden");
        }

        var marginLeft = $("#contest_tabs").offset().left;
        $("#rank_header").css("left", 3+marginLeft-scrollLeft + "px");
        $("#rank_foot").css("left", 3+marginLeft-scrollLeft + "px");
        if (!Vjudge.isScrolledIntoView("#rank_header_1")) {
            $("#rank_header").show();
        } else {
            $("#rank_header").hide();
        }
        if (!Vjudge.isScrolledIntoView("#rank_foot_1")) {
            $("#rank_foot").show();
        } else {
            $("#rank_foot").hide();
        }
    }    

});
