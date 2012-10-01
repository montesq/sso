package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.Crypto
import java.util.Date
import helper.AES

object Application extends Controller {
  
  def giveToken = Action(parse.json) { request =>
    (request.body \ "name").asOpt[Int].map { name =>
      Ok((name + 2).toString())
    }.getOrElse {
      BadRequest("Missing parameter [name]")
    }
  }
  
  def checkToken(token: String) = Action {
 
    val jsonToken = Json.parse(AES.decryptAES(token))
    val username = (jsonToken \ "username").asOpt[String]
    val timestamp = jsonToken.\("timestamp")
    Ok(Json.toJson("test"))
  }
}
