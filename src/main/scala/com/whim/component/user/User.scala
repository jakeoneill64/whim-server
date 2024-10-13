package com.whim.component.user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class User(name: String, email: String)

object UserJsonProtocol extends DefaultJsonProtocol {
    given RootJsonFormat[User] = jsonFormat2(User.apply)
}