//package com.physically.routes
//
//import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
//import akka.http.scaladsl.server.Directives.{as, complete, delete, entity, path, pathEnd, pathPrefix, post, put}
//import akka.http.scaladsl.server.Route
//import spray.json.{DefaultJsonProtocol, RootJsonFormat}
//
//
//private sealed trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
//  given RootJsonFormat[UserDto] = jsonFormat3(UserDto.apply)
//  given RootJsonFormat[LoginRequest] = jsonFormat2(LoginRequest.apply)
//}
//
//object UserRoute extends JsonSupport{
//
//  def apply(): Route =
//
//    pathPrefix("partner") {
//      path("session") {
//
//        post {
//
//          entity(as[LoginRequest]) { request =>
//
//          }
//
//        }
//
//        put {
//          entity(as[String]) { jwt =>
//
//          }
//        }
//
//        delete {
//          entity
//        }
//
//      }
//    }
//
//}
//
