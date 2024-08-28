package tasklist.backend.db

import foxxy.repo.*
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

import java.util.UUID

case class Schema(quill: Quill.Postgres[SnakeCase]) {
  import quill.*

  inline def users     = createEntity[Schema.UserDB]("users")
  inline def todoItems = createEntity[Schema.TodoItemDB]("todo_items")
}

object Schema {
  case class UserDB(id: UUID, name: String, password_hash: String)
  case class TodoItemDB(id: UUID, user_id: UUID, text: String, completed: Boolean)

  given WithId[UserDB] with     { extension (x: UserDB) inline def id = x.id     }
  given WithId[TodoItemDB] with { extension (x: TodoItemDB) inline def id = x.id }

  def live = ZLayer.derive[Schema]
}
