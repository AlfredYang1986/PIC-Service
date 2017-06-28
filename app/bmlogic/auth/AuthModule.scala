package bmlogic.auth

import java.util.Date

import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import AuthMessage._
import bminjection.db.DBTrait
import bminjection.token.AuthTokenTrait
import bmlogic.auth.AuthData.AuthData
import bmlogic.common.sercurity.Sercurity
import bmmessages.MessageDefines
import bmmessages.CommonModules
import bmpattern.ModuleTrait
import bmutil.errorcode.ErrorCode

import scala.collection.immutable.Map
import com.mongodb.casbah.Imports._

object AuthModule extends ModuleTrait with AuthData {

	def dispatchMsg(msg : MessageDefines)(pr : Option[Map[String, JsValue]])(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = msg match {
        case msg_AuthPushUser(data) => authPushUser(data)
		case msg_AuthWithPassword(data) => authWithPassword(data)
        case msg_AuthTokenParser(data) => authTokenPraser(data)
        
        case msg_CheckAuthTokenTest(data) => checkAuthTokenTest(data)(pr)
        case msg_CheckTokenExpire(data) => checkAuthTokenExpire(data)(pr)
        case msg_CheckEdgeScope(data) => checkEdgeScope(data)(pr)
        case msg_CheckProductLevelScope(data) => checkProductLevelScope(data)(pr)
        case msg_CheckManufactureNameScope(data) => checkManufactureNameScope(data)(pr)
        case msg_CheckManufactureTypeScope(data) => checkAuthTokenTest(data)(pr)

		case _ => ???
	}

    def authPushUser(data : JsValue)(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))
            val att = cm.modules.get.get("att").map (x => x.asInstanceOf[AuthTokenTrait]).getOrElse(throw new Exception("no encrypt impl"))

            val date = new Date().getTime
            val o : DBObject = data
            val user_name = (data \ "user_name").asOpt[String].map (x => x).getOrElse(throw new Exception("input error"))
            val pwd = (data \ "pwd").asOpt[String].map (x => x).getOrElse(throw new Exception("input error"))
            o += "user_id" -> Sercurity.md5Hash(user_name + pwd + Sercurity.getTimeSpanWithMillSeconds)
            o += "date" -> date.asInstanceOf[Number]

            db.insertObject(o, "users", "user_name")
            val result = toJson(o - "pwd" - "phoneNo" - "email" - "date" + ("expire_in" -> toJson(date + 60 * 60 * 1000 * 24))) // token 默认一天过期
            val auth_token = att.encrypt2Token(toJson(result))
            val reVal = toJson(o - "user_id" - "pwd" - "phoneNo" - "email" - "date" - "scope")

