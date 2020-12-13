trait Validator[T] {
  def validate(t: T): Option[ApiError]
}

object MessageClassValidator extends Validator[MessageClass] {
  override def validate(messageClass: MessageClass): Option[ApiError] =
    if (messageClass.userName.isEmpty)
      Some(ApiError.emptyNameField)
    else if (messageClass.msg.isEmpty)
      Some(ApiError.emptyMessageField)
    else
      None
}

object StringValidator extends Validator[String] {
  override def validate(string: String): Option[ApiError] =
    if (string.isEmpty)
      Some(ApiError.emptyString)
    else
      None
}
