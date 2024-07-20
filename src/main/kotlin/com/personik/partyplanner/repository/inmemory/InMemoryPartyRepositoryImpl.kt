package com.personik.partyplanner.repository.inmemory

import com.personik.partyplanner.model.Party
import com.personik.partyplanner.repository.PartyRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap


@Repository
class InMemoryPartyRepositoryImpl: PartyRepository {
    private var parties = ConcurrentHashMap<Int, Party>()

    override fun createParty(partyId: Int, party: Party) {
        parties[partyId] = party
    }

    override fun getParty(partyId: Int): Party? {
        return parties[partyId]
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

}