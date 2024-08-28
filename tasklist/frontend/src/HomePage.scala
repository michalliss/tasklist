package tasklist.frontend

import com.raquo.laminar.api.L.*
import zio.*

case class HomePage(httpClient: HttpClient) {
  import httpClient.extensions._
  def render = ZIO.attempt {
    div(
      text <-- Endpoints.todosEndpoint.send(()).map(_.toString()).toEventStream
    )
  }
}
