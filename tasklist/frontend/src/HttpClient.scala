package tasklist.frontend

import foxxy.shared.DefaultErrors
import sttp.model.Uri
import sttp.tapir.Endpoint

case class HttpClient() {
  def host = "localhost:8080"

  object extensions {
    extension [T1, T2, T3](endpoint: Endpoint[Unit, T1, DefaultErrors, T3, Any])
      def send = { foxxy.frontend.utils.send(endpoint)(Uri(host)) }
  }
}
