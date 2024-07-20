package com.personik.partyplanner.servicetests

import com.personik.partyplanner.model.Party
import com.personik.partyplanner.model.User
import com.personik.partyplanner.repository.inmemory.InMemoryPartyRepositoryImpl
import com.personik.partyplanner.repository.inmemory.InMemoryUserRepositoryImpl
import com.personik.partyplanner.service.impl.PartyServiceImpl
import com.personik.partyplanner.service.impl.UserServiceImpl
import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class PartyServiceImplTest {
    private lateinit var partyService: PartyServiceImpl
    private lateinit var userService: UserServiceImpl
    private lateinit var partyRepository: InMemoryPartyRepositoryImpl
    private lateinit var userRepository: InMemoryUserRepositoryImpl

    @BeforeEach
    fun setUp() {
        partyService = PartyServiceImpl()
        userService = UserServiceImpl()
    }

    @Test
    fun `createParty should create a new party with unique partyId`() {
        val ownerId = 12345L
        val partyName = "Test Party"

        val party = partyService.createParty(ownerId, partyName)

        assertEquals(ownerId, party.ownerId)
        assertEquals(partyName, party.name)
        assertEquals(1, party.partyId)
        assertEquals(party, partyService.getParty(party.partyId))
        assertEquals(1, partyService.getAllParties().size)
    }

    @Test
    fun `getAllParties should return a list of all parties`() {
        val ownerId = 12345L
        val partyName = "Test Party"
        val party = partyService.createParty(ownerId, partyName)

        val parties = listOf(Party(party.partyId, party.hotelRoom, party.name, party.ownerId))

        every { partyRepository.getAllParties() } returns parties

        val result = partyService.getAllParties()

        assertEquals(parties, result)
    }

    @Test
    fun `getParty should return the correct party by ID`() {
        val ownerId = 12345L
        val partyName = "Test Party"
        val party = partyService.createParty(ownerId, partyName)

        every { partyRepository.getParty(party.partyId) } returns party

        val result = partyService.getParty(party.partyId)

        assertEquals(party, result)
    }

    @Test
    fun `stopGuestInviting should delete party and return optimal hotel room`() {
        val partyId = 1

        val ownerId1 = 1233145L
        val partyName1 = "Test Party1"
        val user1 = userService.createUser(ownerId1)
        val party1 = partyService.createParty(ownerId1, partyName1)

        val ownerId2 = 1231345L
        val partyName2 = "Test Party2"
        val user2 = userService.createUser(ownerId2)
        val party2 = partyService.createParty(ownerId2, partyName2)

        val ownerId3 = 123456L
        val partyName3 = "Test Party3"
        val user3 = userService.createUser(ownerId3)
        val party3 = partyService.createParty(ownerId3, partyName3)

        // assertEquals(listOf(user1.hotelRoom, user2.hotelRoom, user3.hotelRoom), userRepository.getAllRooms())

        val result = partyService.stopGuestInviting(party3.partyId)
        assertEquals(2, result)
        assertEquals(2, partyService.getAllParties())
    }

    @Test
    fun `join should add user to the party`() {
        /*val user = User(id = 1, hotelRoom = 101)
        userRepository.createUser(user.id, user)
        val retrievedUser = userRepository.getUserById(user.id)
        assertNotNull(retrievedUser)
        assertEquals(user, retrievedUser)

         */

        val user = User(id = 1, hotelRoom = 101)
        val user1 = userService.createUser(user.id)
        val retrievedUser = userService.getUserById(user.id)
        assertNotNull(retrievedUser)
        assertEquals(user1, retrievedUser)

        val party = partyService.createParty(user.id, "Party1")
        val userOrdinary = userService.createUser(432123)

        // partyRepository.addGuestToParty(party.partyId, userOrdinary)
        partyService.join(party.partyId, userOrdinary)

        val guests = partyService.getGuestsOfParty(party.partyId).toList()
        assertEquals(guests.size, 1)
        assertTrue(guests.contains(userOrdinary))
    }

    @Test
    fun `join should throw exception if owner tries to join their own party`() {
        val partyId = 1
        val ownerId = 12345L

        val user = userService.createUser(ownerId)
        val party = partyService.createParty(ownerId, "Party1")

        every { partyRepository.getParty(partyId) } returns party

        val exception = assertThrows<IllegalArgumentException> {
            partyService.join(partyId, user)
        }

        assertEquals("Party owner cannot join it", exception.message)
    }
}