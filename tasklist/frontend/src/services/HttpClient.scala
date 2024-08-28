package tasklist.frontend.services
import foxxy.shared.{DefaultErrors, Unauthorized}
import sttp.model.Uri
import sttp.tapir.Endpoint
import zio.*

case class HttpClient(authService: AuthService, router: Router) {
  def host       = "localhost:8080"
  def fetchToken = authService.getToken

  object extensions {
    extension [T1, T2, T3](endpoint: Endpoint[Unit, T1, DefaultErrors, T3, Any])
      def send = { foxxy.frontend.utils.send(endpoint)(Uri(host)) }

    extension [T1, T2, T3](endpoint: Endpoint[String, T1, DefaultErrors, T3, Any])
      def sendSecure = {
        foxxy.frontend.utils
          .sendSecure(endpoint)(Uri(host))(fetchToken.getOrElse("no_token"))
          .andThen(x =>
            x.tapSome { case Left(Unauthorized(msg)) =>
              ZIO.attempt { authService.logout; router.router.pushState(Router.Page.Home) }
            }
          )
      }
  }
}
