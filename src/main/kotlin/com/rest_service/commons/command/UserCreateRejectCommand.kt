package com.rest_service.commons.command

import io.micronaut.core.annotation.Introspected

@Introspected
data class UserCreateRejectCommand(
    val message: String
) : Command
