package org.byrde.http.server

import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest

import org.byrde.http.server.support.RequestIdSupport.IdHeader
import org.byrde.support.EitherSupport

import io.circe.Json
import io.circe.generic.auto._

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.capabilities
import sttp.capabilities.akka.AkkaStreams
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.ChainingSyntax

class ServerTest extends AnyFlatSpec with Matchers with ScalaFutures with ScalatestRouteTest with EitherSupport {
  class TestRoute(
    fn: () => Future[Either[ByrdeErrorResponse, ByrdeResponse.Default]],
    mapper: EndpointOutput.OneOf[ByrdeErrorResponse, ByrdeErrorResponse]
  ) extends ChainingSyntax {
    self: ByrdeHttpServer#ByrdeRoutesMixin =>
    
    private lazy val test: ByrdeRoute[Unit, ByrdeErrorResponse, ByrdeResponse.Default, AkkaStreams with capabilities.WebSockets] =
      endpoint[ByrdeResponse.Default](mapper = mapper)
        .in("test")
        .toTapirRoute(fn)
    
    override lazy val routes: ByrdeRoutes = test
  }
  
  trait TestServer extends ByrdeHttpServer {
    override lazy val provider: Provider =
      new TestProvider
  
    protected def mapper: EndpointOutput.OneOf[ByrdeErrorResponse, ByrdeErrorResponse] =
      defaultMapper
    
    protected lazy val routes: Seq[ByrdeRoutesMixin] =
      Seq(new TestRoute(test, mapper) with ByrdeRoutesMixin)
  
    protected def test: () => Future[Either[ByrdeErrorResponse, ByrdeResponse.Default]] =
      () =>
        Future
          .successful(Right[String, String]("Hello World!"))
          .toOut {
            case (_, code) =>
              ByrdeResponse.Default("Error", code)
          }
  }
  
  "Server.ping" should "return successfully" in new TestServer {
    HttpRequest(HttpMethods.GET, "/ping") ~> Route.seal(handleByrdeRoutes(routes)) ~> {
      check {
        val entity = responseAs[Json].as[ByrdeResponse.Default].get
        
        assert(response.header[IdHeader].isDefined)
        status.intValue shouldBe 200
        assert(entity.code === successCode)
      }
    }
  }
  
  "Server.routes" should "return a 200 when function completes successfully" in new TestServer {
    HttpRequest(HttpMethods.GET, "/test") ~> Route.seal(handleByrdeRoutes(routes)) ~> {
      check {
        val entity = responseAs[Json].as[ByrdeResponse.Default].get
        
        assert(response.header[IdHeader].isDefined)
        status.intValue shouldBe 200
        assert(entity.code === successCode)
      }
    }
  }
  
  it should "return a 400 when function completes with controlled failure" in new TestServer {
    HttpRequest(HttpMethods.GET, "/test") ~> Route.seal(handleByrdeRoutes(routes)) ~> {
      check {
        val entity = responseAs[Json].as[ByrdeResponse.Default].get
      
        assert(response.header[IdHeader].isDefined)
        status.intValue shouldBe 400
        assert(entity.code === errorCode)
      }
    }
  
    override lazy val test: () => Future[Either[ByrdeErrorResponse, ByrdeResponse.Default]] =
      () =>
        Future.successful(Left(ByrdeResponse.Default("Error", errorCode)))
  }
  
  it should "return a 404 on bogus path" in new TestServer {
    HttpRequest(HttpMethods.GET, "/mrgrlgrlgrl") ~> Route.seal(handleByrdeRoutes(routes)) ~> {
      check {
        status.intValue shouldBe 404
      }
    }
    
    override lazy val test: () => Future[Either[ByrdeErrorResponse, ByrdeResponse.Default]] =
      () =>
        Future.successful(Left(ByrdeResponse.Default("Error", errorCode)))
  }
  
