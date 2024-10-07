package com.whim.component.config

import com.typesafe.config.{Config, ConfigFactory}

case class CryptoConfiguration(
  kmsRegion: String, 
  dekLocation: String, 
  kmsKeyId: String,
  dekAlgorithm: String,
  dekCipherMode: String
)

given config: Config = ConfigFactory.load()
given cryptoConfig: CryptoConfiguration = CryptoConfiguration(
  config.getString("kms-region"),
  config.getString("dek-path"),
  config.getString("kms-key-id"),
  config.getString("dek-algorithm"),
  config.getString("dek-cipher-mode")
)