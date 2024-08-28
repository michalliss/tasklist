package tasklist.frontend.services

case class AuthService(storage: StorageService) {
  def isLoggedIn           = storage.get[String]("token").isDefined
  def login(token: String) = storage.set("token", token)
  def logout               = storage.remove("token")
  def getToken             = storage.get[String]("token")
}
