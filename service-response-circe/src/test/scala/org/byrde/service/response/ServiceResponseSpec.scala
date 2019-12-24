package org.byrde.service.response

import org.byrde.service.response.ServiceResponse.TransientServiceResponse

import io.circe.Printer
import io.circe.generic.auto._
import io.circe.parser._

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers

class ServiceResponseSpec extends AnyFreeSpec with Matchers with EitherValues {
  private val MostlyEqualRegex =
    "\\s+"

  private val printer =
    Printer.spaces2.copy(dropNullValues = true)

  "ServiceResponse" - {
    "should deserialize to expected service response (e.g 1)" in {
      val json =
        """
          |{
          |  "type": "Success",
          |  "status": 200,
          |  "code": 1000,
          |  "response": {
          |    "message": "Test"
          |  }
          |}
        """.stripMargin

      val serialized =
        parse(json).flatMap(_.as[TransientServiceResponse[Message]])

      serialized should be ("right")
    }

    "should deserialize to expected service response (e.g 2)" in {
      val json =
        """
          |{
          |  "type": "Success",
          |  "status": 200,
          |  "code": 1000
          |}
        """.stripMargin

      val serialized =
        parse(json).flatMap(_.as[TransientServiceResponse[Option[Message]]])

      serialized should be ("right")
    }

    "should serialize to expected service response (e.g 1)" in {
      val expected =
        """
          |{
          |  "type": "Success",
          |  "status": 200,
          |  "code": 200,
          |  "response": {
          |    "message": "Test"
          |  }
          |}
        """.stripMargin.replaceAll(MostlyEqualRegex, "")

      val serialized =
        ServiceResponse(Message("Test")).toJson.printWith(printer).replaceAll(MostlyEqualRegex, "")

      assert(serialized == expected)
    }

    "should serialize to expected service response (e.g 2)" in {
      val expected =
        """
          |{
          |  "type": "Success",
          |  "status": 200,
          |  "code": 200
          |}
        """.stripMargin.replaceAll(MostlyEqualRegex, "")

      val serialized =
        ServiceResponse(Option.empty[Message]).toJson.printWith(printer).replaceAll(MostlyEqualRegex, "")

      assert(serialized == expected)
    }
  }
}
