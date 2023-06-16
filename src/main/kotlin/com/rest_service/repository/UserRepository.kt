package com.rest_service.repository

import com.rest_service.domain.User
import com.rest_service.enums.UserType
import io.micronaut.data.annotation.Query
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

    @Query(
        """
            SELECT * FROM user
            WHERE (type = :type OR :type IS NULL)
            AND email LIKE :query
        """
    )
    fun findByTypeAndEmail(type: UserType?, query: String): Flux<User>

    override fun findById(id: UUID): Mono<User>
}
