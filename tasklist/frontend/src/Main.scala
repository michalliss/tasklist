package tasklist.frontend

import zio.*

object Main extends ZIOAppDefault {
  def run = ZIO
    .serviceWithZIO[App](_.run)
    .provide(
      ZLayer.derive[App],
      ZLayer.derive[pages.HomePage],
      ZLayer.derive[pages.TodoListPage],
      ZLayer.derive[pages.LoginPage],
      ZLayer.derive[pages.RegisterPage],
      ZLayer.derive[services.HttpClient],
      ZLayer.derive[services.Router],
      ZLayer.derive[services.AuthService],
      ZLayer.derive[services.StorageService],
      ZLayer.derive[components.Layout]
    )
}
