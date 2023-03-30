package com.rest_service.repository

import com.rest_service.domain.Room
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import reactor.core.publisher.Mono
import java.util.UUID
import javax.transaction.Transactional

@R2dbcRepository(dialect = Dialect.MYSQL)
interface RoomRepository : ReactorCrudRepository<Room, UUID> {
    @Transactional
    fun save(room: Room): Mono<Room>
}
