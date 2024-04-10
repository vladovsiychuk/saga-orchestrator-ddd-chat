package com.rest_service.read_service.repository

import com.rest_service.read_service.entity.RoomMember
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@R2dbcRepository(dialect = Dialect.MYSQL)
interface RoomMembersRepository : ReactorCrudRepository<RoomMember, UUID> {
    fun deleteByRoomId(roomId: UUID): Mono<Long>
    fun findByRoomId(roomId: UUID): Mono<RoomMember>
    fun findByMemberId(memberId: UUID): Flux<RoomMember>
}
