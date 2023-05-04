package com.rest_service

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
        def request = HttpRequest.GET("/").bearerAuth(TestConstant.USER_1_TOKEN)
        def response = client.toBlocking().exchange(request, List)

        then:
        response.body() == [
                [
                        id         : "a0e7fde3-a4ea-45c1-80bd-bcd02fe20c60",
                        name       : "room-1",
                        createdBy  : "e83e9450-e60a-46bc-aa26-74a3152312d1",
                        dateCreated: 1,
                        dateUpdated: 1,
                ]
        ]
    }

    void "POST should create new room"() {
        given:
        def command = [
                userId: "d2b3e3fb-c4a2-40a8-a4ca-ad276b34b81a"
        ]

        when:
        def request = HttpRequest.POST("/", command).bearerAuth(TestConstant.USER_1_TOKEN)
        def response = client.toBlocking().exchange(request, Object)

        then:
        response.body().createdBy == "e83e9450-e60a-46bc-aa26-74a3152312d1"

        cleanup:
        repository.deleteById(UUID.fromString(response.body().id))
    }
}
