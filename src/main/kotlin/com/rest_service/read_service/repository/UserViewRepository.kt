package com.rest_service.read_service.repository

import com.rest_service.commons.enums.UserType
import com.rest_service.read_service.entity.UserView
import io.micronaut.data.annotation.Query
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@R2dbcRepository(dialect = Dialect.MYSQL)
interface UserViewRepository : ReactorCrudRepository<UserView, UUID> {
    fun update(user: UserView): Mono<UserView>
    fun findByEmail(email: String): Mono<UserView>

    @Query(
        """
            SELECT * FROM user_view
            WHERE (type = :type OR :type IS NULL)
            AND email LIKE :query
        """
    )
    fun findByTypeAndEmail(type: UserType?, query: String): Flux<UserView>
}
