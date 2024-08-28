package tasklist.frontend.pages

import com.raquo.laminar.api.L.*
import tasklist.frontend.services.HttpClient
import zio.*

case class HomePage(httpClient: HttpClient) {
  import httpClient.extensions._
  def render = ZIO.attempt {
    div(
      "Hello, world!"
    )
  }
}
