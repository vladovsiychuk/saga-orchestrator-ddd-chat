package com.rest_service.repository

import com.rest_service.domain.User
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@R2dbcRepository(dialect = Dialect.MYSQL)
interface UserRepository : ReactorCrudRepository<User, UUID> {
    fun save(user: User): Mono<User>
    fun findByEmail(email: String): Mono<User>

    fun findByEmailIlike(email: String): Flux<User>

    override fun findById(id: UUID): Mono<User>
}
