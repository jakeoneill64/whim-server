package com.whim.component.auth.service

import com.github.scribejava.apis.FacebookApi
import com.github.scribejava.core.builder.{ScopeBuilder, ServiceBuilder}
import com.github.scribejava.core.model.{OAuthRequest, Verb}
import com.github.scribejava.core.oauth.OAuth20Service
import com.whim.component.auth.{AuthConfiguration, FacebookAuthConfig}
import com.whim.component.config.given
import com.whim.component.user.User

import scala.concurrent.Future

object OAuthFacebookService extends OAuthService {

  private val RESOURCE_URL = "https://graph.facebook.com/v3.2/me"
  private val oauthService: Future[OAuth20Service] = AuthConfiguration().map {
    config =>
      val FacebookAuthConfig(clientId, clientSecret) = config.facebookConfig
      new ServiceBuilder(clientId)
        .apiSecret(clientSecret)
        .callback("http://localhost:8080/auth/facebook/callback")
        .defaultScope(ScopeBuilder().withScopes("email", "public_profile").build())
        .build(FacebookApi.instance());
  }

  override def getRedirect: Future[String] =
    oauthService.map {
      service =>

        val state = secureRandom.nextLong().toString
        stateRepo.createOrUpdate(state)

        service.getAuthorizationUrl(state)

    }

  override def authenticateUser(grantCode: String): Future[User] = oauthService.map {
    service => authenticateUser(service, RESOURCE_URL)(grantCode)
  }

}
