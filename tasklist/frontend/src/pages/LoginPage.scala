package tasklist.frontend.pages

import be.doeraene.webcomponents.ui5.configkeys.{ButtonType, InputType}
import be.doeraene.webcomponents.ui5.{Button, Input, Toast}
import com.raquo.laminar.api.L.*
import foxxy.frontend.utils.*
import tasklist.frontend.services.{HttpClient, Router, StorageService}
import tasklist.shared.Endpoints
import tasklist.shared.Endpoints.LoginRequest
import zio.*
import tasklist.frontend.services.AuthService

case class LoginPage(router: Router, httpClient: HttpClient, authService: AuthService) {
  import httpClient.extensions._

  sealed trait Command
  object Command {
    case class Login(login: String, password: String) extends Command
  }

  def render = ZIO.attempt {
    val loginVar    = Var("")
    val passwordVar = Var("")
    val messages    = new EventBus[String]

    val commandHandler = Observer[Command] {
      case Command.Login(login, password) => {
        Endpoints.login
          .send(LoginRequest(login, password))
          .some
          .tapError(x => ZIO.attempt { messages.emit("Login failed") })
          .tap(x =>
            ZIO.attempt {
              messages.emit(s"Login succeeded")
              authService.login(x)
              router.router.pushState(Router.Page.TodoList)
            }
          )
          .toFutureUnsafe
      }
    }

    hDiv(
      Toast(_.showFromTextEvents(messages.events)),
      justifyContent.center,
      alignItems.center,
      form(
        onSubmit.preventDefault.mapTo(Command.Login(loginVar.now(), passwordVar.now())) --> commandHandler,
        vDivA(
          width := "300px",
          Input(
            width := "100%",
            _.placeholder := "Login",
            onInput.mapToValue --> loginVar
          ),
          Input(
            width := "100%",
            _.placeholder := "Password",
            _.tpe         := InputType.Password,
            onInput.mapToValue --> passwordVar
          ),
          Button("Login", _.tpe := ButtonType.Submit)
        )
      )
    )
  }
}
