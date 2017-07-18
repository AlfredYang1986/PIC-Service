package Utils

import bminjection.db.LoadConfig._
import play.api.libs.json.Json.toJson
import bmlogic.category.CategoryModule
import play.api.libs.json.JsValue

import scala.collection.immutable.Map
/**
  * Created by yym on 7/12/17.
  */
object ConditionUtil{
    //改错 atc_two
    def chooseCondition(condition_name:String): Map[String,List[String]]={
        val tmp=toJson(Map(""->""))
        val category=CategoryModule.Category(tmp)._1.get
//        category.foreach(x=>println(x))
        lazy val configs=config.get
        val configMap=configs.reduce{(a,b) => a.+(b.keys.head -> b.get(b.keys.head).get) }
//        configMap.foreach(x=>println(x))
        var value : Map[String,List[String]] = null
        condition_name match
        {
            case "atc_one" =>
                value=Map("category"->category.get("atc_one").get.as[List[String]])
            case "atc_two" =>
                value=Map("category"->category.get("atc_tow").get.as[List[String]])
            case "atc_three" =>
                value=Map("category"->category.get("atc_three").get.as[List[String]])
            case "oral_name" =>
                value=Map("oral_name"->category.get("oral").get.as[List[String]])
            case "product_name" =>
                value=Map("product_name"->category.get("product").get.as[List[String]])
            case "package" =>
                value=Map("package" -> configMap.get("package").get.as[List[String]])
            case "specifications" =>
                value=Map("specifications" -> configMap.get("specifications").get.as[List[String]])
            case "manufacture" =>
                value=Map("manufacture" -> configMap.get("manufacture").get.as[List[String]])
            case "product_type" =>
                value=Map("product_type" -> configMap.get("product_type").get.as[List[String]])
            case "province" =>
                value=Map("edge" -> configMap.get("province").get.as[List[String]])
            case "manufacture_type" =>
               value=Map("manufacture_type"->List("内资","合资"))
            case _ => ???
        }
        value
    }
    def listToMatrixJsMap (lists: List[Map[String,List[String]]]) : List[Map[String,JsValue]] = {
        val head = lists.head
        val tail = lists.tail
        
        val head_key = head.keys.head
        var result : List[Map[String,JsValue]] = head.get(head_key).map(x => x.map(y => Map(head_key -> toJson(y)))).get
        var result_temp : List[Map[String,JsValue]] = Nil
        var result_final : List[Map[String,JsValue]] = Nil
        
        for (x <- tail){
            val key : String = x.keys.head
            
            for (y <- x.get(key).get){
                result_temp = result
                result = result.map(z => z.+(key -> toJson(y)))
                result_final = result_final ::: result
                result = result_temp
            }
            result = result_final
            result_final = Nil
        }
        result
    }
    def getConditions(lists: List[Map[String,List[String]]],date : List[(String,JsValue)]) : List[Map[String,JsValue]] = {
        var conditions = listToMatrixJsMap(lists)
        conditions = conditions.map(x => x.+(date.head._1 -> date.head._2))
        for (d <- date.tail){
            conditions = conditions ::: conditions.map(x => x.+(d._1 -> d._2))
        }
        conditions
    }
    
    /**
      *
      * @param condition：11个
      *                 atc_one ， atc_two，atc_three，oral_name，product_name，package，specifications，manufacture，product_type，province，manufacture_type
      * @param date
      * @return
      */
    def one_condition_with_time(condition:String,date : List[(String,JsValue)]): List[Map[String,JsValue]] ={
        val res=chooseCondition(condition)::Nil
        getConditions(res,date)
    }
    def two_condition_with_time(condition_one:String,condition_two:String,date : List[(String,JsValue)]): List[Map[String,JsValue]] ={
        val res=chooseCondition(condition_one)::chooseCondition(condition_two)::Nil
        getConditions(res,date)
    }
    def three_condition_with_time(condition_one:String,condition_two:String,condition_three:String,date : List[(String,JsValue)]): List[Map[String,JsValue]] ={
        val res=chooseCondition(condition_one)::chooseCondition(condition_two)::Nil
        getConditions(res,date)
    }
  
    
}
