package com.whim.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import com.whim.component.config.given
import com.whim.component.auth.service.{OAuthFacebookService, OAuthGoogleService, OAuthService}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.Future
import scala.util.{Failure, Success}

private sealed trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  //  given RootJsonFormat[UserDto] = jsonFormat3(UserDto.apply)
}

object AuthRoute extends JsonSupport{

  private val getOauthService = (providerName: String) => providerName match {
    case "google" => OAuthGoogleService
    case "facebook" => OAuthFacebookService
    case "microsoft" => ???
  }

  private val createOauthRoute = (providerName: String) => pathPrefix("google") {
    pathEndOrSingleSlash {

      post {

        onSuccess(getOauthService(providerName).getRedirect) { redirectUrl =>
          redirect(Uri(redirectUrl), StatusCodes.Found)
        }

      }

    } ~
      path("callback") {
        parameters("code", "state") { (grantCode, csrfToken) =>

          onComplete(
            getOauthService(providerName)
              .validateState(csrfToken)
              .flatMap {
                case true => getOauthService(providerName).authenticateUser(grantCode)
                case false => Future.failed(new IllegalArgumentException("Invalid CSRF token"))
              }) {
            case Success(user) => complete(user.name)
            case Failure(exception) => failWith(exception)
          }
        }
      }
  }

  def apply(): Route =

    pathPrefix("auth") {
      concat(
        createOauthRoute("google"),
        createOauthRoute("facebook"),
        createOauthRoute("microsoft")
      )
    }

}

