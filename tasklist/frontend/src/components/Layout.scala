package tasklist.frontend.components

import be.doeraene.webcomponents.ui5.configkeys.{IconName, ListSeparator, PopoverPlacementType}
import be.doeraene.webcomponents.ui5.{Icon, Popover, ShellBar, SideNavigation, UList}
import com.raquo.laminar.api.L.*
import foxxy.frontend.utils.*
import org.scalajs.dom.HTMLElement
import tasklist.frontend.services.*
import tasklist.frontend.services.Router.Page
import zio.*

case class Layout(authSerivce: AuthService, router: Router) {
  def layout(content: HtmlElement) = ZIO.attempt {
    val toggleCollapseBus: EventBus[Unit]     = new EventBus
    val collapsedSignal                       = toggleCollapseBus.events.scanLeft(false)((collapsed, _) => !collapsed)
    val openPopoverBus: EventBus[HTMLElement] = new EventBus
    val profileId                             = "shellbar-profile-id"
    vDiv(
      Popover(
        _.id            := profileId,
        _.showAtFromEvents(openPopoverBus.events),
        _.placementType := PopoverPlacementType.Bottom,
        div(
          UList(
            _.separators := ListSeparator.None,
            if authSerivce.isLoggedIn then {
              _.item(
                _.icon := IconName.log,
                "Sign out",
                onClick --> { _ =>
                  {
                    authSerivce.logout
                    router.router.pushState(Page.Home)
                  }
                }
              )
            } else emptyMod,
            if !authSerivce.isLoggedIn then {
              _.item(
                _.icon := IconName.log,
                "Log in",
                router.router.navigateTo(Page.Login)
              )
            } else emptyMod,
            if !authSerivce.isLoggedIn then {
              _.item(
                _.icon := IconName.log,
                "Register",
                router.router.navigateTo(Page.Register)
              )
            } else emptyMod
          )
        )
      ),
      ShellBar(
        _.primaryTitle  := "Tasklist",
        _.showCoPilot   := true,
        _.slots.logo    := Icon(_.name := IconName.task),
        _.slots.profile := p(
          Icon(_.name := IconName.`user-settings`)
        ),
        _.events.onProfileClick.map(_.detail.targetRef) --> openPopoverBus.writer
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
            _.icon := IconName.`activity-items`,
            router.router.navigateTo(Page.TodoList),
            _.selected <-- router.router.currentPageSignal.map(_ == Page.TodoList)
          )
        ),
        content
      )
    )
  }
}
