package com.whim.component.crypto

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.kms.KmsAsyncClient
import software.amazon.awssdk.services.kms.model.{DecryptRequest, EncryptRequest}
import com.typesafe.config.{Config, ConfigFactory}
import com.whim.component.config.given
import com.whim.component.config.CryptoConfiguration
import com.whim.component.crypto.AwsKmsCipher.kmsClient
import software.amazon.awssdk.core.SdkBytes

import scala.compat.java8.FutureConverters.toScala
import scala.annotation.tailrec
import scala.concurrent.Future
import scala.util.{Failure, Success, Try, Using}
import java.io.{BufferedReader, FileReader}
import java.nio.file.{Files, Paths}
import java.security
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import scala.language.postfixOps

trait AsyncCipher{
  def encrypt(data: Array[Byte]): Future[Array[Byte]]
  def decrypt(data: Array[Byte]): Future[Array[Byte]]
}

object AwsKmsCipher extends AsyncCipher {

  override def decrypt(data: Array[Byte]): Future[Array[Byte]] =
    toScala(
      kmsClient.decrypt(
        DecryptRequest
          .builder
          .keyId(summon[CryptoConfiguration].kmsKeyId)
          .ciphertextBlob(SdkBytes.fromByteArray(data))
          .build
    )).map {
        _.plaintext().asByteArray()
    }

  override def encrypt(data: Array[Byte]): Future[Array[Byte]] =
    toScala(

      kmsClient.encrypt(
        EncryptRequest
          .builder
          .keyId(summon[CryptoConfiguration].kmsKeyId)
          .plaintext(SdkBytes.fromByteArray(data))
          .build

    )).map {
      _.ciphertextBlob().asByteArray()
    }

  //TODO temp solution.
  private val kmsClient: KmsAsyncClient = KmsAsyncClient
    .builder()
    .region(Region.of(summon[CryptoConfiguration].kmsRegion))
    .build()

}

object DekCipher extends AsyncCipher{

  private def loadDekAsync(using masterCipher: AsyncCipher, cryptoConfig: CryptoConfiguration): Future[Array[Byte]] =
    Future {
      Files.readAllBytes(Paths.get(cryptoConfig.dekLocation))
    }
    .flatMap {
      masterCipher.decrypt
    }

  private val cipher = Cipher.getInstance(
    s"${summon[CryptoConfiguration].dekAlgorithm}/${summon[CryptoConfiguration].dekAlgorithm}/PKCS5Padding"
  )

  override def encrypt(data: Array[Byte]): Future[Array[Byte]] =
    loadDekAsync
      .map { dek =>

          val iv: Array[Byte] = new Array(cipher.getBlockSize)
          new SecureRandom() nextBytes iv

          cipher.synchronized {
            cipher.init(
              Cipher.ENCRYPT_MODE,
              new SecretKeySpec(dek, summon[CryptoConfiguration].dekAlgorithm),
              new IvParameterSpec(iv)
            )
            cipher.getIV ++ (cipher doFinal data)
          }

      }

  override def decrypt(data: Array[Byte]): Future[Array[Byte]] =
    loadDekAsync
      .map { dek =>
        cipher.synchronized {
          cipher.init(
            Cipher.DECRYPT_MODE,
            new SecretKeySpec(dek, summon[CryptoConfiguration].dekAlgorithm),
            new IvParameterSpec(data take cipher.getBlockSize)
          )
          cipher doFinal (data drop cipher.getBlockSize)
        }

      }

}