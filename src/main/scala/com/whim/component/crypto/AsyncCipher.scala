package com.whim.component.crypto

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.kms.KmsAsyncClient
import software.amazon.awssdk.services.kms.model.{DecryptRequest, EncryptRequest}
import com.typesafe.config.{Config, ConfigFactory}
import com.whim.component.Parser
import com.whim.component.config.given
import software.amazon.awssdk.core.SdkBytes

import scala.compat.java8.FutureConverters.toScala
import scala.annotation.tailrec
import scala.concurrent.Future
import scala.util.{Failure, Success, Try, Using}
import java.io.{BufferedReader, FileReader}
import java.nio.ByteBuffer
import java.nio.file.{Files, Paths}
import java.security
import java.security.SecureRandom
import javax.crypto.{Cipher, KeyGenerator, SecretKey}
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import scala.language.postfixOps
import com.whim.component.aws.Arn


trait AsyncCipher{
  def encrypt(data: Array[Byte]): Future[Array[Byte]]
  def decrypt(data: Array[Byte]): Future[Array[Byte]]
}

object AwsKmsCipher extends AsyncCipher {

  private lazy val arnParser: Parser[Try[Arn]] = Arn
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
                                    .region(
                                      Region.of(
                                        arnParser
                                          .parse(kekArn)
                                          .map(_ region)
                                          .get // Bubble her. We can't recover from a malformed ARN here.
                                      )
                                    )
                                    .build()

  override def decrypt(data: Array[Byte]): Future[Array[Byte]] =
    toScala(
      kmsClient.decrypt(
        DecryptRequest
          .builder
          .keyId(kekArn)
          .ciphertextBlob(SdkBytes.fromByteArray(data))
          .encryptionAlgorithm(kekAlgorithm)
          .build
    )).map {
        _.plaintext().asByteArray()
    }

  override def encrypt(data: Array[Byte]): Future[Array[Byte]] =
    toScala(

      kmsClient.encrypt(
        EncryptRequest
          .builder
          .keyId(kekArn)
          .plaintext(SdkBytes.fromByteArray(data))
          .encryptionAlgorithm(kekAlgorithm)
          .build

    )).map {
      _.ciphertextBlob().asByteArray()
    }

}

val KekCipher: AsyncCipher = AwsKmsCipher
object DekCipher extends AsyncCipher{

  private lazy val
  CryptoConfiguration(
    kekArn,
    kekAlgorithm,
    dataEncryptionAlgorithm,
    dataEncryptionCipherMode,
    dekSize
  ) = CryptoConfiguration()

  override def encrypt(data: Array[Byte]): Future[Array[Byte]] =

      Future {
        val keyGenerator = KeyGenerator getInstance dataEncryptionAlgorithm
        keyGenerator init dekSize
        val dek: SecretKey = keyGenerator.generateKey()
        (dek, KekCipher.encrypt(dek.getEncoded))
      }
      .flatMap {
        case (dek, encryptedDekFuture) => encryptedDekFuture.map {
          (dek, _)
        }
      }.map {
          case (dek, encryptedDek) =>
            val cipher = Cipher getInstance (
              s"$dataEncryptionAlgorithm/$dataEncryptionCipherMode/PKCS5Padding"
              )
            val iv: Array[Byte] = new Array(cipher.getBlockSize)
            new SecureRandom() nextBytes iv


            cipher.init(
              Cipher.ENCRYPT_MODE,
              dek,
              new IvParameterSpec(iv)
            )


            ByteBuffer.allocate(4).putInt(encryptedDek.length).array() ++
            encryptedDek ++ iv ++ (cipher doFinal data)
      }


  override def decrypt(data: Array[Byte]): Future[Array[Byte]] =

      Future {
        val encryptedDekSize = ByteBuffer.wrap(data take Integer.BYTES).getInt()
        val encryptedDek = data.slice(Integer.BYTES, Integer.BYTES + encryptedDekSize)
        (encryptedDekSize, summon[AsyncCipher].decrypt(encryptedDek))
      }
      .flatMap {
          case (encryptedDekSize, encryptedDekFuture) => encryptedDekFuture.map {
            (encryptedDekSize, _)
          }
      }
      .map {

        case (encryptedDekSize, dek) =>

          val cipher = Cipher getInstance
            s"$dataEncryptionAlgorithm/$dataEncryptionCipherMode/PKCS5Padding"
          val iv = data.slice(Integer.BYTES + encryptedDekSize, Integer.BYTES + encryptedDekSize + cipher.getBlockSize)
          val cipherText = data drop Integer.BYTES + encryptedDekSize + cipher.getBlockSize

          cipher.init(
            Cipher.DECRYPT_MODE,
            new SecretKeySpec(dek, dataEncryptionAlgorithm),
            new IvParameterSpec(iv)
          )
          cipher doFinal cipherText
      }

}