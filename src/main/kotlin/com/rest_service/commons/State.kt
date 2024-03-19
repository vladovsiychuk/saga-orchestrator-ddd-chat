package com.rest_service.commons

import reactor.core.publisher.Mono

interface State {
    fun apply(event: DomainEvent): Mono<Boolean>
    fun createNextEvent(): Mono<DomainEvent>
}
