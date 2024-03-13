package com.rest_service.commons.exception

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

@Produces
@Singleton
@Requires(classes = [IncorrectInputException::class, ExceptionHandler::class])
class IncorrectInputExceptionHandler : ExceptionHandler<IncorrectInputException, HttpResponse<*>> {

    private val mapper = jacksonObjectMapper()

    override fun handle(request: HttpRequest<*>, exception: IncorrectInputException): HttpResponse<*> {
        val errorMap = mapOf("status" to HttpStatus.FORBIDDEN.code, "message" to exception.message)

        return HttpResponse.badRequest<String>()
            .body(mapper.writeValueAsString(errorMap))
    }
}
