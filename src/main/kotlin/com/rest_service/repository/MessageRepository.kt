package com.rest_service.repository

import com.rest_service.domain.Message
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@JdbcRepository(dialect = Dialect.MYSQL)
interface MessageRepository : ReactiveStreamsCrudRepository<Message, Int> {
    fun findBySenderNameOrReceiverName(sender: String, receiver: String): Flux<Message>
    override fun findAll(): Flux<Message>
//    fun save(message: Message) : Mono<Message>
}
