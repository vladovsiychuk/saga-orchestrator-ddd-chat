package com.saga_orchestrator_ddd_chat.commons.exception

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
@Requires(classes = [NotFoundException::class, ExceptionHandler::class])
class NotFoundExceptionHandler : ExceptionHandler<NotFoundException, HttpResponse<*>> {

    private val mapper = jacksonObjectMapper()

    override fun handle(request: HttpRequest<*>, exception: NotFoundException): HttpResponse<*> {
        val errorMap = mapOf("status" to HttpStatus.NOT_FOUND.code, "message" to exception.message)

        return HttpResponse.notFound<String>()
            .body(mapper.writeValueAsString(errorMap))
    }
}
