package tasklist.frontend

import zio.*

object Main extends ZIOAppDefault {
  def run = ZIO
    .serviceWithZIO[App](_.run)
    .provide(
      ZLayer.derive[App],
      ZLayer.derive[HomePage],
      ZLayer.derive[HttpClient]
    )
}
