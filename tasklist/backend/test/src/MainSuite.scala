package tasklist.backend

import foxxy.backend.*
import foxxy.testing.*
import zio.*
import zio.test.*
import zio.test.Assertion.*
import tasklist.shared.Endpoints
import tasklist.shared.Endpoints.LoginRequest
import foxxy.shared.Unauthorized
import tasklist.shared.Endpoints.RegisterRequest

object MainSuite extends ZIOSpecDefault {
  val spec = suite("Simple end to end test")(
    suite("Unauthorized login test")(
      test("Login with invalid credentials should return Unauthorized") {
        for {
          result <- TestClient.send(Endpoints.login, LoginRequest("admin", "admin"))
        } yield assert(result)(isLeft(equalTo(Unauthorized(""))))
      },
      test("Login with empty credentials should return Unauthorized") {
        for {
          result <- TestClient.send(Endpoints.login, LoginRequest("", ""))
        } yield assert(result)(isLeft(equalTo(Unauthorized(""))))
      }
    ),
    suite("Register and login test")(
      test("Register and then login with valid credentials should return Ok") {
        for {
          _      <- TestClient.send(Endpoints.register, RegisterRequest("test", "test"))
          result <- TestClient.send(Endpoints.login, LoginRequest("test", "test"))
        } yield assert(result)(isRight(isNonEmptyString))
      }
    )
  ).provide(
    TestClient.startOnFreePort(
      port => Main.configurableLogic.provide(BackendConfig.withPort(port), PostgresContainer.layer),
      client => client.send(Endpoints.login, LoginRequest("admin", "admin")).unit
    )
  )
    @@ TestAspect.withLiveClock @@ TestAspect.silentLogging
}
