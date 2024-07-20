package com.personik.partyplanner.repositorytests

import com.personik.partyplanner.model.User
import com.personik.partyplanner.repository.inmemory.InMemoryUserRepositoryImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class InMemoryUserRepositoryImplTest {
    private lateinit var userRepository: InMemoryUserRepositoryImpl

    @BeforeEach
    fun setUp() {
        userRepository = InMemoryUserRepositoryImpl()
    }

    @Test
    fun `createUser should add a user to the repository`() {
        val user = User(id = 1, hotelRoom = 101)
        userRepository.createUser(user.id, user)
        val retrievedUser = userRepository.getUserById(user.id)
        assertNotNull(retrievedUser)
        assertEquals(user, retrievedUser)
    }

    @Test
    fun `getUserById should return the correct user`() {
        val user = User(id = 1, hotelRoom = 101)
        userRepository.createUser(user.id, user)
        val retrievedUser = userRepository.getUserById(1)
        assertNotNull(retrievedUser)
        assertEquals(user, retrievedUser)
    }

    @Test
    fun `getUserById should return null if user does not exist`() {
        val user = userRepository.getUserById(999)
        assertNull(user)
    }

    @Test
    fun `isUserExistById should return true if user exists`() {
        val user = User(id = 1, hotelRoom = 101)
        userRepository.createUser(user.id, user)
        assertTrue(userRepository.isUserExistById(user.id))
    }

    @Test
    fun `isUserExistById should return false if user does not exist`() {
        assertFalse(userRepository.isUserExistById(999))
    }

    @Test
    fun `getAllUsers should return all users`() {
        val user1 = User(id = 1, hotelRoom = 101)
        val user2 = User(id = 2, hotelRoom = 102)
        userRepository.createUser(user1.id, user1)
        userRepository.createUser(user2.id, user2)
        val users = userRepository.getAllUsers()
        assertEquals(2, users.size)
        assertTrue(users.contains(user1))
        assertTrue(users.contains(user2))
    }

    @Test
    fun `getAllRooms should return all rooms`() {
        val user1 = User(id = 1, hotelRoom = 101)
        val user2 = User(id = 2, hotelRoom = 102)
        userRepository.createUser(user1.id, user1)
        userRepository.createUser(user2.id, user2)
        val rooms = userRepository.getAllRooms()
        assertEquals(2, rooms.size)
        assertTrue(rooms.contains(user1.hotelRoom))
        assertTrue(rooms.contains(user2.hotelRoom))
    }

    @Test
    fun `savePendingPartyId should save the party ID for the user`() {
        userRepository.savePendingPartyId(1, 1001)
        assertEquals(1001, userRepository.getPendingPartyId(1))
    }

    @Test
    fun `getPendingPartyId should return the correct party ID`() {
        userRepository.savePendingPartyId(1, 1001)
        assertEquals(1001, userRepository.getPendingPartyId(1))
    }

    @Test
    fun `getPendingPartyId should return null if no party ID is saved`() {
        assertNull(userRepository.getPendingPartyId(999))
    }

    @Test
    fun `clearPendingPartyId should set the party ID to 0`() {
        userRepository.savePendingPartyId(1, 1001)
        userRepository.clearPendingPartyId(1)
        assertEquals(0, userRepository.getPendingPartyId(1))
    }
}