package org.byrde.client

package object http {

  type URL = String

  type Method = String

  type Headers = Seq[(String, String)]

  type Status = Int

  type Body = String

}
