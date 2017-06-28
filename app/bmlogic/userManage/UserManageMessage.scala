package bmlogic.userManage

/**
  * Created by jeorch on 17-6-12.
  */
import play.api.libs.json.JsValue
import bmmessages.CommonMessage

abstract class msg_UserManageCommand extends CommonMessage

object UserManageMessage {
    case class msg_userManage_query(data : JsValue) extends msg_UserManageCommand // 用户管理
}