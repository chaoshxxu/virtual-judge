$(function() {
    if ($("#js_require_listContest").length == 0) {
        return;
    }

    $("#contestType").buttonset();
    $("input[name='contestType']").change(function(){
        updateHash("contestType");
    });
    
    $("#contestRunningStatus").buttonset();
    $("[name='contestRunningStatus']").change(function(){
        updateHash("contestRunningStatus");
    });
    
    $("#contestOpenness").selectmenu({
        change : function(){
            console.log($("[name='contestOpenness']").length);
            updateHash("contestOpenness");
        }
    }).selectmenu( "menuWidget").addClass( "ui-menu-icons avatar" );
    $(["#contestOpenness"]).each(function(_, id){
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
    }).change(function(){
        updateHash($(this).attr("name"));
    });
    
    $(window).hashchange( function () {
        var queryParam = getQueryParam();
        var regexp = /(\w+)=([^$&]*)/g;
        while (true) {
            var match = regexp.exec(location.hash);
            if (match == null) {
                break;
            }
            queryParam[match[1]] = match[2];
        }
        setQueryParam(queryParam);

        $("[name='contestType']").filter('[value=' + queryParam.contestType + ']').prop('checked', true);
        $("#contestType").buttonset("refresh");

        $("[name='contestRunningStatus']:not([value=3])").prop("disabled", queryParam.contestType == 1 ? "disabled" : false);
        $("[name='contestRunningStatus']").filter('[value=' + queryParam.contestRunningStatus + ']').prop('checked', true);
        $("#contestRunningStatus").buttonset("refresh");

        $("[name='contestOpenness']").val(queryParam.contestOpenness).selectmenu("refresh");
        $("[name='title']").val(queryParam.title);
        $("[name='manager']").val(queryParam.manager);
        
        if (oTable) {
            oTable.draw();
        }
        Vjudge.sendGaPageview();
    }).hashchange();
    
    var oTable = initDataTable();
    
    /////////////////////////////////////////////////////////////    
    
    function initDataTable() {
        return $('#listContest').DataTable({
            "processing": true,
            "serverSide": true,
            "lengthMenu": [ 10, 20, 25, 50, 100 ],
            "pageLength": 20,
            "dom": '<"H"pr<"#buttonContainer_l"><"#buttonContainer_r">>t<"F"il>',
            "jQueryUI": true,
            "autoWidth": false,
            "stateSave": true,
            "language": {
                "info": "_START_ to _END_ of _TOTAL_ contests",
                "infoEmpty": "No contests",
                "infoFiltered": " (filtering from _MAX_ total contests)",
                "thousands": ""
            },
            "order": [[ 2, "desc" ]],
            "pagingType": "simple_numbers",
            "ajax": {
                "url": basePath + "/contest/listContest.action",
                "type": "POST",
                "data": function ( data ) {
                    $.extend(data, getQueryParam());
                },
                "dataSrc": function ( json ) {
                    var rows = [];
                    for ( var i = 0, ien = json.data.length; i < ien ; i++ ) {
                        var raw = json.data[i];
                        rows.push({
                            contestId : raw[0],
                            contestTitle : raw[1],
                            contestBeginTime : raw[2],
                            contestEndTime : raw[3],
                            contestPublic : raw[4],
                            managerUserName : raw[5],
                            managerUserId : raw[6]
                        });
                    }
                    return rows;
                }     
            },
            "columns": [
                {
                    "className" : "contest_id",
                    "orderSequence": [ "desc", "asc"],
                    "data": function ( row ) {
                        return row.contestId;
                    }
                },
                {
                    "className" : "title",
                    "data": function ( row ) {
                        return "<div class='title'><a cid='" + row.contestId + "' class='contest_entry' href='" + basePath + "/contest/view.action?cid=" + row.contestId + "#overview'>" + row.contestTitle + "</a></div>";
                    }
                },
                { 
                    "className" : "date", 
                    "orderSequence": [ "desc", "asc"],
                    "data": function ( row ) {
                        return "<div class='localizedTime'>" + row.contestBeginTime + "</div>";
                    }
                },
                { 
                    "className" : "length",
                    "orderable": false,
                    "data": function ( row ) {
                        var seconds = Math.round((row.contestEndTime - row.contestBeginTime) / 1000);
                        var days = seconds / 86400;
                        var hours = seconds / 3600;
                        var minutes = seconds / 60;
                        if (days >= 1) {
                            return Math.round(days * 10) / 10 + " days";
                        } else if (hours >= 1) {
                            return Math.round(hours * 10) / 10 + " hours";
                        } else if (seconds > 0) {
                            return Math.round(minutes * 10) / 10 + " minutes";
                        } else {
                            return "-";
                        }
                    }
                },
                { 
                    "className" : "openness",
                    "orderable": false,
                    "data": function ( row ) {
                        return row.contestPublic ? "Public" : "Private";
                    }
                },
                { 
                    "className" : "manager",
                    "data": function ( row ) {
                        return "<a href='" + basePath + "/user/profile.action?uid=" + row.managerUserId + "'>" + row.managerUserName + "</a>";
                    }
                }
            ],
            "createdRow": function( row, data, dataIndex ) {
                var serverTime = Vjudge.getServerTime().valueOf();
                if (serverTime < data.contestBeginTime) {
                    $(row).addClass( 'Scheduled' );
                } else if (serverTime < data.contestEndTime) {
                    $(row).addClass( 'Running' );
                } else {
                    $(row).addClass( 'Ended' );
                }
                
                $(row).find("td:eq(4)").addClass(data.contestPublic ? "Public" : "Private");
            },
            "preDrawCallback": function( settings ) {
            },
            "drawCallback": function( settings ) {
                Vjudge.renderLocalizedTime(function() {
                    return this.parent();
                }, function() {
                    return $("th.date");
                });
            },
            "initComplete": function(settings, json) {
                $("#filter").button().appendTo("#buttonContainer_l").click(function(){
                    if (!updateHash()) {
                        oTable.draw();
                    }
                });
                $("#reset").button().appendTo("#buttonContainer_l").click(function(){
                    $("[name='contestType']:first").prop("checked", true);
                    $("[name='contestRunningStatus']:first").prop("checked", true);
                    $("[name='title']").val("");
                    $("[name='manager']").val("");
                    if (!updateHash()) {
                        oTable.draw();
                    }
                });
                
                $("#contestRunningStatus").appendTo("#buttonContainer_r");
                $("#contestType").appendTo("#buttonContainer_r");
            }
        });        
    }    
    
    /**
     * @return {changed} whether hash changed
     */
    function updateHash(triggerVar) {
        var contestType = $("input[name='contestType']:checked").val();
        var contestRunningStatus = (contestType == 1 ? 3 : triggerVar == "contestType" ? 0 : $("input[name='contestRunningStatus']:checked").val());
        var contestOpenness = $("[name='contestOpenness']").val();
        var title = $("input[name='title']").val();
        var manager = $("input[name='manager']").val();
        
        var oldHash = location.hash;
        location.hash = "#contestType=" + contestType + "&contestRunningStatus=" + contestRunningStatus + "&contestOpenness=" + contestOpenness + "&title=" + title + "&manager=" + manager;
        return oldHash != location.hash;
    }
    
    function getQueryParam() {
        var defaultQueryParam = {
            contestType : "0",
            contestRunningStatus : "0",
            contestOpenness : "0",
            title : "",
            manager : ""
        };
        try {
            return Vjudge.storage.get("contest.list.queryParam", defaultQueryParam);
        } catch (e) {
            console.error(e);
            return defaultQueryParam;
        }
    }
    
    function setQueryParam(queryParam) {
        Vjudge.storage.set("contest.list.queryParam", queryParam);
    }
    
    
    
    
//    $("input[type='checkbox']").each(function(){
//        if ($.cookie("checked_" + $(this).attr("name")) == 'false') {
//            $(this).removeAttr("checked");
//        }
//    });
//
//    if ($.cookie("contestType") == undefined) {
//        $.cookie("contestType", 0, {expires:7});
//    }
//    $("input[name='contestType']").get($.cookie("contestType")).checked = 1;
    
    
    
//    $("#head_status").insertBefore("div#listContest_processing").show();
//    $("div.dataTables_filter").css("width", "250px");
//    
//    $("#add_contest, #statistic_contest").button();
//
//    $("input[type='checkbox']").change(function() {
//        $.cookie("checked_" + $(this).attr("name"), $(this).prop("checked"), {expires:7});
//        oTable.fnDraw();
//    });
//    
//    $("input[name='contestType']").change(function(){
//        $.cookie("contestType", $(this).val(), {expires:7});
//        oTable.fnDraw();
//    });
//    
//    $( "#dialog-form-contest-login" ).dialog({
//        autoOpen: false,
//        height: 200,
//        width: 350,
//        position: ['top', 50],
//        modal: true,
//        buttons: {
//            "Login": function() {
//                var cid = $("#cid").val(), info = {password: $("#contest_password").val(), cid: cid};
//                $.post('contest/loginContest.action', info, function(data) {
//                    if (data == "success") {
//                        $( this ).dialog( "close" );
//                        if (location.hash) {
//                            location.hash = "";
//                        }
//                        window.location.href = "contest/view.action?cid=" + cid + "#overview";
//                    } else {
//                        updateTips(data);                        
//                    }
//                });
//            },
//            "Cancel": function() {
//                $( this ).dialog( "close" );
//            }
//        },
//        close: function() {
//            $("p.validateTips").html("");
//            $( this ).find(":input").val("");
//            if (location.hash) {
//                location.hash = "";
//            }
//        }
//    }).keyup(function(e){
//        if (e.keyCode == 13) {
//            $(this).dialog('option', 'buttons')['Login']();
//        }
//    });
//
//    $("a.contest_entry").live('click', function(){
//        attemptLoginContest($(this).attr("cid"));
//        return false;
//    });
//    
//    var para = parseUrlParameter();
//    var query = para['q'];
//    if (query) {
//        setTimeout(function(){
//            oTable.fnFilter(query);
//        }, 300);
//    }
//    if (location.hash.match(/#\d+$/)) {
//        attemptLoginContest(location.hash.substring(1));
//    }
//    
//    
//    function comfirmDeleteContest(cid){
//        if (confirm("Sure to delete this contest?")){
//            location = 'contest/deleteContest.action?cid=' + cid;
//        }
//    }
//
//    function attemptLoginContest(cid) {
//        $("#cid").val(cid);
//        $.post("contest/checkAuthorizeStatus.action?cid=" + cid, function(authorizeStatus){
//            if (authorizeStatus == "success") {
//                if (location.hash) {
//                    location.hash = "";
//                }
//                window.location.href = "contest/view.action?cid=" + cid + "#overview";
//            } else {
//                $("#dialog-form-contest-login").dialog('open');
//            }
//        });
//    }    
    
});
