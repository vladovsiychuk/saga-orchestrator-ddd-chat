package com.rest_service.repository

import com.rest_service.entity.Member
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import java.util.UUID
import javax.transaction.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@R2dbcRepository(dialect = Dialect.MYSQL)
interface MemberRepository : ReactorCrudRepository<Member, UUID> {
    fun findByUserId(userId: UUID): Flux<Member>

    fun findByRoomId(roomId: UUID): Flux<Member>

    @Transactional
    fun save(member: Member): Mono<Member>

    //Used only for testing
    fun deleteByUserIdAndRoomId(userId: UUID, roomId: UUID): Mono<Long>

    fun deleteByRoomId(roomId: UUID): Mono<Long>
}
