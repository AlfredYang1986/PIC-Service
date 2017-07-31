package bmlogic.dbData

import java.util.UUID

import bminjection.db.DBTrait
import bminjection.db.LoadConfig.d2m
import bmlogic.dbData.DBDataMessage.msg_rawData2DB
import bmmessages.{CommonModules, MessageDefines}
import bmpattern.ModuleTrait
import bmutil.ExcelReader.JavaBean.RawData
import bmutil.ExcelReader.RawDataReader
import bmutil.dao.from
import bmutil.errorcode.ErrorCode
import com.mongodb.casbah.Imports.MongoDBObject
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

import bmlogic.dbData.DataStructure
/**
  * Created by yym on 7/27/17.
  */
object DBDataModule extends ModuleTrait{
    def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_rawData2DB(data) => rawData2DB(data)
        case _ => ???
    }
    def rawData2DB(data:JsValue)(implicit cm : CommonModules): (Option[Map[String, JsValue]], Option[JsValue]) ={
        try{
            val db=cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))
            val data=RawDataReader.read("")
            val objs=data.asInstanceOf[List[RawData]]
            objs.map{obj =>
                val build = MongoDBObject.newBuilder
                build += "rid" ->  UUID.randomUUID()
                build += "GenericName" -> obj.getGenericName
                build += "CompanyName" -> obj.getCompanyName
                build += "Year" -> obj.getYear
                build += "SalesAmount" -> obj.getSalesAmount
                build += "Quantity" -> obj.getSpecification
                build += "Specification" -> obj.getSpecification
                build += "Formulation" -> obj.getFormulation
                build += "Quarter" -> obj.getQuarter
                build += "SinglePackage" -> obj.getSinglePackage
                build += "ROA" -> obj.getROA
                build += "TherapyMicro" -> obj.getTherapyMicro
                build += "TherapyWide" -> obj.getTherapyWide
                build += "City" -> obj.getCity
                db.insertObject(build.result(),"raw_data", "rid")
                ""
            }
            (None,Some(toJson(Map("status" -> toJson("Insert OK")))))
        }catch {
            
            case ex: Exception =>
                (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
        
    }
    
//    def readRawData(data:JsValue)(implicit cm : CommonModules): (Option[Map[String, JsValue]], Option[JsValue])={
//        val db=cm.modules.get.get("db").map(x =>x).asInstanceOf[DBTrait]
//        val data : List[Map[String, JsValue]]=(from db() in "rowData")
//    }
}
