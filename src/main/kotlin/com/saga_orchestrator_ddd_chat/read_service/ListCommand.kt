package com.saga_orchestrator_ddd_chat.read_service

import com.saga_orchestrator_ddd_chat.commons.enums.UserType

data class ListCommand(
    val query: String = "",
    val type: UserType?,
)
