package com.whim.component.crypto

import org.scalatest.flatspec.{AnyFlatSpec, AsyncFlatSpec}
import org.scalatest.*

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

  "The Data Encryption Cipher" should "encrypt and decrypt a utf-16 string correctly" in {
    val cipher: AsyncCipher = DekCipher
    cipherTest(cipher)
  }

}