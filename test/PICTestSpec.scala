
import java.io.{File, PrintWriter}
import java.util.Calendar

import play.core.server.Server
import play.api.routing.sird._
import play.api.mvc._
import play.api.libs.json._
import play.api.test._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import org.specs2.mutable.Specification
import play.api.libs.json.Json.toJson
import play.api.libs.ws.WSClient

/**
  * Created by alfredyang on 07/07/2017.
  */
class PICTestSpec extends Specification {
    import scala.concurrent.ExecutionContext.Implicits.global

    val user_name = "alfred"
    val pwd = "12345"

    var token : String = null

    /**
      * 在这里输入搜索的时间条件：
      * timeType 间隔方式：以年计=>"year";以月计=>"month"，gap：间隔长短（1表示1年或1月）
      */
    val endYear : Int = 2016
    val endMonth : Int = 12
    val timeType : String = "year"
    val gap : Int = 1

    val timeTypeList : List[String] = List("year","month")

    val marketUrl :String = "/data/calc/market"
    val trendUrl :String = "/data/calc/trend"
    val quantityUrl :String = "/data/calc/quantity"
    val percentageUrl :String = "/data/calc/percentage"
    val urlList : List[String] = List(marketUrl,trendUrl,quantityUrl)

    var atc_one :Map[String,List[String]]= null
    var atc_two :Map[String,List[String]]= null
    var atc_three :Map[String,List[String]]= null
    var oral :Map[String,List[String]]= null
    var product :Map[String,List[String]]= null
    var edge :Map[String,List[String]]= null
    var manufacture_name :Map[String,List[String]]= null

    val contains = "他汀类"

    val skip = 1
    val time_out = 120

    override def is = s2"""
        This is a PIC specification to check the 'conditionSearch' string

            The 'PIC ' conditionSearch functions should
                auth with password user_name:${user_name},pwd:${pwd}                                    $authToken
                get category list result must not empty!                                                $getCateList
                testCase3_15 result must be "ok"!                                                       $testCase3_15
                testCase3_15_search with condition with 治疗1类 && 年 双条件混合查询                        $testCase3_15_search
                testCase3_16 result must be "ok"!                                                       $testCase3_16
                testCase3_16_search with condition with 治疗1类 && 月 双条件混合查询                        $testCase3_16_search
                testCase3_17 result must be "ok"!                                                       $testCase3_17
                testCase3_17_search with condition with 治疗2类 && 年 双条件混合查询                        $testCase3_17_search
                testCase3_18 result must be "ok"!                                                       $testCase3_18
                testCase3_18_search with condition with 治疗2类 && 月 双条件混合查询                        $testCase3_18_search
                testCase3_19 result must be "ok"!                                                       $testCase3_19
                testCase3_19_search with condition with 治疗3类 && 年 双条件混合查询                        $testCase3_19_search
                testCase3_20 result must be "ok"!                                                       $testCase3_20
                testCase3_20_search with condition with 治疗3类 && 月 双条件混合查询                        $testCase3_20_search
                testCase3_21 result must be "ok"!                                                       $testCase3_21
                testCase3_21_search with condition with 治疗3类 && 通用名 && 年 三条件混合查询               $testCase3_21_search
                testCase3_22 result must be "ok"!                                                       $testCase3_22
                testCase3_22_search with condition with 治疗3类 && 通用名 && 月 三条件混合查询               $testCase3_22_search
                testCase3_23 result must be "ok"!                                                       $testCase3_23
                testCase3_23_search with condition with 治疗3类 && 产品名 && 年 三条件混合查询               $testCase3_23_search
                testCase3_24 result must be "ok"!                                                       $testCase3_24
                testCase3_24_search with condition with 治疗3类 && 产品名 && 月 三条件混合查询               $testCase3_24_search
                                                                              """

    def authToken =
    WsTestClient.withClient { client =>
        val result = Await.result(
        new PICClient(client, "http://127.0.0.1:9000").authWithPasswordTest(user_name, pwd), time_out.seconds)
        token = result
        result must_!= ""
    }

    def getCateList =
        WsTestClient.withClient { client =>
            val result = Await.result(
                new PICClient(client, "http://127.0.0.1:9000").getCateTest(), time_out.seconds)

            atc_one = Map("atc_one" -> result.get("atc_one").get.asOpt[List[String]].get)
            atc_two = Map("atc_tow" -> result.get("atc_tow").get.asOpt[List[String]].get)
            atc_three = Map("atc_three" -> result.get("atc_three").get.asOpt[List[String]].get)
            oral = Map("oral" -> result.get("oral").get.asOpt[List[String]].get)
            product = Map("product" -> result.get("product").get.asOpt[List[String]].get)

            result must_!= ""
        }

