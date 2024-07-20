package com.personik.partyplanner.api

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.handlers.CallbackQueryHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.personik.partyplanner.service.impl.PartyServiceImpl
import com.personik.partyplanner.service.impl.UserServiceImpl
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PartyPlannerBot(
    private val userService: UserServiceImpl,
    private val partyService: PartyServiceImpl,
    @Value("\${bot.token}") private val botToken: String
) {

    private val bot: Bot = bot {
        token = botToken
        dispatch {
            command("start") {
                val chatId = message.chat.id
                val user = userService.createUser(chatId)
                bot.sendMessage(ChatId.fromId(chatId), "Поздравляем с заселением! Ваш номер - ${user.id}")
                showAvailableParties(chatId)
            }

            command("create") {
                val chatId = message.chat.id
                bot.sendMessage(ChatId.fromId(chatId), "Введите название вечеринки:")
                userService.savePendingPartyId(chatId, -1)
            }

            callbackQuery {
                val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
                val partyId = callbackQuery.data.toIntOrNull() ?: return@callbackQuery
                val party = partyService.getParty(partyId)

                val isOwner = party!!.ownerId == chatId
                val buttonLabel = if (isOwner) "Завершить" else "Присоединиться"
                val buttonCallbackData = if (isOwner) "stop_$partyId" else "join_$partyId"

                val inlineKeyboard = InlineKeyboardMarkup.createSingleButton(
                    InlineKeyboardButton.CallbackData(text = buttonLabel, callbackData = buttonCallbackData)
                )

                bot.sendMessage(
                    ChatId.fromId(chatId),
                    "Количество приглашенных гостей: ${party.invitedGuests.size}",
                    replyMarkup = inlineKeyboard
                )
            }

            callbackQuery("stop_") {
                handleStopCommand()
            }

            callbackQuery("join_") {
                handleJoinCommand()
            }

            text {
                handleTextMessage(message)
            }
        }
    }

    init {
        bot.startPolling()
    }

    private fun CallbackQueryHandlerEnvironment.handleStopCommand() {
        val chatId = callbackQuery.message?.chat?.id ?: return
        val partyId = callbackQuery.data.substringAfter("stop_").toIntOrNull() ?: return
        val result = partyService.stopGuestInviting(partyId)
        val party = partyService.getParty(partyId)

        party!!.invitedGuests.forEach {
            bot.sendMessage(
                ChatId.fromId(it.id),
                "Приглашение гостей на вечеринку завершено. Она будет проходить в номере $result"
            )
        }
        showAvailableParties(chatId)
    }

    private fun CallbackQueryHandlerEnvironment.handleJoinCommand() {
        val chatId = callbackQuery.message?.chat?.id ?: return
        val partyId = callbackQuery.data.substringAfter("join_").toIntOrNull() ?: return

        bot.sendMessage(ChatId.fromId(chatId), "Введите ваш номер в отеле:")
        userService.savePendingPartyId(chatId, partyId)
    }

    private fun handleTextMessage(message: Message) {
        val chatId = message.chat.id
        var pendingPartyId = userService.getPendingPartyId(chatId)
        if (pendingPartyId == -1) {
            val partyName = message.text ?: return
            val party = partyService.createParty(chatId, partyName)
            bot.sendMessage(ChatId.fromId(chatId), "Вечеринка '${party.name}' успешно создана!")
            userService.clearPendingPartyId(chatId)
            showAvailableParties(chatId)
        } else {
            pendingPartyId = userService.getPendingPartyId(chatId) ?: return
            val hotelRoom = message.text?.toIntOrNull()
            if (hotelRoom != null) {
                partyService.join(pendingPartyId, chatId)
                showAvailableParties(chatId)
            } else {
                bot.sendMessage(ChatId.fromId(chatId), "Пожалуйста, введите корректный номер.")
            }
        }
    }

    private fun showAvailableParties(chatId: Long) {
        val parties = partyService.getAllParties()
        val inlineKeyboard = InlineKeyboardMarkup.create(
            parties.map {
                listOf(InlineKeyboardButton.CallbackData(text = it.name, callbackData = partyService.getAllPartiesIds().toString()))
            }
        )
        bot.sendMessage(ChatId.fromId(chatId), "Список доступных вечеринок:", replyMarkup = inlineKeyboard)
    }
}