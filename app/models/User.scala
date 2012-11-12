package models
import play.api.db._
import play.api.Play.current
import play.api.libs.json._

import anorm._
import anorm.SqlParser._

case class User(username: String, password: String)

object User {
  
  // -- Parsers
  
  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[String]("user.username") ~
    get[String]("user.password") map {
      case username~password => User(username, password)
    }
  }
  
  // -- Queries
  
  /**
   * Authenticate a User.
   */
  def authenticate(username: String, password: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
         select * from user where 
         username = {username} and password = {password}
        """
      ).on(
        'username -> username,
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
            {username}, {password}
          )
        """
      ).on(
        'username -> user.username,
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
   * Retrieve a user based on the username.
   */
  def findByusername(username: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
         select * from user where 
         username = {username}
        """
      ).on(
        'username -> username
      ).as(User.simple.singleOpt)
    }
  }
   
  /**
   * Delete a user based on the username
   */
  def deleteByusername(username: String): Int = {
    DB.withConnection { implicit connection =>
      SQL(
        """
         delete * from user where 
         username = {username}
        """
      ).on(
        'username -> username
      ).executeUpdate()
    }
  }
   
}