  it should "return the status specified by the error mapper" in new ByrdeHttpServer {
    HttpRequest(HttpMethods.GET, "/test") ~> Route.seal(handleByrdeRoutes(routes)) ~> {
      check {
        val entity = responseAs[Json].as[ByrdeResponse.Default].get
        
        assert(response.header[IdHeader].isDefined)
        status.intValue shouldBe 403
        assert(entity.code === errorCode + 2)
      }
    }
  
    class TestRoute extends ChainingSyntax {
      self: ByrdeHttpServer#ByrdeRoutesMixin =>
    
      case class Test(code: Int, example: String, example1: String) extends ByrdeResponse
  
      lazy val mapper: EndpointOutput.OneOf[ByrdeErrorResponse, ByrdeErrorResponse] =
        sttp.tapir.oneOf[ByrdeErrorResponse](
          statusMappingValueMatcher(StatusCode.Unauthorized, jsonBody[ByrdeErrorResponse].description("Unauthorized!")) {
            case ex: ByrdeErrorResponse if ex.code == errorCode + 1 => true
          },
          statusMappingValueMatcher(StatusCode.Forbidden, jsonBody[ByrdeErrorResponse].description("Forbidden!")) {
            case ex: ByrdeErrorResponse if ex.code == errorCode + 2 => true
          }
        )
      
      private lazy val test: ByrdeRoute[Unit, ByrdeErrorResponse, Test, AkkaStreams with capabilities.WebSockets] =
        endpoint[Test](mapper = mapper)
          .in("test")
          .toTapirRoute {
            () =>
              Future
                .successful(Left[Int, (String, String)](errorCode + 2))
                .toOut(
                  {
                    case ((example, example1), code) =>
                      Test(code, example, example1)
                  }, {
                    case (code, _) =>
                      ByrdeResponse.Default("Error", code)
                  }
                )
          }
    
      override lazy val routes: ByrdeRoutes = test
    }
  
    override lazy val provider: Provider =
      new TestProvider
  
    protected lazy val routes: Seq[ByrdeRoutesMixin] =
      Seq(new TestRoute with ByrdeRoutesMixin)
  }
  
  it should "return a custom status code based on the error type when function completes with controlled failure" in new TestServer {
    HttpRequest(HttpMethods.GET, "/test") ~> Route.seal(handleByrdeRoutes(routes)) ~> {
      check {
        val entity = responseAs[Json].as[ByrdeResponse.Default].get

        assert(response.header[IdHeader].isDefined)
        status.intValue shouldBe 401
        assert(entity.code === errorCode + 1)
      }
    }
  
    HttpRequest(HttpMethods.GET, "/test") ~> Route.seal(handleByrdeRoutes(routes)) ~> {
      check {
        val entity = responseAs[Json].as[ByrdeResponse.Default].get
      
        assert(response.header[IdHeader].isDefined)
        status.intValue shouldBe 403
        assert(entity.code === errorCode + 2)
      }
    }
    
    private var counter = 0
  
    override lazy val mapper: EndpointOutput.OneOf[ByrdeErrorResponse, ByrdeErrorResponse] =
      sttp.tapir.oneOf[ByrdeErrorResponse](
        statusMappingValueMatcher(StatusCode.Unauthorized, jsonBody[ByrdeErrorResponse].description("Client exception!")) {
          case ex: ByrdeErrorResponse if ex.code == errorCode + 1 => true
        },
        statusMappingValueMatcher(StatusCode.Forbidden, jsonBody[ByrdeErrorResponse].description("Client exception!")) {
          case ex: ByrdeErrorResponse if ex.code == errorCode + 2 => true
        }
      )

    override def test: () => Future[Either[ByrdeErrorResponse, ByrdeResponse.Default]] =
      () =>
        counter
          .pipe(_ + 1)
          .tap(counter = _)
          .pipe(count => Future.successful(Left(ByrdeResponse.Default("Error", errorCode + count))))
  }
  
  it should "return a 500 when function completes with unexpected failure" in new TestServer {
    HttpRequest(HttpMethods.GET, "/test") ~> Route.seal(handleByrdeRoutes(routes)) ~> {
      check {
        val entity = responseAs[Json].as[ByrdeResponse.Default].get
        
        assert(response.header[IdHeader].isDefined)
        status.intValue shouldBe 500
        assert(entity.code === errorCode)
      }
    }
    
    override lazy val test: () => Future[Either[ByrdeErrorResponse, ByrdeResponse.Default]] =
      () => Future.failed(new Exception("Kaboom!"))
  }
  
  it should "expose Swagger documentation" in new TestServer {
    HttpRequest(HttpMethods.GET, "/docs/index.html?url=/docs/docs.yaml") ~> Route.seal(handleByrdeRoutes(routes)) ~> {
      check {
        assert(response.header[IdHeader].isDefined)
        status.intValue shouldBe 200
      }
    }
  }
  
  it should "redirect /docs to Swagger documentation" in new TestServer {
    HttpRequest(HttpMethods.GET, "/docs") ~> Route.seal(handleByrdeRoutes(routes)) ~> {
      check {
        assert(response.header[IdHeader].isDefined)
        status.intValue shouldBe 308
      }
    }
  }
  
