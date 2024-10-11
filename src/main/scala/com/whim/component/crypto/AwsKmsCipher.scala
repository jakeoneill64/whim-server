package com.whim.component.crypto

import com.whim.component.Parser
import com.whim.component.config.given
import com.whim.component.crypto.{AsyncCipher, CryptoConfiguration}
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.kms.KmsAsyncClient
import software.amazon.awssdk.services.kms.model.{DecryptRequest, EncryptRequest}
import scala.jdk.OptionConverters._
import scala.jdk.FutureConverters._
import java.util.concurrent.CompletableFuture
import scala.concurrent.Future
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.Try

object AwsKmsCipher extends AsyncCipher {

  private lazy val
  CryptoConfiguration(
    kekArn,
    kekAlgorithm,
    dataEncryptionAlgorithm,
    dataEncryptionCipherMode,
    dekSize
  ) = CryptoConfiguration()

  private lazy val kmsClient = KmsAsyncClient
    .builder()
    .region(Region.of(
      kekArn
        .region()
        .toScala
        .get
        )
      )
    .build()

  override def decrypt(data: Array[Byte]): Future[Array[Byte]] =
      kmsClient.decrypt(
        DecryptRequest
          .builder
          .keyId(kekArn.toString)
          .ciphertextBlob(SdkBytes.fromByteArray(data))
          .encryptionAlgorithm(kekAlgorithm)
          .build
      )
      .asScala
      .map {
      _.plaintext().asByteArray()
    }

  override def encrypt(data: Array[Byte]): Future[Array[Byte]] =

      kmsClient.encrypt(
        EncryptRequest
          .builder
          .keyId(kekArn toString)
          .plaintext(SdkBytes.fromByteArray(data))
          .encryptionAlgorithm(kekAlgorithm)
          .build

      )
      .asScala
      .map {
      _.ciphertextBlob().asByteArray()
    }

}