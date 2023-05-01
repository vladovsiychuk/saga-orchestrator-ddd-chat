package com.rest_service.command

data class ListCommand(
    val query: String = "",
    val roomLimit: Int?,
)
