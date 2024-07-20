package com.personik.partyplanner.servicetests

import com.personik.partyplanner.repository.inmemory.InMemoryUserRepositoryImpl
import com.personik.partyplanner.service.impl.UserServiceImpl
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class UserServiceImplTest {
    private lateinit var userService: UserServiceImpl
    private lateinit var repository: InMemoryUserRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = mockk(relaxed = true)
        userService = UserServiceImpl()
    }

    @Test
    fun `createUser should create a new user with unique hotel room`() {
        val chatId = 12345L
        val user = userService.createUser(chatId)

        every { repository.createUser(any(), any()) } returns Unit

        assertEquals(chatId, user.id)
        assertEquals(1, user.hotelRoom)
    }

    @Test
    fun `isUserExist should return true if user exists`() {
        val chatId = 12345L
        userService.createUser(chatId)
        every { repository.isUserExistById(chatId) } returns true
        assertTrue(userService.isUserExist(chatId))
    }

    @Test
    fun `savePendingPartyId should save pending party ID`() {
        val userId = 12345L
        val partyId = 678

        every { repository.savePendingPartyId(userId, partyId) } returns Unit

        userService.savePendingPartyId(userId, partyId)

    }

    @Test
    fun `getPendingPartyId should return the correct party ID`() {
        val userId = 12345L
        val expectedPartyId = 678

        every { repository.getPendingPartyId(userId) } returns expectedPartyId

        userService.savePendingPartyId(userId, 678)
        val partyId = userService.getPendingPartyId(userId)

        assertEquals(expectedPartyId, partyId)
    }

}