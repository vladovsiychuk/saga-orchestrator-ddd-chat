package com.saga_orchestrator_ddd_chat.messaging.message.infrastructure

import io.micronaut.data.annotation.Query
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@R2dbcRepository(dialect = Dialect.MYSQL)
interface MessageDomainEventRepository : ReactorCrudRepository<MessageDomainEvent, UUID> {
    fun save(sagaEvent: MessageDomainEvent): Mono<MessageDomainEvent>
    fun existsByOperationIdAndType(operationId: UUID, type: MessageDomainEventType): Mono<Boolean>

    @Query(
        """
            SELECT * FROM message_domain_event
            WHERE message_id = :messageId
            AND operation_id NOT IN (
                SELECT operation_id FROM message_domain_event
                WHERE type = 'UNDO'
            )
            ORDER BY date_created
        """
    )
    fun findDomainEvents(messageId: UUID): Flux<MessageDomainEvent>
}
