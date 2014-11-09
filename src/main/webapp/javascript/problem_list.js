$(function() {
    if ($("#js_require_listProblem").length == 0) {
        return;
    }
    

    $("#OJId").selectmenu({
        change : function(){
            updateHash("OJId");
        }
    }).selectmenu( "menuWidget").addClass( "ui-menu-icons avatar" );
    
    $(["#OJId"]).each(function(_, id){
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

        $("[name='OJId']").val(queryParam.OJId).selectmenu("refresh");
        $("[name='probNum']").val(queryParam.probNum);
        $("[name='title']").val(queryParam.title);
        $("[name='source']").val(queryParam.source);
        
        if (oTable) {
            oTable.draw();
        }
        Vjudge.sendGaPageview();
    }).hashchange();
    
    var oTable = initDataTable();
    
    /////////////////////////////////////////////////////////////
    
    function initDataTable() {
        return $('#listProblem').DataTable({
            "processing": true,
            "serverSide": true,
            "lengthMenu": [ 10, 20, 25, 50, 100 ],
            "pageLength": 20,
            "dom": '<"H"pr<"#buttonContainer">>t<"F"il>',
            "jQueryUI": true,
            "autoWidth": false,
            "stateSave": true,
            "language": {
                "info": "_START_ to _END_ of _TOTAL_ problems",
                "infoEmpty": "No problems",
                "infoFiltered": " (filtering from _MAX_ total problems)",
                "thousands": ""
            },
            "order": [[ 3, "desc" ]],
            "pagingType": "simple_numbers",
            "ajax": {
                "url": basePath + "/problem/listProblem.action",
                "type": "POST",
                "data": function ( data ) {
                    $.extend(data, getQueryParam());
                },
                "dataSrc": function ( json ) {
                    var rows = [];
                    for ( var i = 0, ien = json.data.length; i < ien ; i++ ) {
                        var raw = json.data[i];
                        rows.push([
                            raw[0],
                            "<a href='" + basePath + "/problem/visitOriginUrl.action?id=" + raw[5] + "' target='_blank'>" + raw[1] + "</a>",
                            "<a href='" + basePath + "/problem/viewProblem.action?id=" + raw[5] + "'>" + raw[2] + "</a>",
                            "<div class='localizedTime'>" + raw[3] + "</div>",
                            "<div>" + (raw[4] || "") + "</div>",
                            raw[7]
                        ]);
                    }
                    return rows;
                }     
            },
            "columns": [
                { "className" : "oj", "orderable": false, "searchable": false },
                { "className" : "prob_num" },
                { "className" : "title" },
                { "className" : "date", "orderSequence": [ "desc", "asc"]},
                { "className" : "source" },
                { "visible": false }
            ],
            "createdRow": function( row, data, dataIndex ) {
                if ( data[5] == 1 || data[5] == 2 ) {
                    $(row).addClass( 'Running' );
                }
            },
            "preDrawCallback": function( settings ) {
                if ($("#probNum").val().length > 0 && $("#probNum").is(":focus")) {
                    $("#title").val("");
                    $("#source").val("");
                } else if ($("#title").val().length > 0 || $("#source").val().length > 0) {
                    $("#probNum").val("");
                }
            },
            "drawCallback": function( settings ) {
                Vjudge.renderLocalizedTime(function() {
                    return this.parent();
                }, function() {
                    return $("th.date");
                });
            },
            "initComplete": function(settings, json) {
                $("#filter").button().appendTo("#buttonContainer").click(function(){
                    if (!updateHash()) {
                        oTable.draw();
                    }
                });
                $("#reset").button().appendTo("#buttonContainer").click(function(){
                    $("[name='OJId']").val("All");
                    $("[name='probNum']").val("");
                    $("[name='title']").val("");
                    $("[name='source']").val("");
                    if (!updateHash()) {
                        oTable.draw();
                    }
                });
            }
        });        
    }
    
    /**
     * @return {changed} whether hash changed
     */
    function updateHash(triggerVar) {
        var OJId = $("[name='OJId']").val();
        var probNum = $("[name='probNum']").val();
        var title = $("[name='title']").val();
        var source = $("[name='source']").val();
        
        var oldHash = location.hash;
        location.hash = "#OJId=" + OJId + "&probNum=" + probNum + "&title=" + title + "&source=" + source;
        return oldHash != location.hash;
    }
    
    function getQueryParam() {
        var defaultQueryParam = {
            OJId : "All",
            probNum : "",
            title : "",
            source : ""
        };
        try {
            return Vjudge.storage.get("problem.list.queryParam", defaultQueryParam);
        } catch (e) {
            console.error(e);
            return defaultQueryParam;
        }
    }
    
    function setQueryParam(queryParam) {
        Vjudge.storage.set("problem.list.queryParam", queryParam);
    }
    
});
