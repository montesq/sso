import play.api.test._
import play.api.test.Helpers._
import org.specs2.mutable._

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
import play.api.Play.current

class TokenSpec extends Specification {
 
  def initDb: Unit = {
    User.create(User("johndoe@world.com", "secret"))  
  }
  
  "Response after creating token Action" should {
        
    "be an error 400 when email doesn't exist in the DB" in {
      running(FakeApplication()) {
        initDb
        val Some(result) = routeAndCall(FakeRequest(POST, 
                                   "/token", 
                                   FakeHeaders(Map("Content-Type" -> Seq("application/json"))),
                                   Json.parse(generate(Map("email"    -> "test@test.com",
                                                           "password" -> "secret")))))
        status(result) must equalTo(BAD_REQUEST)
        contentAsString(result) must contain("Invalid email or password")
      }
    }

    "be an error 400 when password is not correct" in {
      running(FakeApplication()) {
        initDb
        val Some(result) = routeAndCall(FakeRequest(POST, 
                                   "/token", 
                                   FakeHeaders(Map("Content-Type" -> Seq("application/json"))),
                                   Json.parse(generate(Map("email"    -> "johndoe@world.com",
                                                           "password" -> "secret_")))))
        status(result) must equalTo(BAD_REQUEST)
        contentAsString(result) must contain("Invalid email or password")
      }
    }

    "be OK otherwise" in {
      running(FakeApplication()) {
        initDb
        val Some(result) = routeAndCall(FakeRequest(POST, 
                                   "/token", 
                                   FakeHeaders(Map("Content-Type" -> Seq("application/json"))),
                                   Json.parse(generate(Map("email"    -> "johndoe@world.com",
                                                           "password" -> "secret")))))
         contentAsString(result) must contain("authToken")        
         status(result) must equalTo(OK)
      }
    }
  }

  "Response after checking token validity" should {
        
    "be an error 400 when the email doesn't exist in the db" in {
      running(FakeApplication()) {
        initDb
        val token = AES.encryptAES(generate(Map("timestamp"-> System.currentTimeMillis.toString,
                                            "email"    -> "invalidEmail@world.com")))
        val Some(result) = routeAndCall(FakeRequest(GET, "/token/" + token))
        status(result) must equalTo(BAD_REQUEST)
      }
    }

    "be an error 400 when the timestamp is too old" in {
      val app = FakeApplication()
      running(app) {
        initDb
        val tokenExpiration = app.configuration.getMilliseconds("application.tokenExpiration") match {
                                  case Some(conf) => conf
                                  case None => 1000 * 60 * 60
                                }
        val token = AES.encryptAES(generate(Map("timestamp"-> (System.currentTimeMillis - tokenExpiration - 1).toString,
                                            "email"    -> "johndoe@world.com")))
        val Some(result) = routeAndCall(FakeRequest(GET, "/token/" + token))
        status(result) must equalTo(BAD_REQUEST)
      }
    }

    "be OK otherwise" in {
     running(FakeApplication()) {
        initDb
        val token = AES.encryptAES(generate(Map("timestamp"-> System.currentTimeMillis.toString,
                                            "email"    -> "johndoe@world.com")))
        val Some(result) = routeAndCall(FakeRequest(GET, "/token/" + token))
        status(result) must equalTo(OK)
      }
     }
  }
}
