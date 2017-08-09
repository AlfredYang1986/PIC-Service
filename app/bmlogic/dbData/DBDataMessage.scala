package bmlogic.dbData

import bmmessages.CommonMessage
import play.api.libs.json.JsValue

/**
  * Created by yym on 7/27/17.
  */
abstract class msg_DBDataCommand extends CommonMessage
object DBDataMessage {
    case class msg_rawData2DB(data :JsValue) extends msg_DBDataCommand
    case class msg_readRawData(data:JsValue) extends msg_DBDataCommand
}
