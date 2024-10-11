package com.whim.component.crypto

import com.whim.component.crypto.{AsyncCipher, AwsKmsCipher, CryptoConfiguration}
import com.whim.component.config.given

import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.{Cipher, KeyGenerator, SecretKey}
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import scala.concurrent.Future

object DekCipher extends AsyncCipher{

  private lazy val
  CryptoConfiguration(
  kekArn,
  kekAlgorithm,
  dataEncryptionAlgorithm,
  dataEncryptionCipherMode,
  dekSize
  ) = CryptoConfiguration()
  private lazy val kekCipher = AwsKmsCipher

  override def encrypt(data: Array[Byte]): Future[Array[Byte]] =

    Future {
      val keyGenerator = KeyGenerator getInstance dataEncryptionAlgorithm
      keyGenerator init dekSize
      val dek: SecretKey = keyGenerator.generateKey()
      (dek, kekCipher.encrypt(dek.getEncoded))
    }
    .flatMap {
      case (dek, encryptedDekFuture) => encryptedDekFuture.map {
        (dek, _)
      }
    }.map {
      case (dek, encryptedDek) =>
      val cipher = Cipher getInstance s"$dataEncryptionAlgorithm/$dataEncryptionCipherMode/PKCS5Padding"
      val iv: Array[Byte] = new Array(cipher.getBlockSize)
      new SecureRandom() nextBytes iv

      cipher.init(
        Cipher.ENCRYPT_MODE,
        dek,
        new IvParameterSpec(iv)
      )

      val encodedSize = ByteBuffer.allocate(4).putInt(encryptedDek.length).array()
      val encryptedDekSize = ByteBuffer.wrap(encodedSize).getInt()


        Base64.getEncoder.encode(
        ByteBuffer.allocate(4).putInt(encryptedDek.length).array() ++
        encryptedDek ++
        iv ++
        (cipher doFinal data)
      )
    }


  override def decrypt(data: Array[Byte]): Future[Array[Byte]] =

    Future {
      val decodedData = Base64.getDecoder.decode(data)
      val encryptedDekSize = ByteBuffer.wrap(decodedData take Integer.BYTES).getInt()
      val encryptedDek = decodedData.slice(Integer.BYTES, Integer.BYTES + encryptedDekSize)
      (encryptedDekSize, kekCipher.decrypt(encryptedDek), decodedData)
    }
    .flatMap {
      case (encryptedDekSize, encryptedDekFuture, decodedData) => encryptedDekFuture.map {
        (encryptedDekSize, _, decodedData)
      }
     }
    .map {

      case (encryptedDekSize, dek, decodedData) =>

        val dekEncoded = new String(Base64.getEncoder.encode(dek), "utf-8")
        val cipher = Cipher getInstance
          s"$dataEncryptionAlgorithm/$dataEncryptionCipherMode/PKCS5Padding"
        val iv = decodedData.slice(Integer.BYTES + encryptedDekSize, Integer.BYTES + encryptedDekSize + cipher.getBlockSize)
        val cipherText = decodedData drop Integer.BYTES + encryptedDekSize + cipher.getBlockSize

        cipher.init(
          Cipher.DECRYPT_MODE,
          new SecretKeySpec(dek, dataEncryptionAlgorithm),
          new IvParameterSpec(iv)
        )
        cipher doFinal cipherText
    }

}