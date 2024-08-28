package tasklist.frontend

import com.raquo.laminar.api.L.*
import foxxy.frontend.utils.*

import pages.HomePage
import services.Router.Page
import services.Router

case class App(homePage: HomePage, router: Router, layout: components.Layout) {

  def renderPage(page: Page) = page.match
    case Page.Home => homePage.render

  def run = makeFrontend(router.router, renderPage)
}
