package com.personik.partyplanner.repository.inmemory

import com.personik.partyplanner.model.Party
import com.personik.partyplanner.model.User
import com.personik.partyplanner.repository.PartyRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryPartyRepositoryImpl: PartyRepository {
    private var parties = ConcurrentHashMap<Int, Party>()
    private val invitedGuests: ConcurrentHashMap<Int, MutableList<User>> = ConcurrentHashMap()

    override fun createParty(partyId: Int, party: Party): Party {
        parties[partyId] = party
        return party
    }

    override fun getParty(partyId: Int): Party {
        return parties[partyId] ?: throw IllegalArgumentException("Party with ID $partyId not found")
    }

    override fun getAllParties(): List<Party> {
        return parties.values.toList()
    }

    override fun getAllPartiesIds(): List<Int> {
        return parties.keys().toList()
    }

    override fun deleteParty(partyId: Int) {
        parties.remove(partyId)
    }

    override fun addGuestToParty(partyId: Int, user: User) {
        invitedGuests.compute(partyId) { _, userList ->
            val list = userList ?: mutableListOf()
            list.add(user)
            list
        }
    }

    override fun getGuestsOfParty(partyId: Int): List<User> {
        return invitedGuests.getOrPut(partyId) { mutableListOf() }
    }

}