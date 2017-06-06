package bmutil.errorcode

import play.api.libs.json.Json
import play.api.libs.json.Json._
import play.api.libs.json.JsValue

object ErrorCode {
  	case class ErrorNode(name : String, code : Int, message : String)

  	private def xls : List[ErrorNode] = List(
  		new ErrorNode("input error", -1, "输入的参数有错误"),

		new ErrorNode("get primary key error", -101, "获取主健健值失败"),
		new ErrorNode("primary key error", -102, "主健重复创建或者主键出错"),
		new ErrorNode("data not exist", -103, "数据不存在"),
		new ErrorNode("data duplicate", -104, "搜索结果不唯一，用query multiple搜索"),

		new ErrorNode("no db connection", -901, "没找到数据库链接"),
		new ErrorNode("db prase error", -902, "数据库结构发现错误"),
		new ErrorNode("no encrypt impl", -903, "权限加密方式不清晰或者Token不存在"),
		new ErrorNode("token parse error", -904, "token数据解析出现错误"),

  		new ErrorNode("unknown error", -999, "unknown error")
  	)
  
  	def getErrorCodeByName(name : String) : Int = (xls.find(x => x.name == name)) match {
  			case Some(y) => y.code
  			case None => -9999
  		}
  	
   	def getErrorMessageByName(name : String) : String = (xls.find(x => x.name == name)) match {
  			case Some(y) => y.message
  			case None => "unknow error"
  		}
   	
   	def errorToJson(name : String) : JsValue =
  		Json.toJson(Map("status" -> toJson("error"), "error" -> 
  				toJson(Map("code" -> toJson(this.getErrorCodeByName(name)), "message" -> toJson(this.getErrorMessageByName(name))))))
}