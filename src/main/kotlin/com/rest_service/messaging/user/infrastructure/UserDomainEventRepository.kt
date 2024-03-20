package com.rest_service.messaging.user.infrastructure

import com.rest_service.commons.enums.EventType
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@R2dbcRepository(dialect = Dialect.MYSQL)
interface UserDomainEventRepository : ReactorCrudRepository<UserDomainEvent, UUID> {
    fun save(sagaEvent: UserDomainEvent): Mono<UserDomainEvent>
    fun findByEmailOrderByDateCreated(email: String): Flux<UserDomainEvent>
    fun findByOperationIdAndType(operationId: UUID, type: EventType): Mono<UserDomainEvent>
}
