package org.byrde.support

import java.util.Base64
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

import scala.util.chaining._

object KeyGenerator {
  private val CryptSpec =
    "AES"

  private def keyGen(bits: Int) =
    javax.crypto.KeyGenerator.getInstance(CryptSpec).tap(_.init(bits))

  def generateKey(bits: Int = 256): SecretKey =
    keyGen(bits).generateKey

  def generateKeyAsString(bits: Int = 256): String =
    generateKey(bits).toSecretKeyString

  implicit class SecretKey2String(value: SecretKey) {
    def toSecretKeyString: String =
      Base64.getEncoder.encodeToString(value.getEncoded)
  }

  implicit class String2SecretKey(value: String) {
    def toSecretKey: SecretKeySpec =
      Base64.getDecoder.decode(value).pipe(key => new SecretKeySpec(key, 0, key.length, CryptSpec))
  }
}
