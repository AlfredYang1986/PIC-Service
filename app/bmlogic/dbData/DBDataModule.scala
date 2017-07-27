package bmlogic.dbData

import java.util.UUID

import bminjection.db.DBTrait
import bmlogic.dbData.DBDataMessage.msg_rawData2DB
import bmmessages.{CommonModules, MessageDefines}
import bmpattern.ModuleTrait
import bmutil.ExcelReader.JavaBean.RawData
import bmutil.ExcelReader.RawDataReader
import com.mongodb.casbah.Imports.MongoDBObject
import play.api.libs.json.JsValue

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
            objs.foreach{obj =>
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
                db.insertObject(build.result(),"rawData", "rid")
            }
        }
        null
    }
}
