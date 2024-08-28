package tasklist.frontend.components

import be.doeraene.webcomponents.ui5.configkeys.IconName
import be.doeraene.webcomponents.ui5.{Button, ShellBar, SideNavigation}
import com.raquo.laminar.api.L.*
import foxxy.frontend.utils.*
import zio.*
import zio.json.JsonCodec
import zio.stream.ZStream
import tasklist.frontend.services.Router.Page

import tasklist.frontend.services.*

case class Layout(authSerivce: AuthService, router: Router) {
  def layout(content: HtmlElement) = ZIO.attempt {
    val toggleCollapseBus: EventBus[Unit] = new EventBus
    val collapsedSignal                   = toggleCollapseBus.events.scanLeft(false)((collapsed, _) => !collapsed)
    vDiv(
      ShellBar(
        _.primaryTitle      := "Foxxy TODO Reference App",
        _.showCoPilot       := true,
        _.slots.startButton := Button(
          _.icon := IconName.menu,
          _.events.onClick.mapTo(()) --> toggleCollapseBus.writer
        ),
        _.slots.profile     := div(
          authSerivce.isLoggedIn
        )
      ),
      hDiv(
        SideNavigation(
          _.collapsed <-- collapsedSignal,
          _.item(
            _.text := "Home",
            _.icon := IconName.home,
            router.router.navigateTo(Page.Home),
            _.selected <-- router.router.currentPageSignal.map(_ == Page.Home)
          ),
          _.item(
            _.text := "Todos",
            _.icon := IconName.home,
            router.router.navigateTo(Page.TodoList),
            _.selected <-- router.router.currentPageSignal.map(_ == Page.TodoList)
          ),
          _.slots.fixedItems := SideNavigation.item(
            _.text := "Login",
            _.icon := IconName.`user-settings`,
            router.router.navigateTo(Page.Login),
            _.selected <-- router.router.currentPageSignal.map(_ == Page.Login)
          ),
          _.slots.fixedItems := SideNavigation.item(
            _.text := "Register",
            _.icon := IconName.`user-settings`,
            router.router.navigateTo(Page.Register),
            _.selected <-- router.router.currentPageSignal.map(_ == Page.Register)
          ),
          _.slots.fixedItems := SideNavigation.item(
            _.text := "Logout",
            _.icon := IconName.error,
            onClick --> { _ => authSerivce.logout },
            _.href := router.router.absoluteUrlForPage(Page.Home)
          )
        ),
        content
      )
    )
  }
}
