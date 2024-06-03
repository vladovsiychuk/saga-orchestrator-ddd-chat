package com.saga_orchestrator_ddd_chat.commons.exception

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

@Produces
@Singleton
@Requires(classes = [UnauthorizedException::class, ExceptionHandler::class])
class UnauthorizedExceptionHandler : ExceptionHandler<UnauthorizedException, HttpResponse<*>> {


    override fun handle(request: HttpRequest<*>, exception: UnauthorizedException): HttpResponse<*> {
        return HttpResponse.unauthorized<String>()
    }
}
