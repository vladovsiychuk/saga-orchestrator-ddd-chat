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
    String user_token_1
    @Shared
    UUID user_id_1
    @Shared
    UUID user_id_2
    @Shared
    UUID room_id

    def conditions = new PollingConditions(timeout: 5, initialDelay: 0.5, factor: 1.25)


    def setupSpec() {
        user_token_1 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJ1c2VyLTFAZ21haWwuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.B7NnRHclfkrcOgK4HX8fqogY-oq3Hv1GrWTylDqOhrg"
        user_id_1 = UUID.fromString("e83e9450-e60a-46bc-aa26-74a3152312d1")
        user_id_2 = UUID.fromString("d2b3e3fb-c4a2-40a8-a4ca-ad276b34b81a")
        room_id = UUID.fromString("a0e7fde3-a4ea-45c1-80bd-bcd02fe20c60")

        def user_1 = new User(user_id_1, null, "user-1@gmail.com", null, LanguageEnum.ENGLISH, null, UserType.REGULAR_USER, 1, 1)
        def user_2 = new User(user_id_2, null, "user-2@gmail.com", null, LanguageEnum.ENGLISH, null, UserType.REGULAR_USER, 1, 1)

        def member = new Member(UUID.randomUUID(), room_id, user_id_1, 1)
        def room = new Room(room_id, null, user_id_1, 1, 1)

        userRepository.save(user_1).block()
        userRepository.save(user_2).block()

        roomRepository.save(room).block()
        memberRepository.save(member).block()
    }

    void "GET should return list of user's rooms"() {
        when:
        def request = HttpRequest.GET("/").bearerAuth(user_token_1)
        def response = client.toBlocking().exchange(request, List)

        then:
        response.body() == [
            [
                id         : room_id.toString(),
                name       : null,
                createdBy  : user_id_1.toString(),
                dateCreated: 1,
                dateUpdated: 1,
            ]
        ]
    }

    void "POST should create new room"() {
        given:
        def command = [
            userId: user_id_2.toString()
        ]

        when:
        def request = HttpRequest.POST("/", command).bearerAuth(user_token_1)
        def response = client.toBlocking().exchange(request, Object)

        then:
        response.body().createdBy == user_id_1.toString()
    }

    void "only cleanup"() {
        cleanup:
        userRepository.deleteAll().block()
        roomRepository.deleteAll().block()
        memberRepository.deleteAll().block()
    }
}
