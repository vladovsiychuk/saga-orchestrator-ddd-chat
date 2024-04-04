package com.rest_service.messaging.room.infrastructure

import io.micronaut.data.annotation.Query
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@R2dbcRepository(dialect = Dialect.MYSQL)
interface RoomDomainEventRepository : ReactorCrudRepository<RoomDomainEvent, UUID> {
    fun save(sagaEvent: RoomDomainEvent): Mono<RoomDomainEvent>
    fun findByOperationIdOrderByDateCreated(operationId: UUID): Flux<RoomDomainEvent>
    fun existsByOperationIdAndType(operationId: UUID, type: RoomDomainEventType): Mono<Boolean>

    @Query(
        """
            SELECT * FROM room_domain_event
            WHERE room_id = :roomId
            AND opration_id NOT IN (
                SELECT operation_id FROM room_domain_event
                WHERE type = 'UNDO'
            )
            ORDER BY date_created
        """
    )
    fun findDomainEvents(roomId: UUID): Flux<RoomDomainEvent>
}
