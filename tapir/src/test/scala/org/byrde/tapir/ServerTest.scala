package org.byrde.tapir

import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest

import org.byrde.support.EitherSupport
import org.byrde.tapir.support.RequestIdSupport.IdHeader

import io.circe.Json
import io.circe.generic.auto._

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.ChainingSyntax

class ServerTest extends AnyFlatSpec with Matchers with ScalaFutures with ScalatestRouteTest with EitherSupport {
  class TestRoute(fn: Unit => Future[Either[TapirErrorResponse, TapirResponse.Default]]) extends ChainingSyntax {
    self: Server#TapirRoutesMixin =>
    
    private lazy val test: TapirRoute =
      endpoint[TapirResponse.Default]
        .in("test")
        .toTapirRoute(fn)
    
    override lazy val routes: TapirRoutes = test
  }
  
  trait TestServer extends Server {
    override lazy val provider: Provider =
      new TestProvider
  
    override lazy val mapper: EndpointOutput.OneOf[TapirErrorResponse, TapirErrorResponse] =
      sttp.tapir.oneOf[TapirErrorResponse](
        statusMappingValueMatcher(StatusCode.BadRequest, jsonBody[TapirErrorResponse].description("Client exception!")) {
          case _: TapirErrorResponse => true
        }
      )
    
    protected lazy val routes: Seq[TapirRoutesMixin] =
      Seq(new TestRoute(test) with TapirRoutesMixin)
  
    protected lazy val test: Unit => Future[Either[TapirErrorResponse, TapirResponse.Default]] =
      _ =>
        Future
          .successful(Right[String, String]("Hello World!"))
          .toOut {
            case (_, code) =>
              TapirResponse.Default(code)
          }
  }
  
  "Server.ping" should "return successfully" in new TestServer {
    HttpRequest(HttpMethods.GET, "/ping") ~> Route.seal(handleTapirRoutes(routes)) ~> {
      check {
        val entity = responseAs[Json].as[TapirResponse.Default].get
        
        assert(response.header[IdHeader].isDefined)
        status.intValue shouldBe 200
        assert(entity.code === SuccessCode)
      }
    }
  }
  
  "Server.routes" should "return a 200 when function completes successfully" in new TestServer {
    HttpRequest(HttpMethods.GET, "/test") ~> Route.seal(handleTapirRoutes(routes)) ~> {
      check {
        val entity = responseAs[Json].as[TapirResponse.Default].get
        
        assert(response.header[IdHeader].isDefined)
        status.intValue shouldBe 200
        assert(entity.code === SuccessCode)
      }
    }
  }
  
  it should "return a 400 when function completes with controlled failure" in new TestServer {
    HttpRequest(HttpMethods.GET, "/test") ~> Route.seal(handleTapirRoutes(routes)) ~> {
      check {
        val entity = responseAs[Json].as[TapirResponse.Default].get
      
        assert(response.header[IdHeader].isDefined)
        status.intValue shouldBe 400
        assert(entity.code === ErrorCode)
      }
    }
  
    override lazy val test: Unit => Future[Either[TapirErrorResponse, TapirResponse.Default]] =
      _ => Future.successful(Left(TapirResponse.Default(ErrorCode)))
  }
  
  it should "return the status specified by the error mapper" in new Server {
    HttpRequest(HttpMethods.GET, "/test") ~> Route.seal(handleTapirRoutes(routes)) ~> {
      check {
        val entity = responseAs[Json].as[TapirResponse.Default].get
        
        assert(response.header[IdHeader].isDefined)
        status.intValue shouldBe 403
        assert(entity.code === ErrorCode + 2)
      }
    }
  
    class TestRoute extends ChainingSyntax {
      self: Server#TapirRoutesMixin =>
    
      case class Test(code: Int, example: String, example1: String) extends TapirResponse
      
      private lazy val test: TapirRoute =
        endpoint[Test]
          .in("test")
          .toTapirRoute { _ =>
            Future
              .successful(Left[Int, (String, String)](ErrorCode + 2))
              .toOut(
                {
                  case ((example, example1), code) =>
                    Test(code, example, example1)
                }, {
                  case (code, _) =>
                    TapirResponse.Default(code)
                }
              )
          }
    
      override lazy val routes: TapirRoutes = test
    }
  
    override lazy val provider: Provider =
      new TestProvider
  
    override lazy val mapper: EndpointOutput.OneOf[TapirErrorResponse, TapirErrorResponse] =
      sttp.tapir.oneOf[TapirErrorResponse](
        statusMappingValueMatcher(StatusCode.Unauthorized, jsonBody[TapirErrorResponse].description("Unauthorized!")) {
          case ex: TapirErrorResponse if ex.code == ErrorCode + 1 => true
        },
        statusMappingValueMatcher(StatusCode.Forbidden, jsonBody[TapirErrorResponse].description("Forbidden!")) {
          case ex: TapirErrorResponse if ex.code == ErrorCode + 2 => true
        }
      )
  
    protected lazy val routes: Seq[TapirRoutesMixin] =
      Seq(new TestRoute with TapirRoutesMixin)
  }
  
