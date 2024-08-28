package tasklist.frontend.pages

import be.doeraene.webcomponents.ui5.{Button, Input, UList}
import com.raquo.laminar.api.L.*
import foxxy.frontend.utils.*
import tasklist.frontend.services.{AuthService, HttpClient, Router}
import tasklist.shared.Endpoints
import tasklist.shared.Endpoints.{AddTodoRequest, TodoResponse, UpdateTodoRequest}
import zio.*

import java.util.UUID

case class TodoListPage(httpClient: HttpClient, authService: AuthService, router: Router) {
  import httpClient.extensions._

  sealed trait Command;
  object Command {
    case class Add(name: String)    extends Command
    case class Delete(id: UUID)     extends Command
    case class Filter(text: String) extends Command
  }

  def render = ZIO.attempt {
    val items  = Var(List.empty[TodoResponse])
    val filter = Var("")

    def fetchItems = Endpoints.getTodos.sendSecure(()).right.tap(x => ZIO.attempt(items.set(x)))

    val commandObserver = Observer[Command] {
      case Command.Add(name)    => (Endpoints.addTodo.sendSecure(AddTodoRequest(name)) *> fetchItems).toFutureUnsafe
      case Command.Delete(id)   => (Endpoints.removeTodo.sendSecure(id) *> fetchItems).toFutureUnsafe
      case Command.Filter(text) => filter.set(text)
    }

    val filteredItems = items.signal.combineWith(filter.signal).map { case (items, filter) =>
      items.filter(_.text.contains(filter))
    }

    vDiv(
      alignItems.center,
      vDivA(
        width.em(40),
        SearchComponent(commandObserver),
        AddComponent(commandObserver),
        child <-- filteredItems.signal.map(items => ListComponent(items, onRemove = commandObserver)),
        onMountCallback(_ => fetchItems.toEventStream)
      )
    )
  }

  def SearchComponent(onSearch: Observer[Command.Filter]) = {
    Input(
      width.percent(100),
      placeholder := "Search",
      onInput.mapToValue.map(Command.Filter.apply) --> onSearch
    )
  }

  def AddComponent(onSubmit: Observer[Command.Add]) = {
    val text = Var("")
    vDivA(
      Input(onChange.mapToValue --> text, width.percent(100)),
      Button("Add todo", onClick.mapTo(Command.Add(text.now())) --> onSubmit)
    )
  }

  def ItemComponent(item: TodoResponse, onRemove: Observer[Unit]) = {
    hDivA(
      maxWidth.em(40),
      justifyContent.spaceBetween,
      hDivA(
        alignItems.center,
        gap.em(0.5),
        // CheckBox(_.checked := todo.completed, 0.mapToValue --> {x => updateAndUpdate(todo.id, x).toFutureUnsafe}),
        p(item.text)
      ),
      Button("Remove", onClick.mapToUnit --> onRemove)
    )
  }

  def ListComponent(items: List[TodoResponse], onRemove: Observer[Command.Delete]) = {
    UList(
      items.map { item =>
        ItemComponent(item, onRemove.contramap(_ => Command.Delete(item.id)))
      }
    )
  }

  val updateTodo = (id: UUID, completed: Boolean) => Endpoints.updateTodo.sendSecure(id, UpdateTodoRequest(completed))
}
