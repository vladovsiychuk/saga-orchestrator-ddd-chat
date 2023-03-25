package com.rest_service.repository

import com.rest_service.domain.Message
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import reactor.core.publisher.Flux
import java.util.UUID

@R2dbcRepository(dialect = Dialect.MYSQL)
interface MessageRepository : ReactorCrudRepository<Message, Int> {
    fun findBySenderOrReceiverOrderByDate(sender: UUID, receiver: UUID): Flux<Message>
    override fun findAll(): Flux<Message>
}