  it should "return a custom status code based on the error type when function completes with controlled failure" in new TestServer {
    HttpRequest(HttpMethods.GET, "/test") ~> Route.seal(handleTapirRoutes(routes)) ~> {
      check {
        val entity = responseAs[Json].as[TapirResponse.Default].get

        assert(response.header[IdHeader].isDefined)
        status.intValue shouldBe 401
        assert(entity.code === ErrorCode + 1)
      }
    }
  
    HttpRequest(HttpMethods.GET, "/test") ~> Route.seal(handleTapirRoutes(routes)) ~> {
      check {
        val entity = responseAs[Json].as[TapirResponse.Default].get
      
        assert(response.header[IdHeader].isDefined)
        status.intValue shouldBe 403
        assert(entity.code === ErrorCode + 2)
      }
    }
    
    private var counter = 0
  
    override lazy val mapper: EndpointOutput.OneOf[TapirErrorResponse, TapirErrorResponse] =
      sttp.tapir.oneOf[TapirErrorResponse](
        statusMappingValueMatcher(StatusCode.Unauthorized, jsonBody[TapirErrorResponse].description("Client exception!")) {
          case ex: TapirErrorResponse if ex.code == ErrorCode + 1 => true
        },
        statusMappingValueMatcher(StatusCode.Forbidden, jsonBody[TapirErrorResponse].description("Client exception!")) {
          case ex: TapirErrorResponse if ex.code == ErrorCode + 2 => true
        }
      )

    override lazy val test: Unit => Future[Either[TapirErrorResponse, TapirResponse.Default]] =
      _ =>
        counter
          .pipe(_ + 1)
          .tap(counter = _)
          .pipe(count => Future.successful(Left(TapirResponse.Default(ErrorCode + count))))
  }
  
  it should "return a 500 when function completes with unexpected failure" in new TestServer {
    HttpRequest(HttpMethods.GET, "/test") ~> Route.seal(handleTapirRoutes(routes)) ~> {
      check {
        val entity = responseAs[Json].as[TapirResponse.Default].get
        
        assert(response.header[IdHeader].isDefined)
        status.intValue shouldBe 500
        assert(entity.code === ErrorCode)
      }
    }
    
    override lazy val test: Unit => Future[Either[TapirErrorResponse, TapirResponse.Default]] =
      _ => Future.failed(new Exception("Kaboom!"))
  }
  
  it should "expose Swagger documentation" in new TestServer {
    HttpRequest(HttpMethods.GET, "/docs/index.html?url=/docs/docs.yaml") ~> Route.seal(handleTapirRoutes(routes)) ~> {
      check {
        assert(response.header[IdHeader].isDefined)
        status.intValue shouldBe 200
      }
    }
  }
  
  it should "redirect /docs to Swagger documentation" in new TestServer {
    HttpRequest(HttpMethods.GET, "/docs") ~> Route.seal(handleTapirRoutes(routes)) ~> {
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
        |          description: ''
        |          content:
        |            application/json:
        |              schema:
        |                $ref: '#/components/schemas/Default'
        |        '400':
        |          description: Client exception!
        |          content:
        |            application/json:
        |              schema:
        |                $ref: '#/components/schemas/Default'
        |  /ping:
        |    get:
        |      summary: Say hello!
        |      description: Standard API endpoint to say hello to the server.
        |      operationId: Ping
        |      responses:
        |        '200':
        |          description: ''
        |          content:
        |            application/json:
        |              schema:
        |                $ref: '#/components/schemas/Default'
        |        '400':
        |          description: Client exception!
        |          content:
        |            application/json:
        |              schema:
        |                $ref: '#/components/schemas/Default'
        |components:
        |  schemas:
        |    Default:
        |      required:
        |      - code
        |      type: object
        |      properties:
        |        code:
        |          type: integer
        |""".stripMargin
    
    HttpRequest(HttpMethods.GET, "/docs/docs.yaml") ~> Route.seal(handleTapirRoutes(routes)) ~> {
      check {
        val actual = response.entity.toStrict(1.seconds).map(_.data.utf8String).futureValue
        
        status.intValue shouldBe 200
        assert(actual === expected)
      }
    }
  }
  
  it should "expose OpenAPI documentation (beta)" in new Server {
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
        |          description: ''
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
        |      summary: Say hello!
        |      description: Standard API endpoint to say hello to the server.
        |      operationId: Ping
        |      responses:
        |        '200':
        |          description: ''
        |          content:
        |            application/json:
        |              schema:
        |                $ref: '#/components/schemas/Default'
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
        |components:
        |  schemas:
        |    Default:
        |      required:
        |      - code
        |      type: object
        |      properties:
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
    
    HttpRequest(HttpMethods.GET, "/docs/docs.yaml") ~> Route.seal(handleTapirRoutes(routes)) ~> {
      check {
        val actual = response.entity.toStrict(1.seconds).map(_.data.utf8String).futureValue
        
        status.intValue shouldBe 200
        assert(actual === expected)
      }
    }
  
    class TestRoute extends ChainingSyntax {
      self: Server#TapirRoutesMixin =>
      
      case class Test(code: Int, example: String, example1: String) extends TapirResponse
    
      private lazy val test: TapirRoute =
        endpoint[Test]
          .in("test")
          .toTapirRoute { _ =>
            Future
              .successful(Right(("Hello World!", "Goodbye World!")))
              .toOut {
                case ((example, example1), code) =>
                  Test(code, example, example1)
              }
          }
    
      override lazy val routes: TapirRoutes = test
    }
  
    override lazy val provider: Provider =
      new TestProvider
  
    override lazy val mapper: EndpointOutput.OneOf[TapirErrorResponse, TapirErrorResponse] =
      sttp.tapir.oneOf[TapirErrorResponse](
        statusMappingValueMatcher(StatusCode.Unauthorized, jsonBody[TapirErrorResponse].description("Unauthorized!")) {
          case ex: TapirErrorResponse if ex.code == ErrorCode + 1 => true
        },
        statusMappingValueMatcher(StatusCode.Forbidden, jsonBody[TapirErrorResponse].description("Forbidden!")) {
          case ex: TapirErrorResponse if ex.code == ErrorCode + 2 => true
        }
      )
  
    protected lazy val routes =
      Seq(new TestRoute with TapirRoutesMixin)
  }
}
