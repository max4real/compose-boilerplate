package com.max4real.compose_boilerplate.shared.model

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.max4real.compose_boilerplate.BuildConfig
import com.max4real.compose_boilerplate.shared.util.mylog
import retrofit2.Response

private const val GENERIC_API_ERROR = "Something went wrong. Please try again."

// Exception messages (e.g. from IOException/HttpException) can contain hostnames, endpoint
// paths, and raw Java text. Only surface them to the UI on debug builds; log them otherwise.
fun Throwable.toSafeMessage(fallback: String): String {
    mylog("${this::class.simpleName}: $localizedMessage")
    return if (BuildConfig.DEBUG) localizedMessage ?: fallback else fallback
}

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val code: String? = null,
    val error: String? = null
)

data class Metadata(
    val statusCode: Int, val message: String
)

data class ErrorMeta(
    val message: JsonElement?,            // can be "string" OR ["a","b"]
    val description: String?, val code: Int?, val statusCode: Int?
) {
    fun extractMessage(): String = when {
        message == null -> description ?: "Unknown error"
        message.isJsonPrimitive -> message.asString
        message.isJsonArray && message.asJsonArray.size() > 0 -> message.asJsonArray.joinToString("; ") { it.asString }

        else -> description ?: "Unknown error"
    }
}

data class ErrorResponse(
    @SerializedName("_metadata") val metadata: ErrorMeta
)

// ---------- universal extension  ------------------------------------------
// Extension to extract the error string from the Retrofit Response
fun <T> Response<T>.parseErrorMessage(): String {
    val errorJson = errorBody()?.string()
    if (errorJson == null) {
        mylog("parseErrorMessage: empty error body (HTTP ${code()})")
        return GENERIC_API_ERROR
    }
    return try {
        // Parse the JSON into the ApiResponse structure
        val apiErr = Gson().fromJson(errorJson, ApiResponse::class.java)
        // apiErr.error is a message our own backend wrote for end users — safe to show as-is.
        apiErr.error ?: run {
            mylog("parseErrorMessage: no error field (HTTP ${code()}), body=$errorJson")
            GENERIC_API_ERROR
        }
    } catch (e: Exception) {
        // Body wasn't the expected {success, error, ...} object — it may be a
        // bare JSON string (e.g. from a proxy/body-size-limit error) or plain text.
        // Don't surface raw body content to the UI; log it for debugging instead.
        mylog("parseErrorMessage: unparsable body (HTTP ${code()}): $errorJson")
        GENERIC_API_ERROR
    }
}