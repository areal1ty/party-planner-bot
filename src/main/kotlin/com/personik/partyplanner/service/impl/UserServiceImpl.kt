package com.personik.partyplanner.service.impl

import com.personik.partyplanner.model.User
import com.personik.partyplanner.repository.inmemory.InMemoryUserRepositoryImpl
import com.personik.partyplanner.service.UserService
import org.springframework.stereotype.Service

@Service
class UserServiceImpl : UserService {
    // private val logger = LoggerFactory.getLogger("UserServiceImpl")
    private val repositoryImpl = InMemoryUserRepositoryImpl()
    private var hotelRoomCounter = 0

    override fun createUser(chatId: Long): User {
        val hotelRoom = generateHotelRoom()
        val newUser = User(chatId, hotelRoom)
        repositoryImpl.createUser(chatId, newUser)
        // logger.info("new user created")
        return newUser
    }

    private fun generateHotelRoom(): Int {
        // logger.info("generating hotelRoom...")
        return ++this.hotelRoomCounter
    }

    override fun isUserExist(chatId: Long): Boolean {
        // logger.info("checking if user exist...")
        return repositoryImpl.isUserExistById(chatId)
    }

    override fun getUserById(chatId: Long): User? {
        return repositoryImpl.getUserById(chatId)
    }

    override fun getAllRooms(): List<Int> {
        return repositoryImpl.getAllRooms()
    }

    override fun savePendingPartyId(userId: Long, partyId: Int) {
        // logger.info("user $userId from now on interact with party $partyId")
        return repositoryImpl.savePendingPartyId(userId, partyId)
    }

    override fun getPendingPartyId(userId: Long): Int? {
        // logger.info("getting the ID of party user $userId interacting with...")
        return repositoryImpl.getPendingPartyId(userId)
    }

    override fun clearPendingPartyId(userId: Long) {
        // logger.info("...")
        return repositoryImpl.clearPendingPartyId(userId)
    }
}