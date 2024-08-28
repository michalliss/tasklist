package tasklist.frontend.services

import com.raquo.waypoint.*
import foxxy.frontend.utils.*
import tasklist.frontend.services.Router.Page
import zio.json.JsonCodec

case class Router() {
  lazy val router = makeRouter(
    Route.static(Page.Home, root / endOfSegments),
    Route.static(Page.Login, root / "login" / endOfSegments),
    Route.static(Page.Register, root / "register" / endOfSegments),
    Route.static(Page.TodoList, root / "todos" / endOfSegments)
  )
}

object Router {
  enum Page derives JsonCodec:
    case Home
    case TodoList
    case Login
    case Register
}
