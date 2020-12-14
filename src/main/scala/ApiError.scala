import akka.http.scaladsl.model.{StatusCode, StatusCodes}

final case class ApiError private(statusCode: StatusCode, message: String)

object ApiError {
  private def apply(statusCode: StatusCode, message: String): ApiError = new ApiError(statusCode, message)

  val generic: ApiError = new ApiError(StatusCodes.InternalServerError, "Unknown error.")

  val emptyNameField: ApiError = new ApiError(StatusCodes.BadRequest, "The name field must not be empty.")
  val emptyMessageField: ApiError = new ApiError(StatusCodes.BadRequest, "The message field must not be empty.")
  val emptyString: ApiError = new ApiError(StatusCodes.BadRequest, "The string must not be empty.")

  def userNotFound(userName: String): ApiError =
    new ApiError(StatusCodes.NotFound, s"The user with username ${userName} could not be found.")

  def userNotSubscribed(userName: String): ApiError =
    new ApiError(StatusCodes.NotFound, s"The user with username ${userName} is not subscribed.")
}