package com.rest_service.repository

import com.rest_service.domain.MessageEvent
import com.rest_service.dto.MessageProjectionDTO
import io.micronaut.data.annotation.Query
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import reactor.core.publisher.Flux
import java.util.*

@R2dbcRepository(dialect = Dialect.MYSQL)
interface MessageEventRepository : ReactorCrudRepository<MessageEvent, UUID> {

    @Query(
        """
        SELECT me.message_id 
        FROM message_event me
        INNER JOIN (
            SELECT message_id, MAX(date_created) as max_date
            FROM message_event
            WHERE room_id = :roomId
            GROUP BY message_id
        ) grouped_event
        ON me.message_id = grouped_event.message_id
        AND me.date_created = grouped_event.max_date
        AND me.room_id = :roomId
        ORDER BY me.date_created
        """
    )
    fun findProjectionMessage(roomId: UUID): Flux<MessageProjectionDTO>

    @Query(
        """
        SELECT me.message_id 
        FROM message_event me
        INNER JOIN (
            SELECT message_id, MAX(date_created) as max_date
            FROM message_event
            WHERE room_id = :roomId
            GROUP BY message_id
        ) grouped_event
        ON me.message_id = grouped_event.message_id
        AND me.date_created = grouped_event.max_date
        AND me.room_id = :roomId
        ORDER BY me.date_created
        LIMIT :limit
        """
    )
    fun findProjectionMessageWithLimit(roomId: UUID, limit: Int): Flux<MessageProjectionDTO>

    fun findByMessageIdOrderByDateCreated(messageId: UUID): Flux<MessageEvent>
}
