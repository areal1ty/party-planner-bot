package com.personik.partyplanner.config

import com.personik.partyplanner.api.PartyPlannerBot
import com.personik.partyplanner.service.impl.PartyServiceImpl
import com.personik.partyplanner.service.impl.UserServiceImpl
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TelegramBotConfig(private val userService: UserServiceImpl,
                        private val partyService: PartyServiceImpl,
                        @Value("\${bot.token}") private val botToken: String
) {

    @Bean
    fun telegramBot(): PartyPlannerBot {
        return PartyPlannerBot(userService, partyService, botToken)
    }

}