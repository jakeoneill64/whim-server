package com.whim.component.config

import com.typesafe.config.{Config, ConfigFactory}

case class CryptoConfiguration(
  kekArn: String,
  kekAlgorithm: String,
  dataEncryptionAlgorithm: String,
  dataEncryptionCipherMode: String,
  dekSize: Int
)

given config: Config = ConfigFactory.load()
given cryptoConfig: CryptoConfiguration = CryptoConfiguration(
  config.getString("crypto.kms.kek-arn"),
  config.getString("crypto.kms.encryption-algorithm"),
  config.getString("crypto.data-encryption-algorithm"),
  config.getString("crypto.data-encryption-cipher-mode"),
  config.getInt("crypto.dek-size")
)