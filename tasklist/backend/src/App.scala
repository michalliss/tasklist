package tasklist.backend

import foxxy.auth.AuthService
import foxxy.backend.*
import foxxy.repo.*
import foxxy.shared.*
import sttp.tapir.*
import sttp.tapir.ztapir.*
import tasklist.backend.db.*
import tasklist.shared.Domain.{TodoItem, User}
import tasklist.shared.Endpoints
import tasklist.shared.Endpoints.TodoResponse
import zio.*

case class App(migrationService: Database.Migration, auth: AuthService, repo: Repository, backend: Backend) {

  def securityLogic(token: String) =
    (for {
      username <- auth.verifyJwt(token)
      user     <- repo.users.byUsername(username)
    } yield user)
      .orElseFail(Unauthorized(""))
      .someOrFail(Unauthorized(""))

  def register: FoxxyServerEndpoint = Endpoints.register
    .zServerLogic { req =>
      for {
        hash  <- auth.encryptPassword(req.password).orElseFail(BadRequest("Failed to hash password"))
        id    <- Random.nextUUID
        user   = User(id, req.name, hash)
        _     <- repo.users.insert(user).orElseFail(BadRequest("Failed to insert user"))
        token <- auth.generateJwt(user.name).orElseFail(BadRequest("Invalid credentials"))
      } yield token
    }

  def login: FoxxyServerEndpoint = Endpoints.login
    .zServerLogic { req =>
      for {
        user  <- repo.users.byUsername(req.name).orElseFail(BadRequest("Failed to find user")).someOrFail(Unauthorized(""))
        _     <- auth.verifyPassword(req.password, user.passwordHash).orElseFail(Unauthorized(""))
        token <- auth.generateJwt(user.name).orElseFail(BadRequest("Failed to generate token"))
      } yield token
    }

  def getTodos: FoxxyServerEndpoint = Endpoints.getTodos
    .zServerSecurityLogic(securityLogic)
    .serverLogic { user => _ =>
      for {
        todos <- repo.todoItems.byUserId(user.id).orElseFail(BadRequest("Invalid credentials"))
      } yield todos.map(x => TodoResponse(x.id, x.text, x.completed))
    }

  def addTodo: FoxxyServerEndpoint = Endpoints.addTodo
    .zServerSecurityLogic(securityLogic)
    .serverLogic { user => req =>
      for {
        id  <- Random.nextUUID
        todo = TodoItem(id, user.id, req.text, completed = false)
        _   <- repo.todoItems.insert(todo).orElseFail(BadRequest("Failed to insert todo"))
      } yield TodoResponse(todo.id, todo.text, todo.completed)
    }

  def updateTodo: FoxxyServerEndpoint = Endpoints.updateTodo
    .zServerSecurityLogic(securityLogic)
    .serverLogic { user => (id, req) =>
      for {
        todo <- repo.todoItems.find(id).orElseFail(BadRequest("Invalid todo id")).someOrFail(BadRequest("Invalid todo id"))
        _    <- repo.todoItems.update(todo.copy(completed = req.completed)).orElseFail(BadRequest("Failed to update todo"))
      } yield TodoResponse(todo.id, todo.text, todo.completed)
    }

  def removeTodo: FoxxyServerEndpoint = Endpoints.removeTodo
    .zServerSecurityLogic(securityLogic)
    .serverLogic { user => id =>
      for {
        todo <- repo.todoItems.find(id).orElseFail(BadRequest("Invalid todo id")).someOrFail(BadRequest("Invalid todo id"))
        _    <- repo.todoItems.delete(todo.id).orElseFail(BadRequest("Failed to delete todo"))
      } yield ()
    }

  def logic = for {
    _  <- migrationService.reset.orDie *> migrationService.migrate.orDie
    id <- Random.nextUUID
    _  <- repo.users.insert(User(id, "admin", "admin")).orDie
    _  <- backend.serve(List(login, register, getTodos, addTodo, updateTodo, removeTodo))
  } yield ()
}

object App {
  val live = ZLayer.derive[App]
}
