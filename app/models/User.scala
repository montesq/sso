package models
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

import anorm._
import anorm.SqlParser._

case class User(email: String, password: String)

object User {
  
  // -- Parsers
  
  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[String]("user.email") ~
    get[String]("user.password") map {
      case email~password => User(email, password)
    }
  }
  
  // -- Queries
  
  /**
   * Authenticate a User.
   */
  def authenticate(email: String, password: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
         select * from user where 
         email = {email} and password = {password}
        """
      ).on(
        'email -> email,
        'password -> password
      ).as(User.simple.singleOpt)
    }
  }
   
  /**
   * Create a User.
   */
  def create(user: User): User = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into user values (
            {email}, {password}
          )
        """
      ).on(
        'email -> user.email,
        'password -> user.password
      ).executeUpdate()
      
      user
      
    }
  }
  
  /**
   * Retrieve all users.
   */
  def findAll: Seq[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from user").as(User.simple *)
    }
  }

  /**
   * Retrieve a user based on the email.
   */
  def findByEmail(email: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
         select * from user where 
         email = {email}
        """
      ).on(
        'email -> email
      ).as(User.simple.singleOpt)
    }
  }
   
  /**
   * Delete a user based on the email
   */
  def deleteByEmail(email: String): Int = {
    DB.withConnection { implicit connection =>
      SQL(
        """
         delete * from user where 
         email = {email}
        """
      ).on(
        'email -> email
      ).executeUpdate()
    }
  }
   
}
