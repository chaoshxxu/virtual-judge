$(function() {
    if ($("#js_require_problem_status").length == 0) {
        return;
    }

    var getResultScheduler;
    var isSup = $("#isSup").val() == 1;
    
    $("#OJId").selectmenu({
    	change : function(){
        	updateHash("OJId");
    	}
    }).selectmenu( "menuWidget").addClass( "ui-menu-icons avatar" );

    $("#res").selectmenu({
        change : function(){
            updateHash("res");
        }
    });

    $("#language").selectmenu({
        change : function(){
            updateHash("language");
        }
    });

    $(["#OJId", "#res", "#language"]).each(function(_, id){
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
//    	if (event.which == 13) {
//        	updateHash($(this).attr("name"));
//    	}
    }).change(function(){
    	updateHash($(this).attr("name"));
    });

    
    $(["run_id", "memory", "time", "length"]).each(function(_, name){
        $("th." + name).click(function(event){
        	$("[name=orderBy]").val(name);
        	updateHash("orderBy");
        });
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
    	setQueryParam(queryParam)
    	
    	$("[name='un']").val(queryParam.un);
    	$("[name='OJId']").val(queryParam.OJId).selectmenu("refresh");
    	$("[name='probNum']").val(queryParam.probNum);
        $("[name='res']").val(queryParam.res).selectmenu("refresh");
        $("[name='language']").val(queryParam.language).selectmenu("refresh");
    	$("[name='orderBy']").val(queryParam.orderBy);
    	if (oTable) {
            oTable.draw();
    	}
    }).hashchange();
    
    $(document).on("click", ".rejudge", function(){
        var $row = $(this).closest("tr");
        var id = $row.attr("id");
        $row.removeClass("no");
        $row.removeClass("yes");
        $row.addClass("working");
        $row.removeClass("rejudge");
        $.post(basePath + "/problem/rejudge.action", {id: id}, function() {
            clearTimeout(getResultScheduler);
            getResultScheduler = setTimeout(getResult, 1000);
        });
    });
    $(document).on("click", ".rejudge a", function(event){
        event.stopPropagation();
    });
    
    var oTable = initDataTable();

    //////////////////////////////////////////////////////////////

    function initDataTable() {
    	return $('#status').DataTable({
            "processing": true,
            "serverSide": true,
            "pageLength": 20,
            "dom": '<"H"pr<"#buttonContainer">>t<"F">',
            "jQueryUI": true,
            "autoWidth": false,
            "stateSave": true,
            "ordering": true,
            "pagingType": "simple_numbers",
            "ajax": {
                "url": basePath + "/problem/fetchStatus.action",
                "type": "POST",
                "data": function ( data ) {
                	$.extend(data, getQueryParam());
                },
                "dataSrc": function ( json ) {
                    var rows = [];
                    for ( var i = 0, ien = json.data.length; i < ien ; i++ ) {
                        var raw = json.data[i];
                        rows.push([
                            raw[0] + (raw[13] > 0 ? "(<a href='" + basePath + "/contest/view.action?cid=" + raw[13] + "#overview' target='_blank'>#</a>)" : ""),
                            "<div><a href='" + basePath + "/user/profile.action?uid=" + raw[9] + "' target='_blank'>" + raw[1] + "</a><img src='" + basePath + "/images/find_me.png' /></div>",
                            "<div><a href='" + basePath + "/problem/visitOriginUrl.action?id=" + raw[2] + "' target='_blank'>" + raw[11] + "</a></div>",
                            "<div><a href='" + basePath + "/problem/viewProblem.action?id=" + raw[2] + "'>" + raw[12] + "</a><img src='" + basePath + "/images/find_me.png' /></div>",
                            "<div>" + (raw[14] ? "<a href='" + basePath + "/problem/fetchSubmissionInfo.action?id=" + raw[0] + "' rel='facebox'>" + raw[3] + "</a>" : raw[3]) + "</div>",
                            (raw[15] == 0 && raw[4] > 0 ? raw[4] : ""),
                            (raw[15] == 0 ? raw[5] : ""),
                            "<div>" + (raw[10] ? "<a " + (raw[10] == 2 ? "class='shared'" : "") + " target='_blank' href='" + basePath + "/problem/viewSource.action?id=" + raw[0] + "'>" + raw[6] + "</a>" : raw[6]) + "</div>",
                            raw[7],
                            "<div class='localizedTime'>" + raw[8] + "</div>",
                            raw[15],
                            raw[16]
                        ]);
                    }
                    return rows;
                }     
            },
            "columns": [
                { "className" : "run_id" },
                { "className" : "username" },
                { "className" : "oj" },
                { "className" : "prob_num" },
                { "className" : "result" },
                { "className" : "memory" },
                { "className" : "time" },
                { "className" : "language" },
                { "className" : "length" },
                { "className" : "date" },
                { "visible": false },
                { "visible": false }
            ],
            "columnDefs": [ {
            	"targets": "_all",
            	"orderable": false
            } ],
            "createdRow": function( row, data, dataIndex ) {
                var $row = $(row);
                $row.addClass(data[10] == 0 ? "yes" : data[10] == 1 ? "no" : "pending");
    			if (data[11]) {
    				$row.addClass("working");
    			}
    			$row.attr("id", data[0].replace(/\(.+/, ''));
    			$('a[rel=facebox]', $row).facebox({
    				loadingImage : basePath + '/facebox/loading.gif',
    				closeImage   : basePath + '/facebox/closelabel.png'
    			});
    			if (data[10] == 2 && data[11] == 0) {
    				$row.addClass("rejudge");
    			} else if (isSup) {
    				$("td.date", $row).addClass("rejudge");
    			}
    			$("td.username img", row).click(function(){
    			    var un = $(this).parent().find("a").text();
    			    if ($("#un").val() == un) {
    			        $("#un").val("");
    			    } else {
    			        $("#un").val(un);
    			    }
    				updateHash("un");
    			});
    			$("td.prob_num img", row).click(function(){
    			    var OJId = data[2].replace(/<.+?>/g, "");
    			    var probNum = $(this).parent().find("a").text();
    			    if ($("#OJId").val() == OJId && $("#probNum").val() == probNum) {
                        $("#OJId").val("All").selectmenu("refresh");
                        $("#probNum").val("");
    			    } else {
    			        $("#OJId").val(data[2].replace(/<.+?>/g, "")).selectmenu("refresh");
    			        $("#probNum").val($(this).parent().find("a").text());
    			    }
    				updateHash("probNum");
    			});
            },
            "preDrawCallback": function( settings ) {
            	var api = this.api();
            	var orderBy = getQueryParam().orderBy;
            	if (orderBy == 'run_id') {
            		api.order([0, 'desc']);
            	} else if (orderBy == 'memory') {
            		api.order([5, 'asc']);
            	} else if (orderBy == 'time') {
            		api.order([6, 'asc']);
            	} else if (orderBy == 'length') {
            		api.order([8, 'asc']);
            	}
            },
            "drawCallback": function( settings ) {
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
                		oTable.draw();
                	}
                });
                $("#reset").button().appendTo("#buttonContainer").click(function(){
                	$(".errorMessage").remove();
                	$("[name='un']").val("");
                	$("[name='OJId']").val("All");
                	$("[name='probNum']").val("");
                	$("[name='orderBy']").val("run_id");
                    $("[name='res']").val("0");
                    $("[name='language']").val("");
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
    	var un = $("[name='un']").val();
        var OJId = $("[name='OJId']").val();
        var probNum = $("[name='probNum']").val();
        var _orderBy = (OJId != 'All' && probNum) ? $("[name='orderBy']").val() : "run_id";
        var _res = $("[name='res']").val();
        var language = $("[name='language']").val();
        var orderBy, res;
        
        if (triggerVar == "res") {
        	res = _res;
        	orderBy = res == 1 ? _orderBy : "run_id";
        } else {
        	orderBy = _orderBy;
        	res = orderBy != "run_id" ? "1" : _res;
        }

        var oldHash = location.hash;
        location.hash = "#un=" + un + "&OJId=" + OJId + "&probNum=" + probNum + "&res=" + res + "&orderBy=" + orderBy + "&language=" + language;
        return oldHash != location.hash;
    }

    function getQueryParam() {
    	var defaultQueryParam = {
			un : "",
			OJId : "All",
			probNum : "",
            res : "0",
            language : "",
			orderBy : "run_id"
    	};
        try {
            return Vjudge.storage.get("problem.status.queryParam", defaultQueryParam);
        } catch (e) {
            console.error(e);
            return defaultQueryParam;
        }
    }
    
    function setQueryParam(queryParam) {
    	Vjudge.storage.set("problem.status.queryParam", queryParam);
    }
    
    function getResult(){
        var workingRunIds = [];
        $("tr.working").each(function(){
            workingRunIds.push($(this).attr("id"));
        });
        if (!workingRunIds.length) {
            return;
        }

        judgeService.getResult(workingRunIds, function(back){
            for (var rowBack in back) {
                cbRow(back[rowBack]);
            }
            clearTimeout(getResultScheduler);
            getResultScheduler = setTimeout(getResult, 2000);
        });
    }

    function cbRow(back){
        var id = back[0];
        var result = back[1];
        var memory = back[2];
        var time = back[3];
        var info = back[4];
        var color = back[5];
        var working = back[6];
        
        var $row = $("#" + id);
        if (!$row.length){
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
            $(".memory", $row).html(memory);
            $(".time", $row).html(time);
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
            loadingImage : 'facebox/loading.gif',
            closeImage   : 'facebox/closelabel.png'
        });
    }

});
