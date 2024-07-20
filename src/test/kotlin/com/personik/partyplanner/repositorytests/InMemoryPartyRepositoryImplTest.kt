package com.personik.partyplanner.repositorytests

import com.personik.partyplanner.model.Party
import com.personik.partyplanner.repository.inmemory.InMemoryPartyRepositoryImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class InMemoryPartyRepositoryImplTest {
    private lateinit var partyRepository: InMemoryPartyRepositoryImpl

    @BeforeEach
    fun setUp() {
        partyRepository = InMemoryPartyRepositoryImpl()
    }

    @Test
    fun `createParty should add a party to the repository and return correct party`() {
        val partyId = 12
        val newParty = Party(partyId, 0, "TestName", mutableListOf(), 145)
        partyRepository.createParty(partyId, newParty)
        val retrievedParty = partyRepository.getParty(partyId)
        assertNotNull(retrievedParty)
        assertEquals(newParty, retrievedParty)
    }

    @Test
    fun `getParty should return null if party does not exist`() {
        val party = partyRepository.getParty(999)
        assertNull(party)
    }

    @Test
    fun `getAllParties should return all parties`() {
        val partyId1 = 213
        val partyId2 = 123
        val newParty1 = Party(partyId1, 0, "TestName", mutableListOf(), 145)
        val newParty2 = Party(partyId2, 0, "TestName", mutableListOf(), 145)

        partyRepository.createParty(newParty1.partyId, newParty1)
        partyRepository.createParty(newParty2.partyId, newParty2)
        val parties = partyRepository.getAllParties()
        assertEquals(2, parties.size)
        assertTrue(parties.contains(newParty1))
        assertTrue(parties.contains(newParty2))
    }

    @Test
    fun `getAllPartiesIds should return all party IDs`() {
        val newParty1 = Party(113, 0, "TestName", mutableListOf(), 145)
        val newParty2 = Party(11213, 0, "TestName", mutableListOf(), 145)

        partyRepository.createParty(newParty1.partyId, newParty1)
        partyRepository.createParty(newParty2.partyId, newParty2)
        val partyIds = partyRepository.getAllPartiesIds()
        assertEquals(2, partyIds.size)
        assertTrue(partyIds.contains(newParty1.partyId))
        assertTrue(partyIds.contains(newParty2.partyId))
    }

    @Test
    fun `deleteParty should remove the party from the repository`() {
        val newParty1 = Party(113, 0, "TestName", mutableListOf(), 145)
        partyRepository.createParty(newParty1.partyId, newParty1)
        partyRepository.deleteParty(newParty1.partyId)
        val retrievedParty = partyRepository.getParty(newParty1.partyId)
        assertNull(retrievedParty)
    }
}