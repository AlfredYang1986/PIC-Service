package bminjection.db

import bmutil.dao.from
import com.mongodb.casbah.Imports._
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

/**
  * Created by yym on 7/12/17.
  */
object LoadConfig {
    private val d2m : DBObject => Map[String, JsValue] = { obj =>
        def convertJsValue(lst: List[BasicDBObject]): JsValue = {
        
            toJson(lst.map { x =>
                Map("level" -> toJson(x.getAs[Number]("level").map(x => x.intValue()).getOrElse(throw new Exception("index error"))),
                    "parent" -> toJson(x.getAs[String]("parent").map(x => x).getOrElse(throw new Exception("index error"))),
                    "def" -> toJson(x.getAs[String]("def").map(x => x).getOrElse(throw new Exception("index error"))),
                    "des" -> toJson(x.getAs[String]("des").map(x => x).getOrElse(throw new Exception("index error")))
                )
            
            })
        }
        Map(
            "index"->toJson(obj.getAs[String]("index").map(x=>x).getOrElse(throw new Exception("index error"))),
            "province"->toJson(obj.getAs[List[String]]("province").map(x=>x).getOrElse(throw new Exception("province error"))),
            "category"-> convertJsValue(obj.getAs[List[BasicDBObject]]("category").map(x => x).get),
            "manufacture"->toJson(obj.getAs[List[String]]("manufacture").get),
            "product_type"->toJson(obj.getAs[List[String]]("product_type").get),
            "specifications"->toJson(obj.getAs[List[String]]("specifications").get),
            "package"->toJson(obj.getAs[List[String]]("package").get)
        
        )
    }
    
    lazy val config: Option[List[Map[String, JsValue]]] = Some((from db() in "config").select(x => d2m(x)).toList)
    
    
}
