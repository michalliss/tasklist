package tasklist.frontend.pages

import com.raquo.laminar.api.L.*
import foxxy.frontend.utils.*
import tasklist.frontend.services.HttpClient
import zio.*

case class HomePage(httpClient: HttpClient) {
  import httpClient.extensions._
  def render = ZIO.attempt {
    hDiv(
      vDiv(
        alignItems.center,
        justifyContent.center,
        p("Hello, world!", fontSize := "4em")
      )
    )
  }
}
