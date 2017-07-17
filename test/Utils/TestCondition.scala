package Utils


import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

import scala.collection.immutable.Map

/**
  * Created by yym on 7/14/17.
  */
case class TestCondition(test_name:List[String],test_condition:List[List[String]],conditionNum:Int)
object TestCondition {
    def condition(condition:List[String],conditionNum:Int,endYear:Int=2016,endMonth:Int=12,timeType:String="year",gap:Int=1): List[Map[String,JsValue]] ={
        val time=TimeUtil.timeArrInstance(endYear,endMonth,timeType,gap)
        var c:List[Map[String,JsValue]]=List(Map(""->toJson("")))
        conditionNum match {
            case 1 =>
                c = ConditionUtil.one_condition_with_time(condition.head, time)
                println(c)
            case 2 =>
                c = ConditionUtil.two_condition_with_time(condition(0), condition(1), time)
            case 3 =>
                c = ConditionUtil.three_condition_with_time(condition(0), condition(1), condition(2), time)
    
        }
        c
    }
}