    def testCase3_15 =
        WsTestClient.withClient{client=>
            val lists = List(atc_one)
            val date = testUtil.timeArrInstance(endYear,endMonth,"year",gap)
            val conditions = testUtil.getConditions(lists,date)
            val listStr = urlList.map{ url =>
                val res = twoConditionCombination(client,url,token,conditions,time_out)
                res
            }
            val result = testUtil.listStrToStr(listStr,time_out)
            result must_== "ok"
        }
    def testCase3_15_search =
        WsTestClient.withClient { client =>
            val lists = List(atc_one)
            val date = testUtil.timeArrInstance(endYear,endMonth,"year",gap)
            val conditions = testUtil.getConditions(lists,date)
            val resList = new PICClient(client, "http://127.0.0.1:9000").conditionSearchResult(token, conditions, skip, contains)
            val result = testUtil.listFutureToString(resList,time_out)
            result must_== "ok"
        }
    def testCase3_16 =
        WsTestClient.withClient{client=>
            val lists = List(atc_one)
            val date = testUtil.timeArrInstance(endYear,endMonth,"month",gap)
            val conditions = testUtil.getConditions(lists,date)
            val listStr = urlList.map{ url =>
                val res = twoConditionCombination(client,url,token,conditions,time_out)
                res
            }
            val result = testUtil.listStrToStr(listStr,time_out)
            result must_== "ok"
        }
    def testCase3_16_search =
        WsTestClient.withClient { client =>
            val lists = List(atc_one)
            val date = testUtil.timeArrInstance(endYear,endMonth,"month",gap)
            val conditions = testUtil.getConditions(lists,date)
            val resList = new PICClient(client, "http://127.0.0.1:9000").conditionSearchResult(token, conditions, skip, contains)
            val result = testUtil.listFutureToString(resList,time_out)
            result must_== "ok"
        }


    def testCase3_17 =
        WsTestClient.withClient{client=>
            val lists = List(atc_two)
            val date = testUtil.timeArrInstance(endYear,endMonth,"year",gap)
            val conditions = testUtil.getConditions(lists,date)
            val listStr = urlList.map{ url =>
                val res = twoConditionCombination(client,url,token,conditions,time_out)
                res
            }
            val result = testUtil.listStrToStr(listStr,time_out)
            result must_== "ok"
        }
    def testCase3_17_search =
        WsTestClient.withClient { client =>
            val lists = List(atc_two)
            val date = testUtil.timeArrInstance(endYear,endMonth,"year",gap)
            val conditions = testUtil.getConditions(lists,date)
            val resList = new PICClient(client, "http://127.0.0.1:9000").conditionSearchResult(token, conditions, skip, contains)
            val result = testUtil.listFutureToString(resList,time_out)
            result must_== "ok"
        }
    def testCase3_18 =
        WsTestClient.withClient{client=>
            val lists = List(atc_two)
            val date = testUtil.timeArrInstance(endYear,endMonth,"month",gap)
            val conditions = testUtil.getConditions(lists,date)
            val listStr = urlList.map{ url =>
                val res = twoConditionCombination(client,url,token,conditions,time_out)
                res
            }
            val result = testUtil.listStrToStr(listStr,time_out)
            result must_== "ok"
        }
    def testCase3_18_search =
        WsTestClient.withClient { client =>
            val lists = List(atc_two)
            val date = testUtil.timeArrInstance(endYear,endMonth,"month",gap)
            val conditions = testUtil.getConditions(lists,date)
            val resList = new PICClient(client, "http://127.0.0.1:9000").conditionSearchResult(token, conditions, skip, contains)
            val result = testUtil.listFutureToString(resList,time_out)
            result must_== "ok"
        }

