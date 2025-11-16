package com.example.listitaapp.data.datasource

import com.example.listitaapp.data.dto.ErrorResponse
import com.squareup.moshi.Moshi
import retrofit2.Response

abstract class BaseRemoteDataSource(
    private val moshi: Moshi
) {

    protected fun <T> handleResponse(response: Response<T>, defaultMessage: String): T {
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!
        }
        throw Exception(parseErrorMessage(response, defaultMessage))
    }

    protected fun handleUnitResponse(response: Response<Unit>, defaultMessage: String) {
        if (response.isSuccessful) return
        throw Exception(parseErrorMessage(response, defaultMessage))
    }

    private fun parseErrorMessage(response: Response<*>, defaultMessage: String): String {
        return try {
            response.errorBody()?.string()?.let { body ->
                val adapter = moshi.adapter(ErrorResponse::class.java)
                adapter.fromJson(body)?.message
            } ?: response.message().ifEmpty { defaultMessage }
        } catch (_: Exception) {
            response.message().ifEmpty { defaultMessage }
        }
    }
}
