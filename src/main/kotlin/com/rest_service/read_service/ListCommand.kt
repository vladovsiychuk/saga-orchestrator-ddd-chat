package com.rest_service.read_service

import com.rest_service.commons.enums.UserType

data class ListCommand(
    val query: String = "",
    val type: UserType?,
)
