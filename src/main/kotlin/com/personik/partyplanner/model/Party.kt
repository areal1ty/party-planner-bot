package com.personik.partyplanner.model

data class Party(val partyId: Int,
                 var hotelRoom: Int,
                 val name: String,
                 val ownerId: Long
)
