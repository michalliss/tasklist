package tasklist.frontend

import com.raquo.laminar.api.L.*
import foxxy.frontend.utils.*

import services.Router.Page
import services.Router.Page.*
import services.Router
import zio.*

case class App(
    router: Router,
    layout: components.Layout,
    homePage: pages.HomePage,
    todoListPage: pages.TodoListPage,
    loginPage: pages.LoginPage,
    registerPage: pages.RegisterPage,
    authService: services.AuthService
) {

  def auth[R](render: ZIO[R, Throwable, HtmlElement]) = {
    if (authService.isLoggedIn) then render
    else { ZIO.attempt { router.router.pushState(Page.Login); div() } }
  }

  def renderPage(page: Page) = page.match
    case Home     => homePage.render.flatMap(layout.layout)
    case TodoList => todoListPage.render.flatMap(layout.layout)
    case Login    => loginPage.render
    case Register => registerPage.render

  def run = makeFrontend(router.router, renderPage)
}
