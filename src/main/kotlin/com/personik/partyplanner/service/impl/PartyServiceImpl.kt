package com.personik.partyplanner.service.impl

import com.personik.partyplanner.model.Party
import com.personik.partyplanner.model.User
import com.personik.partyplanner.repository.inmemory.InMemoryPartyRepositoryImpl
import com.personik.partyplanner.service.PartyService
import org.springframework.stereotype.Service

@Service
class PartyServiceImpl : PartyService {
    private val repositoryImpl = InMemoryPartyRepositoryImpl()
    private var partyIdCounter = 0

    override fun createParty(ownerId: Long, partyName: String): Party {
        val partyId = generatePartyId()
        val newParty = Party(partyId, 0, partyName, ownerId)
        return repositoryImpl.createParty(partyId, newParty)
//        logger.info("new party created")
    }

    private fun generatePartyId(): Int {
        //      logger.info("generating partyId...")
        return ++this.partyIdCounter
    }

    override fun getAllParties(): List<Party> {
        //    logger.info("getting the list of all parties...")
        return repositoryImpl.getAllParties()
    }

    override fun getAllPartiesIds(): List<Int> {
        return repositoryImpl.getAllPartiesIds()
    }

    override fun getParty(partyId: Int): Party {
        //  logger.info("getting the party with ID $partyId")
        return repositoryImpl.getParty(partyId)
    }

    override fun stopGuestInviting(partyId: Int, rooms: List<Int>): Int {
        //logger.info("stopping guest inviting...")
        repositoryImpl.deleteParty(partyId)
        //logger.info("party was successfully deleted")
        return determineOptimalHotelRoom(rooms)
    }

    override fun join(partyId: Int, user: User) {
        val ownerId = repositoryImpl.getParty(partyId).ownerId
        if (ownerId != user.id) {
            repositoryImpl.addGuestToParty(partyId, user)
        } else {
            //logger.info("user trying to join is owner. Throwing exception")
            throw IllegalArgumentException("Party owner cannot join it")
        }
    }

    override fun getGuestsOfParty(partyId: Int): List<User> {
        return repositoryImpl.getGuestsOfParty(partyId)
    }

    private fun determineOptimalHotelRoom(rooms: List<Int>): Int {
        if (rooms.isEmpty()) return 0
        val roomsCopy = rooms.toMutableList()
        val medianIndex = roomsCopy.size / 2
        // logger.info("determining optimal hotel room...")
        return quickSelect(roomsCopy, medianIndex)
    }

    private fun quickSelect(list: MutableList<Int>, k: Int): Int {
        if (list.size == 1) return list[0]

        val pivot = list[list.size / 2]
        val lows = list.filter { it < pivot }.toMutableList()
        val highs = list.filter { it > pivot }.toMutableList()
        val pivots = list.filter { it == pivot }.toMutableList()

        return when {
            k < lows.size -> quickSelect(lows, k)
            k < lows.size + pivots.size -> pivots[0]
            else -> quickSelect(highs, k - lows.size - pivots.size)
        }
    }
}
