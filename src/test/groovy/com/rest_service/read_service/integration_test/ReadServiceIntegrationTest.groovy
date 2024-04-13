package com.rest_service.read_service.integration_test

import com.rest_service.UserConstant
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.read_service.SagaEventHandler
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.annotation.Client
import io.micronaut.reactor.http.client.ReactorHttpClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import static com.rest_service.Fixture.*

@MicronautTest(transactional = false)
@Requires(env = Environment.TEST)
class ReadServiceIntegrationTest extends Specification {

    @Inject
    @Client("/v1")
    ReactorHttpClient client

    @Inject
    SagaEventHandler eventHandler

    @Shared
    UUID userId = UUID.randomUUID()

    def conditions = new PollingConditions(timeout: 5, initialDelay: 0.5, factor: 1.25)

    void "Should create or update user view on user completed event"() {
        given:
        def userDto = anyValidUserDTO()
        userDto.id = userId
        userDto.email = 'user-1@gmail.com' //needed for the next test
        def event = new SagaEvent(SagaEventType.USER_CREATE_COMPLETED, UUID.randomUUID(), ServiceEnum.SAGA_SERVICE, "example@test.com", userId, userDto)

        when:
        eventHandler.messageActionListener(event)

        then:
        conditions.eventually {
            def request = HttpRequest.GET("/users/$userId").bearerAuth(UserConstant.USER_1_TOKEN)
            def response = client.toBlocking().exchange(request, Map)

            assert response.body().id.toString() == userId.toString()
        }
    }

    void "Should create or update room view on room completed event"() {
        given:
        def roomId = UUID.randomUUID()
        def roomDto = anyValidRoomDTO()
        roomDto.id = roomId
        roomDto.members.add(userId)
        def event = new SagaEvent(SagaEventType.ROOM_CREATE_COMPLETED, UUID.randomUUID(), ServiceEnum.SAGA_SERVICE, "example@test.com", UUID.randomUUID(), roomDto)

        when:
        eventHandler.messageActionListener(event)

        then:
        conditions.eventually {
            def request = HttpRequest.GET("/rooms/$roomId").bearerAuth(UserConstant.USER_1_TOKEN)
            def response = client.toBlocking().exchange(request, Map)

            assert response.body().id.toString() == roomId.toString()
        }
    }

    void "Should create or update message view on message completed event"() {
        given:
        def messageId = UUID.randomUUID()
        def messageDto = anyValidMessageDTO()
        messageDto.id = messageId
        def event = new SagaEvent(SagaEventType.MESSAGE_CREATE_COMPLETED, UUID.randomUUID(), ServiceEnum.SAGA_SERVICE, "example@test.com", UUID.randomUUID(), messageDto)

        when:
        eventHandler.messageActionListener(event)

        then:
        conditions.eventually {
            def request = HttpRequest.GET("/messages/$messageId").bearerAuth(UserConstant.USER_1_TOKEN)
            def response = client.toBlocking().exchange(request, Map)

            assert response.body().id.toString() == messageId.toString()
            assert true
        }
    }
}
