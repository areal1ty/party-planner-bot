package com.personik.partyplanner.repository

import com.personik.partyplanner.model.Party

interface PartyRepository {
    fun createParty(partyId: Int, party: Party)
    fun getParty(partyId: Int): Party?
    fun getAllParties(): List<Party>
    fun getAllPartiesIds(): List<Int>
    fun deleteParty(partyId: Int)

}