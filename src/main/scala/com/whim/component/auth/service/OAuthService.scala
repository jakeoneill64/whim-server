package com.whim.component.auth.service

import com.github.scribejava.apis.GoogleApi20
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.{OAuth2AccessToken, OAuthRequest, Verb}
import com.github.scribejava.core.oauth.OAuth20Service
import com.whim.component.auth.repository.OAuthStateRepository
import com.whim.component.auth.service.OAuthGoogleService.oauthService
import com.whim.component.config.given
import com.whim.component.user.User
import com.whim.generic.{Containable, Deletable, Modifiable}

import java.security.SecureRandom
import scala.concurrent.Future
import spray.json.*
import com.whim.component.user.UserJsonProtocol.given

trait OAuthService {

  protected val stateRepo: Modifiable[String] & Containable[String] & Deletable[String] = OAuthStateRepository
  protected val secureRandom: SecureRandom = new SecureRandom()

  def getRedirect: Future[String]
  def authenticateUser(grantCode: String): Future[User]

  protected def authenticateUser(oauth20Service: OAuth20Service, protectedResourceUrl: String)(grantCode: String): User = {
        val resourceRequest = new OAuthRequest(Verb.GET, protectedResourceUrl)
        oauth20Service.signRequest(oauth20Service.getAccessToken(grantCode), resourceRequest)
        val response = oauth20Service.execute(resourceRequest)
        response.getBody.parseJson.convertTo[User]
  }

  def validateState(csrfToken: String): Future[Boolean] = Future {
    val validated = OAuthStateRepository contains csrfToken
    OAuthStateRepository delete csrfToken
    validated
  }

}
