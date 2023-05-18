package eozel.domain

import enumeratum._

import java.time.Instant

sealed trait UserFileType extends EnumEntry

case object UserFileType extends Enum[UserFileType] with CirceEnum[UserFileType] {
  case object Photo       extends UserFileType
  case object Certificate extends UserFileType
  val values = findValues
}

case class UserFile(
  id: Long,
  userId: Long,
  fileType: UserFileType,
  path: String,
  description: Option[String],
  created: Instant
)