  it should "expose OpenAPI documentation (alpha)" in new TestServer {
    private val expected =
      """openapi: 3.0.3
        |info:
        |  title: test
        |  version: v1.0
        |paths:
        |  /test:
        |    get:
        |      operationId: getTest
        |      responses:
        |        '200':
        |          description: Response Body.
        |          content:
        |            application/json:
        |              schema:
        |                $ref: '#/components/schemas/Default'
        |        '400':
        |          description: 'Client exception! Error code: 101'
        |          content:
        |            application/json:
        |              schema:
        |                $ref: '#/components/schemas/Default'
        |              example:
        |                message: Error
        |                code: 101
        |  /ping:
        |    get:
        |      description: Standard API endpoint to say hello to the server.
        |      operationId: Ping
        |      responses:
        |        '200':
        |          description: 'Default response! Success code: 100'
        |          content:
        |            application/json:
        |              schema:
        |                $ref: '#/components/schemas/Default'
        |              example:
        |                message: Success
        |                code: 100
        |        '400':
        |          description: 'Client exception! Error code: 101'
        |          content:
        |            application/json:
        |              schema:
        |                $ref: '#/components/schemas/Default'
        |              example:
        |                message: Error
        |                code: 101
        |components:
        |  schemas:
        |    Default:
        |      required:
        |      - message
        |      - code
        |      type: object
        |      properties:
        |        message:
        |          type: string
        |        code:
        |          type: integer
        |""".stripMargin
    
    HttpRequest(HttpMethods.GET, "/docs/docs.yaml") ~> Route.seal(handleByrdeRoutes(routes)) ~> {
      check {
        val actual = response.entity.toStrict(1.seconds).map(_.data.utf8String).futureValue
        
        status.intValue shouldBe 200
        assert(actual === expected)
      }
    }
  }
  
  it should "expose OpenAPI documentation (beta)" in new ByrdeHttpServer {
    private val expected =
      """openapi: 3.0.3
        |info:
        |  title: test
        |  version: v1.0
        |paths:
        |  /test:
        |    get:
        |      operationId: getTest
        |      responses:
        |        '200':
        |          description: Response Body.
        |          content:
        |            application/json:
        |              schema:
        |                $ref: '#/components/schemas/Test'
        |        '401':
        |          description: Unauthorized!
        |          content:
        |            application/json:
        |              schema:
        |                $ref: '#/components/schemas/Default'
        |        '403':
        |          description: Forbidden!
        |          content:
        |            application/json:
        |              schema:
        |                $ref: '#/components/schemas/Default'
        |  /ping:
        |    get:
        |      description: Standard API endpoint to say hello to the server.
        |      operationId: Ping
        |      responses:
        |        '200':
        |          description: 'Default response! Success code: 100'
        |          content:
        |            application/json:
        |              schema:
        |                $ref: '#/components/schemas/Default'
        |              example:
        |                message: Success
        |                code: 100
        |        '400':
        |          description: 'Client exception! Error code: 101'
        |          content:
        |            application/json:
        |              schema:
        |                $ref: '#/components/schemas/Default'
        |              example:
        |                message: Error
        |                code: 101
        |components:
        |  schemas:
        |    Default:
        |      required:
        |      - message
        |      - code
        |      type: object
        |      properties:
        |        message:
        |          type: string
        |        code:
        |          type: integer
        |    Test:
        |      required:
        |      - code
        |      - example
        |      - example1
        |      type: object
        |      properties:
        |        code:
        |          type: integer
        |        example:
        |          type: string
        |        example1:
        |          type: string
        |""".stripMargin
    
    HttpRequest(HttpMethods.GET, "/docs/docs.yaml") ~> Route.seal(handleByrdeRoutes(routes)) ~> {
      check {
        val actual = response.entity.toStrict(1.seconds).map(_.data.utf8String).futureValue
        
        status.intValue shouldBe 200
        assert(actual === expected)
      }
    }
  
    class TestRoute extends ChainingSyntax {
      self: ByrdeHttpServer#ByrdeRoutesMixin =>
      
      case class Test(code: Int, example: String, example1: String) extends ByrdeResponse
  
      private lazy val mapper: EndpointOutput.OneOf[ByrdeErrorResponse, ByrdeErrorResponse] =
        sttp.tapir.oneOf[ByrdeErrorResponse](
          statusMappingValueMatcher(StatusCode.Unauthorized, jsonBody[ByrdeErrorResponse].description("Unauthorized!")) {
            case ex: ByrdeErrorResponse if ex.code == errorCode + 1 => true
          },
          statusMappingValueMatcher(StatusCode.Forbidden, jsonBody[ByrdeErrorResponse].description("Forbidden!")) {
            case ex: ByrdeErrorResponse if ex.code == errorCode + 2 => true
          }
        )
    
      private lazy val test: ByrdeRoute[Unit, ByrdeErrorResponse, Test, AkkaStreams with capabilities.WebSockets] =
        endpoint[Test](mapper = mapper)
          .in("test")
          .toTapirRoute {
            () =>
              Future
                .successful(Right(("Hello World!", "Goodbye World!")))
                .toOut {
                  case ((example, example1), code) =>
                    Test(code, example, example1)
                }
          }
    
      override lazy val routes: ByrdeRoutes = test
    }
  
    override lazy val provider: Provider =
      new TestProvider
  
    private lazy val routes =
      Seq(new TestRoute with ByrdeRoutesMixin)
  }
}
