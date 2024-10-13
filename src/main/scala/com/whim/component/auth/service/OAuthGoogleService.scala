package com.whim.component.auth.service

import com.github.scribejava.apis.GoogleApi20
import com.github.scribejava.core.builder.{ScopeBuilder, ServiceBuilder}
import com.github.scribejava.core.model.{OAuth2AccessToken, OAuthRequest, Verb}
import com.github.scribejava.core.oauth.OAuth20Service
import com.whim.component.auth.repository.OAuthStateRepository
import com.whim.component.auth.service.OAuthFacebookService.oauthService
import com.whim.component.auth.{AuthConfiguration, GoogleAuthConfig}
import com.whim.component.config.given
import com.whim.component.user.User
import com.whim.generic.{Containable, Deletable, Modifiable}
import spray.json.*

import java.security.SecureRandom
import scala.concurrent.Future
import scala.jdk.CollectionConverters.*

object OAuthGoogleService extends OAuthService{

  private val RESOURCE_URL = "https://www.googleapis.com/oauth2/v3/userinfo"

  private val oauthService: Future[OAuth20Service] = AuthConfiguration().map {
    config =>
      val GoogleAuthConfig(clientId, clientSecret) = config.googleConfig
      new ServiceBuilder(clientId)
        .apiSecret(clientSecret)
        .callback("http://localhost:8080/auth/google/callback")
        .defaultScope(ScopeBuilder().withScope("profile").withScope("email").build())
        .build(GoogleApi20.instance());
  }

  override def getRedirect: Future[String] =
    oauthService.map {
      service =>

      val additionalParams: Map[String, String] = Map(
        "access_type" -> "offline",
        "prompt" -> "consent"
      )

      val state = secureRandom.nextLong().toString
      stateRepo.createOrUpdate(state)

      service
        .createAuthorizationUrlBuilder
        .state(state)
        .additionalParams(additionalParams.asJava)
        .build()

    }

  override def authenticateUser(grantCode: String): Future[User] = oauthService.map {
    service => authenticateUser(service, RESOURCE_URL)(grantCode)
  }

}
