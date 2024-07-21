package com.personik.partyplanner.repository

import com.personik.partyplanner.model.User

interface UserRepository {
    fun createUser(id: Long, user: User)
    fun getUserById(id: Long): User?
    fun getAllUsers(): List<User>
    fun isUserExistById(id: Long): Boolean
    fun savePendingPartyId(userId: Long, partyId: Int)
    fun getPendingPartyId(userId: Long): Int?
    fun clearPendingPartyId(userId: Long)
}