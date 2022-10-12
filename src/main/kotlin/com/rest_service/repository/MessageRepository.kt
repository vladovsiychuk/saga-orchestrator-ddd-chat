package com.rest_service.repository

import com.rest_service.domain.Message
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository
import reactor.core.publisher.Flux
import java.util.UUID

@JdbcRepository(dialect = Dialect.MYSQL)
interface MessageRepository : ReactiveStreamsCrudRepository<Message, Int> {
    fun findBySenderOrReceiverOrderByDate(sender: UUID, receiver: UUID): Flux<Message>
    override fun findAll(): Flux<Message>
}
