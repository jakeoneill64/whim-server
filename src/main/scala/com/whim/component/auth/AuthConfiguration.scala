package com.whim.component.auth

import com.typesafe.config.{Config, ConfigFactory}
import com.whim.component.crypto.{AsyncCipher, DekCipher}
import com.whim.component.config.given

import java.nio.charset.StandardCharsets
import scala.concurrent.Future

case class GoogleAuthConfig(clientId: String, clientSecret: String)
case class AuthConfiguration(googleConfig: GoogleAuthConfig)

case object AuthConfiguration{

  private lazy val config: Config = ConfigFactory.load()
  private lazy val dekCipher: AsyncCipher = DekCipher

  def apply(): Future[AuthConfiguration] =

    dekCipher.decrypt(config.getString("auth.google.client-secret").getBytes(StandardCharsets.UTF_8)).map{

      clientSecret => AuthConfiguration(
            GoogleAuthConfig(
              config.getString("auth.google.client-id"),
              String(clientSecret, StandardCharsets.UTF_8)
            )
      )
    }

}