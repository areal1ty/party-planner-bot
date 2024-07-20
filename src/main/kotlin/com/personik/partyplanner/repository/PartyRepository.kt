package com.personik.partyplanner.repository

import com.personik.partyplanner.model.Party
import com.personik.partyplanner.model.User

interface PartyRepository {
    fun createParty(partyId: Int, party: Party): Party
    fun getParty(partyId: Int): Party?
    fun getAllParties(): List<Party>
    fun getAllPartiesIds(): List<Int>
    fun deleteParty(partyId: Int)
    fun addGuestToParty(partyId: Int, user: User)
    fun getGuestsOfParty(partyId: Int): List<User>?
}