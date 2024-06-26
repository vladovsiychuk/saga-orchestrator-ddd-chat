package com.saga_orchestrator_ddd_chat.messaging.user.infrastructure

import io.micronaut.data.annotation.Query
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@R2dbcRepository(dialect = Dialect.MYSQL)
interface UserDomainEventRepository : ReactorCrudRepository<UserDomainEvent, UUID> {
    fun save(sagaEvent: UserDomainEvent): Mono<UserDomainEvent>

    @Query(
        """
            SELECT * FROM user_domain_event
            WHERE user_id = :userId
            AND operation_id NOT IN (
                SELECT operation_id FROM user_domain_event
                WHERE type = 'UNDO'
            )
            ORDER BY date_created
        """
    )
    fun findDomainEvents(userId: UUID): Flux<UserDomainEvent>

    fun existsByOperationIdAndType(operationId: UUID, type: UserDomainEventType): Mono<Boolean>
}
