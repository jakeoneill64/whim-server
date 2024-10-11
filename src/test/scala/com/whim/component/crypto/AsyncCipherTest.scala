package com.whim.component.crypto

import com.typesafe.config.ConfigFactory
import com.whim.component.auth.AuthConfiguration.{config, dekCipher}
import com.whim.component.auth.{AuthConfiguration, GoogleAuthConfig}
import org.scalatest.flatspec.{AnyFlatSpec, AsyncFlatSpec}
import org.scalatest.*

import java.io.File
import java.nio.charset.{Charset, StandardCharsets}
import scala.concurrent.Future

class AsyncCipherTest extends AsyncFlatSpec {

  behavior of "AsyncCiphers"

  private val lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
  private val cipherTest: AsyncCipher => Future[Assertion] = (cipher: AsyncCipher) => cipher.encrypt(lorem.getBytes).flatMap(cipher.decrypt).map {
    decrypted => assert(new String(decrypted) == lorem)
  }

  "The KMS cipher" should "encrypt and decrypt data a utf-16 string correctly" in {

    val cipher: AsyncCipher = AwsKmsCipher
    cipherTest(cipher)

  }

  "The data encryption cipher" should "encrypt and decrypt a utf-16 string correctly" in {
    val cipher: AsyncCipher = DekCipher
    cipherTest(cipher)
  }

}