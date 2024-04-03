package com.rest_service.saga_orchestrator.integration_test

import com.rest_service.UserConstant
import com.rest_service.commons.enums.LanguageEnum
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.commons.enums.UserType
import com.rest_service.saga_orchestrator.infrastructure.SagaEventRepository
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.annotation.Client
import io.micronaut.reactor.http.client.ReactorHttpClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@MicronautTest(transactional = false)
@Requires(env = Environment.TEST)
class SagaOrchestratorIntegrationTest extends Specification {

    @Inject
    @Client("/v1/users")
    ReactorHttpClient client

    @Inject
    SagaEventRepository sagaEventRepository

    def conditions = new PollingConditions(timeout: 5, initialDelay: 0.5, factor: 1.25)

    void "Creating a new user should create the expected saga event"() {
        given: 'a user command'
        def command = [
            type           : UserType.REGULAR_USER.toString(),
            primaryLanguage: LanguageEnum.ENGLISH.toString(),
            temporaryId    : UUID.randomUUID().toString()
        ]

        when: 'calling the endpoint'
        def request = HttpRequest.POST("/currentUser", command).bearerAuth(UserConstant.USER_1_TOKEN)
        def response = client.toBlocking().exchange(request, Map)

        then: 'the response contains the operation id'
        def operationId = response.body().operationId.toString()
        operationId != null

        and: 'the expected event is created'
        conditions.eventually {
            def event = sagaEventRepository.findByOperationIdOrderByDateCreated(UUID.fromString(operationId)).blockFirst()
            assert event.responsibleService == ServiceEnum.SAGA_SERVICE
            assert event.type == SagaEventType.USER_CREATE_START
        }
    }
}
