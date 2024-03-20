package com.rest_service.saga_orchestrator.web

import com.rest_service.commons.DomainEvent
import com.rest_service.commons.command.UserCommand
import com.rest_service.commons.enums.UserType
import com.rest_service.saga_orchestrator.infrastructure.EventFactory
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import java.util.UUID
import reactor.core.publisher.Mono

@Controller("/v1/users")
@Secured(SecurityRule.IS_AUTHENTICATED)
class UserController(
    private val applicationEventPublisher: ApplicationEventPublisher<DomainEvent>,
    private val eventFactory: EventFactory,
) {
    @Post("/currentUser")
    fun create(command: UserCommand): Mono<ResponseDTO> {
        if (command.type == UserType.TRANSLATOR) {
            command.translationLanguages = command.translationLanguages ?: mutableSetOf()
            command.translationLanguages!!.add(command.primaryLanguage)
        }

        command.id = UUID.randomUUID()

        return command.validate()
            .flatMap { eventFactory.createStartEvent(command) }
            .doOnNext { applicationEventPublisher.publishEventAsync(it) }
            .map { ResponseDTO(it.operationId) }
    }
}
