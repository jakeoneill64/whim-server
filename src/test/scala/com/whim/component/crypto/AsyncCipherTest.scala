package com.whim.component.crypto

import org.scalatest.flatspec.{AnyFlatSpec, AsyncFlatSpec}
import org.scalatest.*

import java.nio.charset.{Charset, StandardCharsets}

class AsyncCipherTest extends AsyncFlatSpec {

  behavior of "AsyncCiphers"

  "The KMS cipher" should "encrypt and decrypt data a utf-16 string correctly" in {

    val cipher: AsyncCipher = AwsKmsCipher

    val lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    cipher.encrypt(lorem.getBytes).flatMap(cipher.decrypt).map {
      decrypted => assert(new String(decrypted) == lorem)
    }

  }

}