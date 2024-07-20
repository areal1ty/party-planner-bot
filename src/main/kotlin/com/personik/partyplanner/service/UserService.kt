package com.personik.partyplanner.service

import com.personik.partyplanner.model.User

interface UserService {
        fun createUser(chatId: Long): User
        fun isUserExist(chatId: Long): Boolean
        fun getUserById(chatId: Long): User?
        fun savePendingPartyId(userId: Long, partyId: Int)
        fun getPendingPartyId(userId: Long): Int?
        fun clearPendingPartyId(userId: Long)
}