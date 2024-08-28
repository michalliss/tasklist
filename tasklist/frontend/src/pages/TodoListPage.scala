package tasklist.frontend.pages

import be.doeraene.webcomponents.ui5.{Button, Input, UList}
import com.raquo.laminar.api.L.*
import foxxy.frontend.utils.*
import tasklist.frontend.services.{AuthService, HttpClient, Router}
import tasklist.shared.Endpoints
import tasklist.shared.Endpoints.{AddTodoRequest, TodoResponse, UpdateTodoRequest}
import zio.*

import java.util.UUID
import be.doeraene.webcomponents.ui5.CheckBox
import be.doeraene.webcomponents.ui5.ListItem
import be.doeraene.webcomponents.ui5.configkeys.IconName
import be.doeraene.webcomponents.ui5.configkeys.ButtonType
import be.doeraene.webcomponents.ui5.configkeys.ButtonDesign
import be.doeraene.webcomponents.ui5.configkeys.ValueState

case class TodoListPage(httpClient: HttpClient, authService: AuthService, router: Router) {
  import httpClient.extensions._

  sealed trait Command;
  object Command {
    case class Add(name: String)                    extends Command
    case class Delete(id: UUID)                     extends Command
    case class Update(id: UUID, completed: Boolean) extends Command
    case class Filter(text: String)                 extends Command
  }

  def render = ZIO.attempt {
    val items  = Var(List.empty[TodoResponse])
    val filter = Var("")

    def fetchItems = Endpoints.getTodos.sendSecure(()).right.tap(x => ZIO.attempt(items.set(x)))

    val commandObserver = Observer[Command] {
      case Command.Add(name)             => (Endpoints.addTodo.sendSecure(AddTodoRequest(name)) *> fetchItems).toFutureUnsafe
      case Command.Delete(id)            => (Endpoints.removeTodo.sendSecure(id) *> fetchItems).toFutureUnsafe
      case Command.Update(id, completed) => {
        println(completed)
        println(id)

        (Endpoints.updateTodo.sendSecure((id, UpdateTodoRequest(completed))) *> fetchItems).debug.toFutureUnsafe

      }
      case Command.Filter(text)          => filter.set(text)
    }

    val filteredItems = items.signal
      .map(_.sortBy(_.completed))
      .combineWith(filter.signal)
      .map { case (items, filter) =>
        items.filter(_.text.contains(filter))
      }

    vDiv(
      alignItems.center,
      padding.em(1),
      vDiv(
        gap.em(1),
        SearchComponent(commandObserver),
        AddComponent(commandObserver),
        child <-- filteredItems.signal.map(items => ListComponent(items, onRemove = commandObserver, onItemUpdate = commandObserver)),
        onMountCallback(_ => fetchItems.toEventStream)
      )
    )
  }

  def SearchComponent(onSearch: Observer[Command.Filter]) = {
    Input(
      width.percent(100),
      placeholder := "Search",
      _.valueState := ValueState.Information,
      onInput.mapToValue.map(Command.Filter.apply) --> onSearch
    )
  }

  def AddComponent(onSubmit: Observer[Command.Add]) = {
    val text = Var("")
    hDivA(
      alignItems.center,
      gap.em(1),
      Input(onChange.mapToValue --> text, width.percent(100)),
      hDivA(
        Button(
          "Add todo",
          onClick.mapTo(Command.Add(text.now())) --> onSubmit,
          _.design := ButtonDesign.Positive,
          _.icon   := IconName.add
        )
      )
    )
  }

  def ItemComponent(item: TodoResponse, onRemove: Observer[Unit], onItemUpdate: Observer[Boolean]) = {
    hDivA(
      alignItems.center,
      gap.em(0.5),
      hDivA(
        alignItems.center,
        Button(_.icon := IconName.delete, onClick.mapToUnit --> onRemove),
        CheckBox(_.checked := item.completed, _.events.onChange.map(_.target.checked) --> onItemUpdate)
      ),
      hDivA(
        alignItems.center,
        p(item.text)
      )
    )
  }

  def ListComponent(items: List[TodoResponse], onRemove: Observer[Command.Delete], onItemUpdate: Observer[Command.Update]) = {
    UList(
      items.map { item =>
        ListItem(
          ItemComponent(
            item,
            onRemove.contramap(_ => Command.Delete(item.id)),
            onItemUpdate.contramap(x => Command.Update(item.id, x))
          )
        )
      }
    )
  }

  val updateTodo = (id: UUID, completed: Boolean) => Endpoints.updateTodo.sendSecure(id, UpdateTodoRequest(completed))
}
