package com.rest_service

import com.rest_service.domain.Member
import com.rest_service.domain.Room
import com.rest_service.domain.User
import com.rest_service.enums.LanguageEnum
import com.rest_service.enums.UserType
import com.rest_service.repository.MemberRepository
import com.rest_service.repository.RoomRepository
import com.rest_service.repository.UserRepository
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

@MicronautTest(transactional = false)
@Requires(env = Environment.TEST)
class RoomControllerIntegrationTest extends Specification {

    @Inject
    @Client("/v1/rooms")
    ReactorHttpClient client

    @Inject
    @Shared
    UserRepository userRepository

    @Inject
    @Shared
    RoomRepository roomRepository

    @Inject
    @Shared
    MemberRepository memberRepository

    @Shared
    String user_token
    @Shared
    UUID user_id
    @Shared
    UUID room_id

    def conditions = new PollingConditions(timeout: 5, initialDelay: 0.5, factor: 1.25)


    def setupSpec() {
        user_token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJ1c2VyLTFAZ21haWwuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.B7NnRHclfkrcOgK4HX8fqogY-oq3Hv1GrWTylDqOhrg"
        user_id = UUID.fromString("e83e9450-e60a-46bc-aa26-74a3152312d1")
        room_id = UUID.fromString("a0e7fde3-a4ea-45c1-80bd-bcd02fe20c60")

        def user = new User(user_id, null, "user-1@gmail.com", null, LanguageEnum.ENGLISH, null, UserType.REGULAR_USER, 1, 1)
        def member = new Member(UUID.randomUUID(), room_id, user_id, 1)
        def room = new Room(room_id, null, user_id, 1, 1)

        userRepository.save(user).block()
        roomRepository.save(room).block()
        memberRepository.save(member).block()
    }

    def cleanupSpec() {
        userRepository.deleteAll().block()
        roomRepository.deleteAll().block()
        memberRepository.deleteAll().block()
    }

    void "GET should return list of user's rooms"() {
        when:
        def request = HttpRequest.GET("/").bearerAuth(user_token)
        def response = client.toBlocking().exchange(request, List)

        then:
        response.body() == [
            [
                id         : room_id.toString(),
                name       : null,
                createdBy  : user_id.toString(),
                dateCreated: 1,
                dateUpdated: 1,
            ]
        ]
    }

    void "only cleanup"() {
        cleanup:
        userRepository.deleteAll().block()
        roomRepository.deleteAll().block()
        memberRepository.deleteAll().block()
    }
}
