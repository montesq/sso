package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Result
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import com.codahale.jerkson.Json._
import play.api.libs.Crypto
import java.util.Date
import helper.AES
import validation.Constraints._
import models._

object Token extends Controller {
  
  private val tokenExpiration = Play.maybeApplication.flatMap(_.configuration.getMilliseconds("application.tokenExpiration")) match {
                                  case Some(conf) => conf
                                  case None => 1000 * 60 * 60
                                }
  
  val loginForm = Form(
    tuple(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    ) verifying ("Invalid email or password", result => result match {
      case (email, password) => User.authenticate(email, password).isDefined
      }
    )
  )

  def giveToken = Action (parse.json) { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(formWithErrors.errorsAsJson),
      value => {
        val (email, password) = value
        val toBeCrypted = generate(Map("timestamp"-> System.currentTimeMillis.toString,
                                       "email"    -> email))
        val token = AES.encryptAES(toBeCrypted)
        Ok(Json.parse(generate(Map("authToken"-> token)))).withCookies(
          Cookie("authToken", 
                 token, 
                 tokenExpiration.toInt, 
                 "/", 
                 Option(request.domain), 
                 true, 
                 false)
          )
        } 
    )
  }
  
  val tokenValidityForm = Form(
    tuple(
      "email" -> nonEmptyText,
      "timestamp" -> longNumber
    ) verifying ("Invalid token", result => result match {
      case (email, timestamp) => (User.findByEmail(email).isDefined
                                    && timestamp < System.currentTimeMillis
                                    && timestamp > System.currentTimeMillis 
                                       - tokenExpiration)
      }
    )
  )

  def checkToken(token: String) = Action {
    val jsonToken = Json.parse(AES.decryptAES(token))
    tokenValidityForm.bind(jsonToken).fold(
      formWithErrors => BadRequest(formWithErrors.errorsAsJson),
      value => Ok("Token is valid")
    )
  }
}
