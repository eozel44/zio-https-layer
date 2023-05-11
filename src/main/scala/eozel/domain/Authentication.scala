package eozel.domain

trait HttpRequest

case class LoginRequest(
  email: String,
  password: String
) extends HttpRequest

case class SignupRequest(email: String, password: String, phone: String, firstName: String, lastName: String)
    extends HttpRequest {
  def asUser[A]: User = User(
    0L,
    email,
    password,
    phone,
    firstName,
    lastName
  )
}
