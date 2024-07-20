package com.personik.partyplanner.repository.inmemory

import com.personik.partyplanner.model.User
import com.personik.partyplanner.repository.UserRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryUserRepositoryImpl: UserRepository {
    private var userChatIdStorage = ConcurrentHashMap<Long, User>()
    private var userHotelRoomStorage = ConcurrentHashMap<Int, User>()
    private val pendingPartyIdStorage = ConcurrentHashMap<Long, Int>()

    override fun createUser(id: Long, user: User) {
        userChatIdStorage[id] = user
        userHotelRoomStorage[user.hotelRoom] = user
    }

    override fun getUserById(id: Long): User? {
        return userChatIdStorage[id]
    }

    override fun isUserExistById(id: Long): Boolean {
        return userChatIdStorage.containsKey(id)
    }

    override fun getAllUsers(): List<User> {
        return userChatIdStorage.values.toList()
    }

    fun getAllRooms(): List<Int> {
        return userHotelRoomStorage.keys.toList()
    }

    override fun savePendingPartyId(userId: Long, partyId: Int) {
        pendingPartyIdStorage[userId] = partyId
    }

    override fun getPendingPartyId(userId: Long): Int? {
        return pendingPartyIdStorage[userId]
    }

    override fun clearPendingPartyId(userId: Long) {
        pendingPartyIdStorage[userId] = 0
    }
}