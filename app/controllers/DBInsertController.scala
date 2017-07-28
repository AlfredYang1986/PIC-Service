package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import bminjection.db.DBTrait
import bminjection.token.AuthTokenTrait
import bmlogic.common.requestArgsQuery
import bmlogic.config.ConfigMessage.msg_QueryInfoCommand
import bmlogic.dbData.DBDataMessage.msg_rawData2DB
import bmmessages.{CommonModules, MessageRoutes}
import bmpattern.LogMessage.msg_log
import bmpattern.ResultMessage.msg_CommonResultMessage
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, Controller}

/**
  * Created by yym on 7/27/17.
  */
class DBInsertController @Inject () (as_inject : ActorSystem, dbt : DBTrait, att : AuthTokenTrait) extends Controller{
    implicit val as = as_inject
    def InsertRawData = Action (request => requestArgsQuery().requestArgsV2(request) { jv =>
        import bmpattern.LogMessage.common_log
        import bmpattern.ResultMessage.common_result
        
        MessageRoutes(msg_log(toJson(Map("method" -> toJson("Insert raw data"))), jv)
            ::msg_rawData2DB(jv)::msg_CommonResultMessage() :: Nil, None)(CommonModules(Some(Map("db" -> dbt, "att" -> att))))
    })
    
}
