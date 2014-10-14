$(function(){
    
    $("#same_contests_table").DataTable({
        "scrollY": 400,
        "scrollCollapse": true,
        "jQueryUI": true,
        "bPaginate": false,
        "bLengthChange": false,
        "bFilter": false,
        "bSort": false,
        "bInfo": false,
        "bAutoWidth": false
    });
    $("#same_contests_table_wrapper").css("min-height", "100px");

    var cid = $("[name=cid]").val();
    var ids = Vjudge.storage.get("contest_" + cid, [cid]);
    for (var i = 0; i < ids.length; i++){
        $("input[type=checkbox][value=" + ids[i] + "]").prop("checked",'true');
    }

    $("[name=showTeams]").eq(Vjudge.storage.get("show_all_teams", 0)).prop("checked", "checked");
    $("[name=showNick]").prop("checked", Vjudge.storage.get("show_nick", true) == true ? "checked" : "");
    $("[name=showUsername]").prop("checked", Vjudge.storage.get("show_username", true) == true ? "checked" : "");
    $("[name=showAnimation]").eq(Vjudge.storage.get("show_animation", "1")).prop("checked", "checked");

//    $("[name=showTeams]").change(function(){
//        if ($("[name=showTeams]:checked").val() == 1) {
//            $("[name=showTeams]").eq(0).prop("checked", "checked");
//        }
//    });

    $("[name=ids]").click(function(){
        updateCheckAll();
    });

    $("#checkAll").click(function(){
        $("[name=ids]").prop("checked", $(this).prop("checked"));
    });

    $("#exportRank").button();
    $("#exportRank").click(function(){
        var exportRankWindow = window.open('about:blank'); 
        exportRankWindow.document.write(Vjudge.storage.get("exportRankHtml", "", true)); 
    });

    updateCheckAll();

    function updateCheckAll() {
        if ($("[name=ids]:checked").length == 0){
            $("#checkAll").attr("checked", false);
        } else if ($("[name=ids]:not(:checked)").length == 0){
            $("#checkAll").attr("checked", true);
        }
    }
    
});
