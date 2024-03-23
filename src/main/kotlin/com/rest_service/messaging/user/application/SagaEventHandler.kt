package com.rest_service.messaging.user.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.State
import com.rest_service.commons.command.UserCommand
import com.rest_service.commons.enums.EventType
import com.rest_service.messaging.user.infrastructure.UserDomainEventRepository
import com.rest_service.messaging.user.model.UserDomain
import com.rest_service.messaging.user.model.convertEvent
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@Singleton
@Named("sagaEventHandler")
open class SagaEventHandler(
    private val repository: UserDomainEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher<DomainEvent>,
) : AbstractEventHandler(applicationEventPublisher) {

    private val mapper = jacksonObjectMapper()
    override fun shouldHandle(eventType: EventType): Boolean {
        return eventType in listOf(EventType.USER_CREATE_INITIATE, EventType.USER_CREATE_REJECT)
    }

    override fun rebuildState(event: DomainEvent): Mono<State> {
        val command = mapper.convertValue(event.payload, UserCommand::class.java)
        val operationId = event.operationId

        return Mono.zip(
            rebuildUser(event.responsibleUserEmail, operationId),
            rebuildUser(command.email, operationId)
        ) { responsible, current ->
            current.responsibleUserEmail = responsible.currentUserEmail
            if (responsible.currentUser != null)
                current.responsibleUser = responsible.currentUser
            current
        }
    }

    private fun rebuildUser(email: String, operationId: UUID): Mono<UserDomain> {
        return repository.findByEmailOrderByDateCreated(email) //returns Flux<UserDomainEvent>
            .collectList()
            .flatMap { events ->
                val userDomain = UserDomain(email, operationId)

                if (events.isEmpty())
                    return@flatMap userDomain.toMono()

                events.toFlux()
                    .concatMap { event ->
                        userDomain.apply(event).thenReturn(userDomain)
                    }
                    .last()
            }
    }

    override fun saveEvent(newEvent: DomainEvent): Mono<Boolean> {
        return repository.save(newEvent.convertEvent())
            .thenReturn(true)
    }

    override fun handleError(event: DomainEvent, error: Throwable): Mono<Void> {
        return repository.findByOperationIdAndType(event.operationId, EventType.USER_CREATE_REJECT)
            .switchIfEmpty {
                val errorEvent = DomainEvent(
                    EventType.USER_CREATE_REJECT,
                    event.operationId,
                    event.responsibleService,
                    event.responsibleUserEmail,
                    mapOf("message" to error.message)
                )
                applicationEventPublisher.publishEventAsync(errorEvent)
                Mono.error(error)
            }
            .then()
    }
}
