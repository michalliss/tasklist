package tasklist.frontend

import com.raquo.laminar.api.L.*
import com.raquo.waypoint.*
import foxxy.frontend.utils.*
import zio.json.JsonCodec

case class App(homePage: HomePage) {
  enum Page derives JsonCodec:
    case Home

  val router = makeRouter(
    Route.static(Page.Home, root / endOfSegments)
  )

  def renderPage(page: Page) = page.match
    case Page.Home => homePage.render

  def run = makeFrontend(router, renderPage)
}
