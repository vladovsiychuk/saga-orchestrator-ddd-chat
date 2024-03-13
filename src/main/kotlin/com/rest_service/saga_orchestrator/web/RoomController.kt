package com.rest_service.saga_orchestrator.web

import com.rest_service.commons.DomainEvent
import com.rest_service.commons.command.RoomCommand
import com.rest_service.saga_orchestrator.infrastructure.EventFactory
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import reactor.core.publisher.Mono

@Controller("/v1/rooms")
@Secured(SecurityRule.IS_AUTHENTICATED)
class RoomController(
    private val applicationEventPublisher: ApplicationEventPublisher<DomainEvent>,
    private val eventFactory: EventFactory,
) {
    @Post("/")
    fun create(command: RoomCommand): Mono<ResponseDTO> {
        return eventFactory.createStartEvent(command)
            .doOnNext { applicationEventPublisher.publishEventAsync(it) }
            .map { ResponseDTO(it.operationId) }
    }
}
