package com.rest_service.read_service.integration_test

import com.rest_service.UserConstant
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.read_service.SagaEventHandler
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

import static com.rest_service.Fixture.anyValidUserDTO

@MicronautTest(transactional = false)
@Requires(env = Environment.TEST)
class ReadServiceIntegrationTest extends Specification {

    @Inject
    @Client("/v1/users")
    ReactorHttpClient client

    @Inject
    SagaEventRepository sagaEventRepository

    @Inject
    SagaEventHandler eventHandler

    def conditions = new PollingConditions(timeout: 5, initialDelay: 0.5, factor: 1.25)

    void "Should create or update user view on user approval event"() {
        given:
        def userId = UUID.randomUUID()
        def userDto = anyValidUserDTO()
        userDto.id = userId
        def event = new SagaEvent(SagaEventType.USER_CREATE_APPROVED, UUID.randomUUID(), ServiceEnum.SAGA_SERVICE, "example@test.com", userId, userDto)

        when:
        eventHandler.messageActionListener(event)

        then:
        conditions.eventually {
            def request = HttpRequest.GET("/$userId").bearerAuth(UserConstant.USER_1_TOKEN)
            def response = client.toBlocking().exchange(request, Map)

            assert response.body().id.toString() == userId.toString()
        }
    }
}
