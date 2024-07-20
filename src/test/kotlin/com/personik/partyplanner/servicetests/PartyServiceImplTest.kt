package com.personik.partyplanner.servicetests

import com.personik.partyplanner.model.Party
import com.personik.partyplanner.model.User
import com.personik.partyplanner.repository.inmemory.InMemoryPartyRepositoryImpl
import com.personik.partyplanner.repository.inmemory.InMemoryUserRepositoryImpl
import com.personik.partyplanner.service.impl.PartyServiceImpl
import com.personik.partyplanner.service.impl.UserServiceImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
        partyRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        partyService = PartyServiceImpl()
        userService = UserServiceImpl()
    }

    @Test
    fun `createParty should create a new party with unique partyId`() {
        val ownerId = 12345L
        val partyName = "Test Party"

        every { partyRepository.createParty(any(), any()) } returns Unit

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

        val parties = listOf(Party(party.partyId, party.hotelRoom, party.name, party.invitedGuests, party.ownerId))

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

        assertEquals(listOf(user1.hotelRoom, user2.hotelRoom, user3.hotelRoom), userRepository.getAllRooms())


        every { partyRepository.deleteParty(partyId) } returns Unit
        every { userRepository.getAllRooms() } returns listOf(101, 102, 103)

        val result = partyService.stopGuestInviting(party3.partyId)
        assertEquals(2, result)
        assertEquals(2, partyService.getAllParties())
    }

    @Test
    fun `join should add user to the party`() {
        val userId = 12345L
        val party = partyService.createParty(userId, "Party1")
        val user = User(userId, 101)

        every { partyRepository.getParty(party.partyId) } returns party
        every { userRepository.getUserById(userId) } returns user

        partyService.join(party.partyId, userId)

        assertTrue(party.invitedGuests.contains(user))
    }

    @Test
    fun `join should throw exception if owner tries to join their own party`() {
        val partyId = 1
        val ownerId = 12345L
        val party = Party(partyId, 0, "Party1", mutableListOf(), ownerId)

        every { partyRepository.getParty(partyId) } returns party

        val exception = assertThrows<IllegalArgumentException> {
            partyService.join(partyId, ownerId)
        }

        assertEquals("Party owner cannot join it", exception.message)
    }
}