            (Some(Map(
                "auth_token" -> toJson(auth_token),
                "user" -> reVal
            )), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def authWithPassword(data : JsValue)(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
		try {
			val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))
			val user_name = (data \ "user_name").asOpt[String].map (x => x).getOrElse(throw new Exception("input error"))
			val pwd = (data \ "pwd").asOpt[String].map (x => x).getOrElse(throw new Exception("input error"))
			val result = db.queryObject($and("user_name" -> user_name, "pwd" -> pwd), "users")
            val date = new Date().getTime
            if (result.isEmpty) throw new Exception("unkonw error")
			else {
                
                val att = cm.modules.get.get("att").map (x => x.asInstanceOf[AuthTokenTrait]).getOrElse(throw new Exception("no encrypt impl"))
                val reVal = result.get + ("expire_in" -> toJson(date + 60 * 60 * 1000 * 24))
                val auth_token = att.encrypt2Token(toJson(reVal))

                (Some(Map(
                    "auth_token" -> toJson(auth_token),
                    "user" -> toJson(result.get)
                )), None)
            }
		} catch {
			case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
		}
    }

    def queryUser(data : JsValue)(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            val db = cm.modules.get.get("db").map (x => x.asInstanceOf[DBTrait]).getOrElse(throw new Exception("no db connection"))

            val user_id = (data \ "conditions" \ "user_id").asOpt[String].getOrElse(throw new Exception("input error"))
            val result = db.queryObject(DBObject("user_id" -> user_id), "users")
            if (result.isEmpty) throw new Exception("unkonw error")
            else (Some(result.get), None)
            

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def authTokenPraser(data : JsValue)(implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {
        try {
            println("---Im in authTokenPraser---")
            val att = cm.modules.get.get("att").map (x => x.asInstanceOf[AuthTokenTrait]).getOrElse(throw new Exception("no encrypt impl"))

            val auth_token = (data \ "token").asOpt[String].map (x => x).getOrElse(throw new Exception("input error"))
            val auth = att.decrypt2JsValue(auth_token)
            (Some(Map("auth" -> auth)), None)

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def checkAuthTokenTest(data : JsValue)
                          (pr : Option[Map[String, JsValue]])
                          (implicit cm : CommonModules): (Option[Map[String, JsValue]], Option[JsValue]) = {
//        println(pr)
        (pr, None)
    }

    def checkEdgeScope(data : JsValue)
                      (pr : Option[Map[String, JsValue]])
                      (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val auth = pr.map (x => x.get("auth").get).getOrElse(throw new Exception("token parse error"))
            val edge_lst = (auth \ "scope" \ "edge").asOpt[List[String]].map (x => x.distinct.sorted).getOrElse(throw new Exception("token parse error"))

            (data \ "condition" \ "edge").asOpt[List[String]].map { x =>

                val edge_condition = x.distinct.sorted
                var result = pr.get
                var edges : List[String] = Nil

                if (edge_lst.isEmpty) {
                    edges = edge_condition
                } else {
                    edge_condition.foreach { x =>
                        if (edge_lst.contains(x))
                            edges = x :: edges
                        else
                            result = result + ("Warning" -> toJson("没有区域搜索的全权限，请联系你的管理员添加"))
                    }
                }

                if (!edges.isEmpty) {
                    result = result + ("search_edge_condition" -> toJson(edges))
                }
                (Some(result), None)

            }.getOrElse((Some(pr.get), None))

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def checkProductLevelScope(data : JsValue)
                              (pr : Option[Map[String, JsValue]])
                              (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val auth = pr.map (x => x.get("auth").get).getOrElse(throw new Exception("token parse error"))
            val product_level = (auth \ "scope" \ "category").asOpt[List[String]].
                map (x => x).getOrElse(throw new Exception("token parse error"))

            var result = pr.get
            // val lst = (data \ "condition" \ "category").asOpt[List[String]].map (x => x).getOrElse(Nil)
            val lst = (data \ "condition" \ "category").asOpt[String].map (x => x).getOrElse("") :: Nil
            val category_lst = productLevel2Category(lst).distinct.sorted
            val auth_cat_lst = productLevel2Category(product_level).distinct.sorted
            val reVal = if (auth_cat_lst.isEmpty) category_lst
                        else category_lst.takeWhile(auth_cat_lst.contains(_))
            if (!reVal.isEmpty)
                result = result + ("search_category_condition" -> toJson(reVal))
            else if (reVal.isEmpty && !auth_cat_lst.isEmpty)
                result = result + ("Warning" -> toJson("没有药品类别搜索的全权限，请联系你的管理员添加"))
//                    ("search_category_condition" -> toJson(auth_cat_lst))
            else Unit
//            result = result + ("Warning" -> toJson("没有搜索的全权限，请联系你的管理员添加"))
            (Some(result), None)
        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def checkManufactureNameScope(data : JsValue)
                                 (pr : Option[Map[String, JsValue]])
                                 (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            val auth = pr.map (x => x.get("auth").get).getOrElse(throw new Exception("token parse error"))
            val name_lst = (auth \ "scope" \ "manufacture_name").asOpt[List[String]].map (x => x.distinct.sorted).getOrElse(throw new Exception("token parse error"))
            (data \ "condition" \ "manufacture_name").asOpt[List[String]].map { x =>
                val name_condition = x.distinct.sorted
                var result = pr.get
                var names : List[String] = Nil

                if (name_lst.isEmpty) {
                    names = name_condition
                } else {
                    name_condition.foreach { x =>
                        if (name_lst.contains(x))
                            names = x :: names
                        else
                            result = result + ("Warning" -> toJson("没有公司名称搜索的全权限，请联系你的管理员添加"))
                    }
                }

                if (!names.isEmpty) {
                    result = result + ("search_manufacture_name_condition" -> toJson(names))
                }
                (Some(result), None)

            }.getOrElse((Some(pr.get), None))

        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }

    def checkAuthTokenExpire(data : JsValue)
                            (pr : Option[Map[String, JsValue]])
                            (implicit cm : CommonModules) : (Option[Map[String, JsValue]], Option[JsValue]) = {

        try {
            println("---Im in checkAuthTokenExpire---")
            val auth = pr.map (x => x.get("auth").get).getOrElse(throw new Exception("token parse error"))
            val expire_in = (auth \ "expire_in").asOpt[Long].map (x => x).getOrElse(throw new Exception("token parse error"))

            if (new Date().getTime > expire_in) throw new Exception("token expired")
            else (pr, None)
        } catch {
            case ex : Exception => (None, Some(ErrorCode.errorToJson(ex.getMessage)))
        }
    }
}