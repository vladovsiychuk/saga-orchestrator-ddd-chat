package com.rest_service

import com.rest_service.constant.RoomConstant
import com.rest_service.constant.UserConstant
import com.rest_service.repository.MemberRepository
import com.rest_service.repository.RoomRepository
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
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
    RoomRepository roomRepository

    @Inject
    MemberRepository memberRepository

    def conditions = new PollingConditions(timeout: 5, initialDelay: 0.5, factor: 1.25)

    void "GET should return room by id"() {
        when:
        def request = HttpRequest.GET("/$RoomConstant.ROOM_1_ID").bearerAuth(UserConstant.USER_1_TOKEN)
        def response = client.toBlocking().exchange(request, Object)

        then:
        response.body() == [
            id         : RoomConstant.ROOM_1_ID,
            name       : "room-1",
            createdBy  : UserConstant.USER_1_ID,
            members    : [UserConstant.USER_3_ID, UserConstant.USER_1_ID],
            dateCreated: 1,
            dateUpdated: 1,
        ]
    }

    void "GET should return list of user's rooms"() {
        when:
        def request = HttpRequest.GET("/").bearerAuth(UserConstant.USER_1_TOKEN)
        def response = client.toBlocking().exchange(request, List)

        then:
        response.body().collect { it.id }.sort() == [RoomConstant.ROOM_1_ID, RoomConstant.ROOM_3_ID, RoomConstant.ROOM_4_ID].sort()
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
        roomRepository.deleteById(UUID.fromString(response.body().id)).block()
    }

    void "PUT should add new member to the room"() {
        given:
        def command = [
            userId: UserConstant.USER_2_ID
        ]

        when:
        def request = HttpRequest.PUT("/${RoomConstant.ROOM_1_ID}/members", command).bearerAuth(UserConstant.USER_1_TOKEN)
        def response = client.toBlocking().exchange(request, Object)

        then: "new member should be added"
        response.body().members.sort() == [UserConstant.USER_1_ID, UserConstant.USER_3_ID, UserConstant.USER_2_ID].sort()

        cleanup:
        memberRepository.deleteByUserIdAndRoomId(UUID.fromString(UserConstant.USER_2_ID), UUID.fromString(RoomConstant.ROOM_1_ID))
    }

    void "PUT should throw an error when #cause"() {
        when:
        def request = HttpRequest.PUT("/${roomId}/members", command).bearerAuth(token)
        client.toBlocking().exchange(request, Object)

        then:
        HttpClientResponseException exception = thrown()
        exception.response.code() == expectedCode

        if (exception.response.code() != HttpStatus.UNAUTHORIZED.code)
            exception.message == expectedMessage

        where:
        cause                                 | command                                          | roomId                                 | expectedCode                 | expectedMessage                                                     | token
        "user is already added user"          | [userId: UserConstant.USER_3_ID]                 | RoomConstant.ROOM_1_ID                 | HttpStatus.BAD_REQUEST.code  | "User with id ${UserConstant.USER_3_ID} is already a room member."  | UserConstant.USER_1_TOKEN
        "room does not exist"                 | [userId: UserConstant.USER_2_ID]                 | "9f5cd305-0667-40d6-aef3-09c3c422315a" | HttpStatus.NOT_FOUND.code    | "Room with id 9f5cd305-0667-40d6-aef3-09c3c422315a does not exist." | UserConstant.USER_1_TOKEN
        "user does not exist"                 | [userId: "4a865a22-481c-45a9-bc64-5fb12919eaa1"] | RoomConstant.ROOM_1_ID                 | HttpStatus.NOT_FOUND.code    | "User with id 4a865a22-481c-45a9-bc64-5fb12919eaa1 does not exist." | UserConstant.USER_1_TOKEN
        "not room creator is adding a member" | [userId: UserConstant.USER_2_ID]                 | RoomConstant.ROOM_1_ID                 | HttpStatus.UNAUTHORIZED.code | "User is not room creator."                                         | UserConstant.USER_3_ID
    }
}