    def testCase3_19 =
        WsTestClient.withClient{client=>
            val lists = List(atc_three)
            val date = testUtil.timeArrInstance(endYear,endMonth,"year",gap)
            val conditions = testUtil.getConditions(lists,date)
            val listStr = urlList.map{ url =>
                val res = twoConditionCombination(client,url,token,conditions,time_out)
                res
            }
            val result = testUtil.listStrToStr(listStr,time_out)
            result must_== "ok"
        }
    def testCase3_19_search =
        WsTestClient.withClient { client =>
            val lists = List(atc_three)
            val date = testUtil.timeArrInstance(endYear,endMonth,"year",gap)
            val conditions = testUtil.getConditions(lists,date)
            val resList = new PICClient(client, "http://127.0.0.1:9000").conditionSearchResult(token, conditions, skip, contains)
            val result = testUtil.listFutureToString(resList,time_out)
            result must_== "ok"
        }
    def testCase3_20 =
        WsTestClient.withClient{client=>
            val lists = List(atc_three)
            val date = testUtil.timeArrInstance(endYear,endMonth,"month",gap)
            val conditions = testUtil.getConditions(lists,date)
            val listStr = urlList.map{ url =>
                val res = twoConditionCombination(client,url,token,conditions,time_out)
                res
            }
            val result = testUtil.listStrToStr(listStr,time_out)
            result must_== "ok"
        }
    def testCase3_20_search =
        WsTestClient.withClient { client =>
            val lists = List(atc_three)
            val date = testUtil.timeArrInstance(endYear,endMonth,"month",gap)
            val conditions = testUtil.getConditions(lists,date)
            val resList = new PICClient(client, "http://127.0.0.1:9000").conditionSearchResult(token, conditions, skip, contains)
            val result = testUtil.listFutureToString(resList,time_out)
            result must_== "ok"
        }



    def testCase3_21 =
    WsTestClient.withClient{client=>
        val lists = List(atc_three,oral)
        val date = testUtil.timeArrInstance(endYear,endMonth,"year",gap)
        val conditions = testUtil.getConditions(lists,date)
        val listStr = urlList.map{ url =>
            val res = twoConditionCombination(client,url,token,conditions,time_out)
            res
        }
        val result = testUtil.listStrToStr(listStr,time_out)
        result must_== "ok"
    }

    def testCase3_21_search =
    WsTestClient.withClient { client =>
        val lists = List(atc_three,oral)
        val date = testUtil.timeArrInstance(endYear,endMonth,"year",gap)
        val conditions = testUtil.getConditions(lists,date)

        val resList = new PICClient(client, "http://127.0.0.1:9000").conditionSearchResult(token, conditions, skip, contains)

        val result = testUtil.listFutureToString(resList,time_out)

        result must_== "ok"
    }
    def testCase3_22 =
    WsTestClient.withClient{client=>
        val lists = List(atc_three,oral)
        val date = testUtil.timeArrInstance(endYear,endMonth,"month",gap)
        val conditions = testUtil.getConditions(lists,date)
        val listStr = urlList.map{ url =>
            val res = twoConditionCombination(client,url,token,conditions,time_out)
            res
        }
        val result = testUtil.listStrToStr(listStr,time_out)
        result must_== "ok"
    }

    def testCase3_22_search =
    WsTestClient.withClient { client =>
        val lists = List(atc_three,oral)
        val date = testUtil.timeArrInstance(endYear,endMonth,"month",gap)
        val conditions = testUtil.getConditions(lists,date)

        val resList = new PICClient(client, "http://127.0.0.1:9000").conditionSearchResult(token, conditions, skip, contains)

        val result = testUtil.listFutureToString(resList,time_out)

        result must_== "ok"
    }

    def testCase3_23 =
    WsTestClient.withClient{client=>
        val lists = List(atc_three,product)
        val date = testUtil.timeArrInstance(endYear,endMonth,"year",gap)
        val conditions = testUtil.getConditions(lists,date)
        val listStr = urlList.map{ url =>
            val res = twoConditionCombination(client,url,token,conditions,time_out)
            res
        }
        val result = testUtil.listStrToStr(listStr,time_out)
        result must_== "ok"
    }

    def testCase3_23_search =
    WsTestClient.withClient { client =>
        val lists = List(atc_three,product)
        val date = testUtil.timeArrInstance(endYear,endMonth,"year",gap)
        val conditions = testUtil.getConditions(lists,date)

        val resList = new PICClient(client, "http://127.0.0.1:9000").conditionSearchResult(token, conditions, skip, contains)

        val result = testUtil.listFutureToString(resList,time_out)

        result must_== "ok"
    }

    def testCase3_24 =
    WsTestClient.withClient{client=>
        val lists = List(atc_three,product)
        val date = testUtil.timeArrInstance(endYear,endMonth,"month",gap)
        val conditions = testUtil.getConditions(lists,date)
        val listStr = urlList.map{ url =>
            val res = twoConditionCombination(client,url,token,conditions,time_out)
            res
        }
        val result = testUtil.listStrToStr(listStr,time_out)
        result must_== "ok"
    }

