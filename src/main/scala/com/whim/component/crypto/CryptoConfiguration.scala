package com.whim.component.crypto

import com.typesafe.config.{Config, ConfigFactory}
import software.amazon.awssdk.arns.Arn

case class CryptoConfiguration(
  kekArn: Arn,
  kekAlgorithm: String,
  dataEncryptionAlgorithm: String,
  dataEncryptionCipherMode: String,
  dekSize: Int
)

private case object CryptoConfiguration{
  private val config: Config = ConfigFactory.load()

  def apply(): CryptoConfiguration =
    CryptoConfiguration(
      kekArn = Arn.fromString(config.getString("crypto.kms.kek-arn")), // Bubble her. We can't recover from a malformed ARN here.
      kekAlgorithm = config.getString("crypto.kms.encryption-algorithm"),
      dataEncryptionAlgorithm = config.getString("crypto.data-encryption-algorithm"),
      dataEncryptionCipherMode = config.getString("crypto.data-encryption-cipher-mode"),
      dekSize = config.getInt("crypto.dek-size")
    )
}