package com.rest_service.repository

import com.rest_service.domain.User
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository
import reactor.core.publisher.Mono
import java.util.UUID

@JdbcRepository(dialect = Dialect.MYSQL)
interface UserRepository : ReactiveStreamsCrudRepository<User, UUID> {
    fun save(user: User): Mono<User>
    fun findByEmail(email: String): Mono<User>
}
