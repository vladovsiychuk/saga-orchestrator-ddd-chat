package com.saga_orchestrator_ddd_chat.read_service.repository

import com.saga_orchestrator_ddd_chat.read_service.entity.MessageView
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@R2dbcRepository(dialect = Dialect.MYSQL)
interface MessageViewRepository : ReactorCrudRepository<MessageView, UUID> {
    fun update(message: MessageView): Mono<MessageView>
    fun findByRoomIdOrderByDateCreated(roomId: UUID): Flux<MessageView>
}
