import java.text.SimpleDateFormat
import java.util.Calendar

import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.Await
import scala.concurrent.duration._

import scala.collection.mutable.ArrayBuffer

/**
  * Created by yym on 7/10/17.
  */
object testUtil {
    /**
      *
      * @param endYear 截止年
      * @param endMonth 截止月
      * @param timeType 间隔方式：以年计=>"year";以月计=>"month"
      */
    def timeGenerator(endYear:Int=2016,endMonth:Int=12,timeType:String="year"): (String,JsValue)={
    
        var end :String=""
        var start:String=""
        if(timeType=="month") {
                val sdf = new SimpleDateFormat("yyyyMM")
                val cal = Calendar.getInstance()
                cal.setTime(sdf.parse(endYear+""+endMonth))
                end = sdf.format(cal.getTime)
                cal.add(Calendar.MONTH,-1)
                start = sdf.format(cal.getTime)
        }else {
            val sdf = new SimpleDateFormat("yyyyMM")
            val cal = Calendar.getInstance()
            cal.setTime(sdf.parse(endYear + ""+endMonth))
            end = sdf.format(cal.getTime)
            cal.add(Calendar.YEAR, -1)
            start = sdf.format(cal.getTime)
        }
        ("date",toJson(Map("start"->start, "end"->end)))
        
    }
    def timeArrInstance(endYear:Int=2016,endMonth:Int=12,timeType:String="year",gap:Int=1): List[(String,JsValue)] ={
        val arr: ArrayBuffer[(String,JsValue)] = new ArrayBuffer[(String,JsValue)]()
        if(timeType=="month"){
            for(i<-0 to gap*12) {
                val sdf = new SimpleDateFormat("yyyyMM")
                val cal = Calendar.getInstance()
                cal.setTime(sdf.parse(endYear + "" + endMonth))
                cal.add(Calendar.MONTH, -i)
                val res = timeGenerator(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), timeType)
                arr.append(res)
            }
            
        }else{
            for(i<-0 to gap){
                val sdf = new SimpleDateFormat("yyyyMM")
                val cal = Calendar.getInstance()
                cal.setTime(sdf.parse(endYear+""+endMonth))
                cal.add(Calendar.YEAR,-i)
                val res=timeGenerator(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),timeType)
                arr.append(res)
            }
        }
        
        arr.toList
    }
    def twoCondition(time:List[(String,JsValue)],another:List[(String,JsValue)]):List[((String,JsValue),(String,JsValue))]={
        val arr:ArrayBuffer[((String,JsValue),(String,JsValue))]=new ArrayBuffer[((String,JsValue),(String,JsValue))]
        time.map{v1=>
            another.map{v2=>
                val res=(v1,v2)
                arr.append(res)
                res
            }
            
        }
        arr.toList
    }
    def resultHandling(response:WSResponse,requestJs:JsValue): String ={
        var info="Error!"
        val res=(response.json \ "status").asOpt[String].get
        if(res=="ok"){
            info=res
        }else{
            info+=(requestJs \ "condition")+"->"+(response.json \ "error" \ "message")+"\n"
        }
        info
    }
    def finalResult(resArr:Array[String]): String ={
        var info=""
        resArr.foreach{r=>
            if(r!="ok"){
                info+=r
            }else{
                info="ok"
            }
        }
        info
    }
   
    
    

    
//    def resultHandling(resArr:Array[String]):String={
//        var info="ok"
//        resArr.foreach{r=>
//            if(r!="ok"){
//                info="Error"
//            }
//        }
//        info
//    }
    
}
