package Utils

import java.io._

import play.api.libs.json.JsValue
import play.api.libs.json.Json.{asciiStringify, toJson}
import play.api.libs.json._

import scala.collection.immutable.Map
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * Created by yym on 7/13/17.
  */
object JsonFileUtil {
    def writeJson(lsts:List[(String,List[Map[String,JsValue]])],conditionNum:String,flag:Boolean): Unit ={
        val file  = new File(".")
        val absolutePath = file.getAbsolutePath()
        val filePath:String=absolutePath+"//test//JsonFile//"+conditionNum+".txt"
        val out=new FileOutputStream(filePath,true)
        val writer=new PrintWriter(out)
        val res=lsts.map {lst =>
            val t = lst._2.map { x =>
                val condition = toJson(x)
                //              println(toJson(token.+("condition" -> condition)))
                toJson(Map("condition" -> condition))
            }
            (lst._1 , t)
        }
        var conMap=Map("pic"->toJson("test"))
        println(res.length)
        for(data<-res){
            conMap=conMap.+(data._1 -> toJson(data._2))
        }
        val conditions=toJson(Map("conditions" -> toJson(conMap)))
        writer.println(conditions)
        writer.close()
    }
    def readJson(conditionNum:String,test_case:String): List[Map[String,JsValue]] = {
        val file = new File(".")
        val absolutePath = file.getAbsolutePath()
        val filePath = absolutePath + "//test//JsonFile//" + conditionNum + ".txt"
        val lines = Source.fromFile(filePath).getLines()
        val jsArr:ArrayBuffer[JsValue]=new ArrayBuffer[JsValue]
        while (lines.hasNext){
            val js=lines.next()
            jsArr.append(Json.parse(js))
        }

        val conArr=(jsArr.head \ "conditions" \ test_case).as[List[JsValue]]
        val res_map=conArr.map{x =>
            val v=(x \ "condition").get
            val k="condition"
            println(Map(k -> v))
            Map(k -> v)
        }
        res_map
    }
    
}