    def testCase3_24_search =
    WsTestClient.withClient { client =>
        val lists = List(atc_three,product)
        val date = testUtil.timeArrInstance(endYear,endMonth,"month",gap)
        val conditions = testUtil.getConditions(lists,date)

        val resList = new PICClient(client, "http://127.0.0.1:9000").conditionSearchResult(token, conditions, skip, contains)

        val result = testUtil.listFutureToString(resList,time_out)

        result must_== "ok"
    }

    def testCase3_25 =
    WsTestClient.withClient{client=>
        val lists = List(atc_three,product,edge)
        val date = testUtil.timeArrInstance(endYear,endMonth,"year",gap)
        val conditions = testUtil.getConditions(lists,date)
        val listStr = urlList.map{ url =>
            val res = twoConditionCombination(client,url,token,conditions,time_out)
            res
        }
        val result = testUtil.listStrToStr(listStr,time_out)
        result must_== "ok"
    }

    def testCase3_25_search =
    WsTestClient.withClient { client =>
        val lists = List(atc_three,product,edge)
        val date = testUtil.timeArrInstance(endYear,endMonth,"year",gap)
        val conditions = testUtil.getConditions(lists,date)

        val resList = new PICClient(client, "http://127.0.0.1:9000").conditionSearchResult(token, conditions, skip, contains)

        val result = testUtil.listFutureToString(resList,time_out)

        result must_== "ok"
    }

    def testCase3_26 =
    WsTestClient.withClient{client=>
        val lists = List(atc_three,product,edge)
        val date = testUtil.timeArrInstance(endYear,endMonth,"month",gap)
        val conditions = testUtil.getConditions(lists,date)
        val listStr = urlList.map{ url =>
            val res = twoConditionCombination(client,url,token,conditions,time_out)
            res
        }
        val result = testUtil.listStrToStr(listStr,time_out)
        result must_== "ok"
    }

    def testCase3_26_search =
    WsTestClient.withClient { client =>
        val lists = List(atc_three,product,edge)
        val date = testUtil.timeArrInstance(endYear,endMonth,"month",gap)
        val conditions = testUtil.getConditions(lists,date)

        val resList = new PICClient(client, "http://127.0.0.1:9000").conditionSearchResult(token, conditions, skip, contains)

        val result = testUtil.listFutureToString(resList,time_out)

        result must_== "ok"
    }

    def testCase3_27 =
    WsTestClient.withClient{client=>
        val lists = List(atc_three,product,edge,manufacture_name)
        val date = testUtil.timeArrInstance(endYear,endMonth,"year",gap)
        val conditions = testUtil.getConditions(lists,date)
        val listStr = urlList.map{ url =>
            val res = twoConditionCombination(client,url,token,conditions,time_out)
            res
        }
        val result = testUtil.listStrToStr(listStr,time_out)
        result must_== "ok"
    }

    def testCase3_27_search =
    WsTestClient.withClient { client =>
        val lists = List(atc_three,product,edge,manufacture_name)
        val date = testUtil.timeArrInstance(endYear,endMonth,"year",gap)
        val conditions = testUtil.getConditions(lists,date)

        val resList = new PICClient(client, "http://127.0.0.1:9000").conditionSearchResult(token, conditions, skip, contains)

        val result = testUtil.listFutureToString(resList,time_out)

        result must_== "ok"
    }

    def testCase3_28 =
    WsTestClient.withClient{client=>
        val lists = List(atc_three,product,edge,manufacture_name)
        val date = testUtil.timeArrInstance(endYear,endMonth,"month",gap)
        val conditions = testUtil.getConditions(lists,date)
        val listStr = urlList.map{ url =>
            val res = twoConditionCombination(client,url,token,conditions,time_out)
            res
        }
        val result = testUtil.listStrToStr(listStr,time_out)
        result must_== "ok"
    }

    def testCase3_28_search =
    WsTestClient.withClient { client =>
        val lists = List(atc_three,product,edge,manufacture_name)
        val date = testUtil.timeArrInstance(endYear,endMonth,"month",gap)
        val conditions = testUtil.getConditions(lists,date)

        val resList = new PICClient(client, "http://127.0.0.1:9000").conditionSearchResult(token, conditions, skip, contains)

        val result = testUtil.listFutureToString(resList,time_out)

        result must_== "ok"
    }

    def twoConditionCombination(client:WSClient,conditionSearchUrl:String,token:String,lists:List[Map[String,JsValue]] ,time_out:Int): String ={
        val resArr=lists.map{condition=>
            val res=Await.result(
                new PICClient(client, "http://127.0.0.1:9000").oneCondition(token,condition,conditionSearchUrl), 10.seconds)
            res
        }.toArray
        testUtil.finalResult(resArr)
    }

}
