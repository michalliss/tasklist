package tasklist.backend.db

import foxxy.repo.*
import io.getquill.*
import tasklist.shared.Domain.*
import zio.*

import java.util.UUID
import javax.sql.DataSource

case class Repository(schema: Schema, dataSource: DataSource) {

  import schema.quill.*

  object users {
    private def toDomain(db: Schema.UserDB) = User(db.id, db.name, db.password_hash)
    private def toDb(domain: User)          = Schema.UserDB(domain.id, domain.name, domain.passwordHash)
    private def c                           = crud(dataSource, schema.users)

    def byUsername(username: String) = run(schema.users.filter(_.name == lift(username)).take(1)).map(_.headOption.map(toDomain))
    def find(id: UUID)               = c.find(id).map(_.map(toDomain))
    def update(user: User)           = c.update(toDb(user))
    def insert(user: User)           = c.insert(toDb(user))
    def delete(id: UUID)             = c.delete(id)
  }

  object todoItems {
    private def toDomain(db: Schema.TodoItemDB) = TodoItem(db.id, db.user_id, db.text, db.completed)
    private def toDb(domain: TodoItem)          = Schema.TodoItemDB(domain.id, domain.userId, domain.text, domain.completed)
    private def c                               = crud(dataSource, schema.todoItems)

    def byUserId(userId: UUID)     = run(schema.todoItems.sortBy(_.id).filter(_.user_id == lift(userId))).map(_.map(toDomain))
    def find(id: UUID)             = c.find(id).map(_.map(toDomain))
    def update(todoItem: TodoItem) = c.update(toDb(todoItem))
    def insert(todoItem: TodoItem) = c.insert(toDb(todoItem))
    def delete(id: UUID)           = c.delete(id)
  }
}

object Repository {
  val live = ZLayer.derive[Repository]
}
