package com.rest_service.repository

import com.rest_service.domain.Member
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import javax.transaction.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@R2dbcRepository(dialect = Dialect.MYSQL)
interface MemberRepository : ReactorCrudRepository<Member, UUID> {
    fun findByUserId(userId: UUID): Flux<Member>

    fun findByRoomId(roomId: UUID): Flux<Member>

    @Transactional
    fun save(member: Member): Mono<Member>

    //Used only for testing
    fun deleteByUserIdAndRoomId(userId: UUID, roomId: UUID): Mono<Long>
}
