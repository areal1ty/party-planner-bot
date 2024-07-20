package com.personik.partyplanner.service

import com.personik.partyplanner.model.Party

interface PartyService {
    fun createParty(ownerId: Long, partyName: String): Party
    fun getAllParties(): List<Party>
    fun getAllPartiesIds(): List<Int>
    fun getParty(partyId: Int): Party?
    fun stopGuestInviting(partyId: Int): Int
    fun join(partyId: Int, userId: Long)

}