package com.rest_service.command

import com.rest_service.enums.UserType

data class ListCommand(
    val query: String = "",
    val type: UserType?,
    val roomLimit: Int?,
)
