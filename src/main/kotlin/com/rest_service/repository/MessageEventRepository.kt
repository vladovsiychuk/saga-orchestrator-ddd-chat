package com.rest_service.repository

import com.rest_service.domain.MessageEvent
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import java.util.UUID

@R2dbcRepository(dialect = Dialect.MYSQL)
interface MessageEventRepository : ReactorCrudRepository<MessageEvent, UUID> {
}
