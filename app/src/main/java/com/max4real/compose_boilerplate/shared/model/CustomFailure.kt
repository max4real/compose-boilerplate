package com.max4real.compose_boilerplate.shared.model

sealed class CustomFailure(val errorMessage: String) {
    class NetworkFailure(message: String?) : CustomFailure(message ?: "Network connection failed")
    class ApiFailure(message: String?) : CustomFailure(message ?: "Server error")
    class UnexpectedFailure(message: String?) : CustomFailure(message ?: "Something went wrong")
}

// Usage Example in a Repository/DataSource
/*
fun handleResponse(response: Response<ApiResponse<User, Meta>>): Either<CustomFailure, User> {
    return if (response.isSuccessful && response.body()?.success == true) {
        val data = response.body()?.data
        if (data != null) Either.Right(data)
        else Either.Left(CustomFailure.ApiFailure("Data was null"))
    } else {
        Either.Left(CustomFailure.ApiFailure(response.parseErrorMessage()))
    }
}
*/
class NetworkFailure(
    errorMessage: String?,
) : CustomFailure(errorMessage ?: "Something went wrong!")