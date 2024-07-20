package com.personik.partyplanner.model

data class Party(val partyId: Int,
                 var hotelRoom: Int,
                 val name: String,
                 var invitedGuests: MutableList<User> = mutableListOf(),
                 val ownerId: Long
) {
    fun addGuest(user: User) {
        invitedGuests.add(user)
    }
}
