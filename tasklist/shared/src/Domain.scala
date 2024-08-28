package tasklist.shared

import java.util.UUID

object Domain {
  case class User(id: UUID, name: String, passwordHash: String)
  case class TodoItem(id: UUID, userId: UUID, text: String, completed: Boolean)
}
