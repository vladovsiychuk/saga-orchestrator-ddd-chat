package com.rest_service

import com.rest_service.constant.RoomConstant
import com.rest_service.constant.UserConstant
import com.rest_service.repository.RoomRepository
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
class RoomControllerIntegrationTest extends Specification {

    @Inject
    @Client("/v1/rooms")
    ReactorHttpClient client

    @Inject
    RoomRepository repository

    def conditions = new PollingConditions(timeout: 5, initialDelay: 0.5, factor: 1.25)

    void "GET should return list of user's rooms"() {
        when:
        def request = HttpRequest.GET("/").bearerAuth(UserConstant.USER_1_TOKEN)
        def response = client.toBlocking().exchange(request, List)

        then:
        response.body() == [
            [
                id         : RoomConstant.ROOM_1_ID,
                name       : "room-1",
                createdBy  : UserConstant.USER_1_ID,
                members    : [UserConstant.USER_3_ID, UserConstant.USER_1_ID],
                dateCreated: 1,
                dateUpdated: 1,
            ]
        ]
    }

    void "POST should create new room"() {
        given:
        def command = [
            userId: UserConstant.USER_2_ID
        ]

        when:
        def request = HttpRequest.POST("/", command).bearerAuth(UserConstant.USER_1_TOKEN)
        def response = client.toBlocking().exchange(request, Object)

        then:
        response.body().createdBy == UserConstant.USER_1_ID

        cleanup:
        repository.deleteById(UUID.fromString(response.body().id))
    }
}
