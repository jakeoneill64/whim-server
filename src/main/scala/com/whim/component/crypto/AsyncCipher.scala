package com.whim.component.crypto

import scala.concurrent.Future

trait AsyncCipher{
  def encrypt(data: Array[Byte]): Future[Array[Byte]]
  def decrypt(data: Array[Byte]): Future[Array[Byte]]
}