package org.byrde.service.response

import org.byrde.service.response.DefaultServiceResponse.Message
import org.byrde.service.response.ServiceResponse.TransientServiceResponse

import io.circe.Printer
import io.circe.generic.auto._
import io.circe.parser._

import org.scalatest.{EitherValues, FreeSpec, Matchers}

class ServiceResponseSpec extends FreeSpec with Matchers with EitherValues {
  private val MostlyEqualRegex =
    "\\s+"

  private val printer =
    Printer.spaces2

  "ServiceResponse" - {
    "should serialize to expected service response" in {
      val json =
        """
          |{
          |  "type": "Success",
          |  "message": "Test",
          |  "status": 200,
          |  "code": 1000,
          |  "response": {
          |    "message": "Test"
          |  }
          |}
        """.stripMargin

      val serialized =
        parse(json).flatMap(_.as[TransientServiceResponse[Message]])

      serialized should be ('right)
    }

    "should deserialize to expected service response" in {
      val expected =
        """
          |{
          |  "type": "Success",
          |  "message": "Success",
          |  "status": 200,
          |  "code": 200,
          |  "response": {
          |    "message": "Test"
          |  }
          |}
        """.stripMargin.replaceAll(MostlyEqualRegex, "")

      val serialized =
        ServiceResponse(Message("Test")).toJson.pretty(printer).replaceAll(MostlyEqualRegex, "")

      assert(serialized == expected)
    }
  }
}