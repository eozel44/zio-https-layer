package eozel.domain

case class User(
  id: Long,
  email: String,
  password: String,
  phone: String,
  firstName: String,
  lastName: String
)

case class UserProfile(
  id: Long,
  email: String,
  password: String,
  phone: String,
  firstName: String,
  lastName: String,
  roles: Seq[Role]
)
