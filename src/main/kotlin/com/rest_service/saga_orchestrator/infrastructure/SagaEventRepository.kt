package com.rest_service.saga_orchestrator.infrastructure

import com.rest_service.commons.enums.EventType
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@R2dbcRepository(dialect = Dialect.MYSQL)
interface SagaEventRepository : ReactorCrudRepository<SagaEvent, UUID> {
    fun save(sagaEvent: SagaEvent): Mono<SagaEvent>
    fun findByOperationIdOrderByDateCreated(operationId: UUID): Flux<SagaEvent>
    fun findByOperationIdAndType(operationId: UUID, type: EventType): Mono<SagaEvent>
}
