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
    def writeJson(lsts:List[List[Map[String,JsValue]]],conditionNum:String,test_case:List[String],token:Map[String,JsValue],flag:Boolean): Unit ={
        val file  = new File(".")
        val absolutePath = file.getAbsolutePath()
        val filePath:String=absolutePath+"//test//JsonFile//"+conditionNum+".txt"
        val out=new FileOutputStream(filePath,true)
        val writer=new PrintWriter(out)
        lsts.map(lst=>lst.map{x=>
            val condition= toJson(x)
//            println(toJson(token.+("condition" -> condition)))
            toJson(token.+("condition" -> condition))
        })
//        println(lsts)
        println(test_case.length)
        val testJs:ArrayBuffer[JsValue]=new ArrayBuffer[JsValue]()
        for(i<-0 to test_case.length){
           val t=toJson(Map(test_case(i)->lsts(i)))
            println(t)
            testJs.append(t)
        }
        println(testJs.head)
        writer.println(toJson(Map("conditions"->testJs.toArray)).toString())
        writer.close()
    }
    def readJson(conditionNum:String,test_case:String): Array[JsValue] = {
        val file = new File(".")
        val absolutePath = file.getAbsolutePath()
        val filePath = absolutePath + "//test//JsonFile//" + conditionNum + ".txt"
        val lines = Source.fromFile(filePath).getLines()
        val jsArr:ArrayBuffer[JsValue]=new ArrayBuffer[JsValue]
        while (lines.hasNext){
            val js=lines.next()
            jsArr.append(Json.parse(js))
        }
        (jsArr.head \ "conditions" \ test_case).as[Array[JsValue]]
    }
    
}
