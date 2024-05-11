package com.saga_orchestrator_ddd_chat.saga_orchestrator.infrastructure

import com.saga_orchestrator_ddd_chat.commons.enums.SagaEventType
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@R2dbcRepository(dialect = Dialect.MYSQL)
interface SagaEventRepository : ReactorCrudRepository<SagaDomainEvent, UUID> {
    fun save(sagaDomainEvent: SagaDomainEvent): Mono<SagaDomainEvent>
    fun findByOperationIdOrderByDateCreated(operationId: UUID): Flux<SagaDomainEvent>
    fun existsByOperationIdAndType(operationId: UUID, type: SagaEventType): Mono<Boolean>
}
