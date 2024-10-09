package com.whim.component.crypto

import com.typesafe.config.{Config, ConfigFactory}


case class CryptoConfiguration(
  kekArn: String,
  kekAlgorithm: String,
  dataEncryptionAlgorithm: String,
  dataEncryptionCipherMode: String,
  dekSize: Int
)

private case object CryptoConfiguration{
  private val config: Config = ConfigFactory.load()

  def apply(): CryptoConfiguration = 
    CryptoConfiguration(
      kekArn = config.getString("crypto.kms.kek-arn"),
      kekAlgorithm = config.getString("crypto.kms.encryption-algorithm"),
      dataEncryptionAlgorithm = config.getString("crypto.data-encryption-algorithm"),
      dataEncryptionCipherMode = config.getString("crypto.data-encryption-cipher-mode"),
      dekSize = config.getInt("crypto.dek-size")
    )
}