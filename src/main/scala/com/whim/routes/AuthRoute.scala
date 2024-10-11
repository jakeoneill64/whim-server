package com.physically.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives.{as, complete, delete, entity, onSuccess, path, pathEnd, pathPrefix, post, put, redirect}
import akka.http.scaladsl.server.Route
import com.whim.component.auth.{OAuthGoogle, OAuthService}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}


private sealed trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
//  given RootJsonFormat[UserDto] = jsonFormat3(UserDto.apply)
//  given RootJsonFormat[LoginRequest] = jsonFormat2(LoginRequest.apply)
}

object AuthRoute extends JsonSupport{

  private val googleAuthService: OAuthService = OAuthGoogle

  def apply(): Route =

    pathPrefix("auth") {
      path("google") {

        post {

          onSuccess(googleAuthService.getRedirect) { redirectUrl =>
            redirect(Uri(redirectUrl), StatusCodes.Found)
          }

        }

      }
    }

}

