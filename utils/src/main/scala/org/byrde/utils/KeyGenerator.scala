package org.byrde.utils

import java.util.Base64

import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object KeyGenerator {
  private val CryptSpec =
    "AES"

  private val Bits =
    128

  private val KeyGen = {
    val innerKeyGenerator =
      javax.crypto.KeyGenerator.getInstance(CryptSpec)

    innerKeyGenerator.init(Bits)
    innerKeyGenerator
  }

  def generateKey: SecretKey =
    KeyGen.generateKey

  def generateKeyAsString: String =
    generateKey.toSecretKeyString

  implicit class SecretKey2String(value: SecretKey) {
    def toSecretKeyString =
      Base64.getEncoder.encodeToString(value.getEncoded)
  }

  implicit class String2SecretKey(value: String) {
    def toSecretKey = {
      val decodedKey =
        Base64.getDecoder.decode(value)

      new SecretKeySpec(decodedKey, 0, decodedKey.length, CryptSpec)
    }
  }
}
