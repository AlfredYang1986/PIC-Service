/**
 * Created by qianpeng on 2017/6/15.
 */

/**
 * 重置按钮
 */
var reset=function () {
    var atc1=""
    var atc2=""
    var atc3=""
    var oral_name=""
    var product_name=""
    var edge=""
    var manufacture_type=""
    var manufacture_name=""
    var product_type=""
    var specifications=""
    var pac=""
    $(".selectInfo").remove("div")
    $("#yearInputb").val("")
    $("#monthInputb").val("")
    $("#guim").text("X")
    $('#zengzl').text("X");
    $("#fene").text("X")
    $("#chanps").text("X")


}
/**
 * 数据列表
 */
var showDataList = function() {
    $(".img").hide();
    $(".dataTable").show();
    pageResult(1)
}

var pageResult = function(skip) {

    searchCount++
    $("#tbody").empty();
    var c = $.extend(getSearchValue(), getTime())
    var data = JSON.stringify({
        "token": $.cookie("token"),
        "condition":{
            "category": c.category,
            "oral_name": c.oral_name,
            "product_name": c.product_name,
            "edge": c.edge,
            "date": c.date,
            "product_type": c.product_type,
            "manufacture_name": c.manufacture_name,
            "manufacture_type": c.manufacture_type,
            "specifications": c.specifications,
            "package": c.package
        },
        "skip": skip
    });
    ajaxData("/data/search", data, "POST", function(r){
        if (r.status == "ok") {
            $("#tbody").empty();
            $("#pageview").show()
            $.each(r.search_result, function(i, v){
                $("#tbody").append(v.html)
            })
            Page(r)
            if(searchCount == 5){
                $("#xssj").attr({"class":"search-btn","onclick":"showDig()"})
            }
        }else{
            $("#xssj").attr({"class":"search-btn","onclick":"showDig()"})
            $("#pageview").hide()
            Page(null)
        }
        searchCount = 0
    }, function(e){console.error(e)})
}

var getTime = function() {
    var timeType = $("#timeType").val();
    var start = null;
    var end = null;
    if(timeType == 1 && $("#yearInputb").val() != ""){
        var year = $("#yearInputb").val()
        start = (parseInt(year) - 1) + "01"
        end = year + "01"
    }else if(timeType == 2 && $("#monthInputb").val() != ""){
        var yearMonth = $("#monthInputb").val()
        var year = yearMonth.split("-")[0]
        var month = yearMonth.split("-")[1]
        start = (parseInt(year) - 1) + month
        end = year + month
    }

    var date = null;
    if(start != null) {
        date = {
            "date": {
                "start": start,
                "end": end
            }
        }
    }
    return date;
}

/**
 * 显示主页的四个小汇总
 */
var showDataGather = function() {
    var c = $.extend(getSearchValue(), getTime())
    var data = JSON.stringify({
        "token": $.cookie("token"),
        "condition":{
            "category": c.category,
            "oral_name": c.oral_name,
            "product_name": c.product_name,
            "date": c.date
        }
    });

    calcMarket(data)
    calcTrend(data)
    calcPercentage(data)
    productSize(data)

}

/**
 * 市场规模
 */
var calcMarket = function(data) {
    searchCount++
    ajaxData("/data/calc/market", data, "POST", function(r){
        if (r.status == "ok") {
            var market = r.result.calc.sales;
            $("#guim").text(toThousands(market))

            if(searchCount == 5){
                $("#xssj").attr({"class":"search-btn","onclick":"showDig()"})
            }
        }else{
            $("#xssj").attr({"class":"search-btn","onclick":"showDig()"})
        }
        searchCount = 0
    }, function (e) {console.error(e)})
}

/**
 * 市场增占率
 */
var calcTrend = function(data) {
    searchCount++
    ajaxData("/data/calc/trend", data, "POST", function (r) {
        if (r.status == "ok") {
            var trend = parseFloat(r.result.trend);
            var treNum=(Math.floor(trend*100)/100)+ "%"
            $('#zengzl').text(treNum);
            if(searchCount == 5){
                $("#xssj").attr({"class":"search-btn","onclick":"showDig()"})
            }
        }else {
            $("#xssj").attr({"class":"search-btn","onclick":"showDig()"})
        }
        searchCount = 0
    },function(e){console.error(e)})
}

/**
 * 市场份额
 */
var calcPercentage = function(data) {
    searchCount++
    ajaxData("/data/calc/percentage", data, "POST", function (r) {
        if (r.status == "ok") {
            var percentage = parseFloat(r.result.percentage);
            var percentNum=(Math.floor(percentage*10000)/100) +"%"
            $('#fene').text(percentNum);
            if(searchCount == 5){
                $("#xssj").attr({"class":"search-btn","onclick":"showDig()"})
            }
        }else {
            $("#xssj").attr({"class":"search-btn","onclick":"showDig()"})
        }
        searchCount = 0
    }, function (e) {console.error(e)})
}

/**
 * 产品数量
 */
var productSize = function(data) {
    searchCount++
    ajaxData("/data/calc/quantity", data, "POST", function(r){
        if (r.status == "ok") {
            $('#chanps').text(r.result.size);
            if(searchCount == 5){
                $("#xssj").attr({"class":"search-btn","onclick":"showDig()"})
            }
        }else {
            $("#xssj").attr({"class":"search-btn","onclick":"showDig()"})
        }
        searchCount = 0
    }, function(e){console.error(e)});
}

var showData = function() {
    var c = $.extend(getSearchValue(), getTime())
    var token = {"token": $.cookie("token")}
    var condition = {
        "condition": {
            "category": c.category,
            "oral_name": c.oral_name,
            "product_name": c.product_name,
            "date": c.date
        }
    }
    var reportid = ""
    if(!$.isEmptyObject(JSON.parse(JSON.stringify(condition)).condition)) {
        reportid = md5(JSON.stringify(token)+JSON.stringify(condition))
        var data = JSON.stringify({
            "reportid": reportid,
            "token" : token.token,
            "condition" : condition.condition
        })
        ajaxData("/data/reportparameter", data, "POST", function(r){}, function(e){console.error(e)})
    }
    return reportid
}



var report = function() {
    var result = showData()
    if(result != ""){
        var w = window.open("")
        w.window.location = "/report"+result
    }else {
        alert("error")
    }
    // var w = window.open("")
    // w.window.location = "/report"+"aa"
}

