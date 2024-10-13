package com.whim.component.auth

import com.typesafe.config.{Config, ConfigFactory}
import com.whim.component.crypto.{AsyncCipher, DekCipher}
import com.whim.component.config.given

import java.nio.charset.StandardCharsets
import scala.concurrent.Future

case class GoogleAuthConfig(clientId: String, clientSecret: String)
case class FacebookAuthConfig(clientId: String, clientSecret: String)
case class AuthConfiguration(googleConfig: GoogleAuthConfig, facebookConfig: FacebookAuthConfig)

case object AuthConfiguration{

  private lazy val config: Config = ConfigFactory.load()
  private lazy val dekCipher: AsyncCipher = DekCipher

  def apply(): Future[AuthConfiguration] =

    dekCipher.decrypt(config.getString("auth.google.client-secret").getBytes(StandardCharsets.UTF_8)).map {
      googleClientSecret =>
        (
          googleClientSecret,
          dekCipher.decrypt(config.getString("auth.facebook.client-secret").getBytes(StandardCharsets.UTF_8))
        )
    }.flatMap {
      case (googleClientSecret, facebookClientSecretFuture) => facebookClientSecretFuture.map {
        (googleClientSecret, _)
      }
    }.map {
      case (googleClientSecret, facebookClientSecret) => AuthConfiguration(
        GoogleAuthConfig(
          config.getString("auth.google.client-id"),
          String(googleClientSecret, StandardCharsets.UTF_8)
        ),
        FacebookAuthConfig(
          config.getString("auth.facebook.client-id"),
          String(facebookClientSecret, StandardCharsets.UTF_8)
        )
      )
    }

}