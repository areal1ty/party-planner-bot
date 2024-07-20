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
import java.util.concurrent.ConcurrentHashMap

enum class UserState {
    NONE,
    AWAITING_PARTY_NAME,
    AWAITING_HOTEL_ROOM
}

@Component
class PartyPlannerBot(
    private val userService: UserServiceImpl,
    private val partyService: PartyServiceImpl,
    @Value("\${bot.token}") private val botToken: String
) {
    private val userStates = ConcurrentHashMap<Long, UserState>()
    private val processedUpdateIds = ConcurrentHashMap.newKeySet<Long>()

    private val bot: Bot = bot {
        token = botToken
        dispatch {
            command("start") {
                val updateId = update.updateId
                if (processedUpdateIds.contains(updateId)) return@command
                processedUpdateIds.add(updateId)

                val chatId = message.chat.id
                val user = userService.createUser(chatId)
                bot.sendMessage(ChatId.fromId(chatId), "Поздравляем с заселением! Ваш номер - ${user.hotelRoom}")
                showAvailablePartiesButton(chatId)
            }

            callbackQuery("show_parties") {
                val updateId = update.updateId
                if (processedUpdateIds.contains(updateId)) return@callbackQuery
                processedUpdateIds.add(updateId)

                val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
                showAvailableParties(chatId)
            }

            callbackQuery("create_party") {
                val updateId = update.updateId
                if (processedUpdateIds.contains(updateId)) return@callbackQuery
                processedUpdateIds.add(updateId)

                val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
                bot.sendMessage(ChatId.fromId(chatId), "Введите название вечеринки:")
                userStates[chatId] = UserState.AWAITING_PARTY_NAME
            }

            callbackQuery {
                val updateId = update.updateId
                if (processedUpdateIds.contains(updateId)) return@callbackQuery
                processedUpdateIds.add(updateId)

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
                val updateId = update.updateId
                if (processedUpdateIds.contains(updateId)) return@text
                processedUpdateIds.add(updateId)

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
        showAvailablePartiesButton(chatId)
    }

    private fun CallbackQueryHandlerEnvironment.handleJoinCommand() {
        val chatId = callbackQuery.message?.chat?.id ?: return
        val partyId = callbackQuery.data.substringAfter("join_").toIntOrNull() ?: return

        bot.sendMessage(ChatId.fromId(chatId), "Введите ваш номер в отеле:")
        userService.savePendingPartyId(chatId, partyId)
        userStates[chatId] = UserState.AWAITING_HOTEL_ROOM
    }

    private fun handleTextMessage(message: Message) {
        val chatId = message.chat.id
        val state = userStates[chatId]

        when (state) {
            UserState.AWAITING_PARTY_NAME -> {
                val partyName = message.text ?: return
                val party = partyService.createParty(chatId, partyName)
                bot.sendMessage(ChatId.fromId(chatId), "Вечеринка '${party.name}' успешно создана!")
                userService.clearPendingPartyId(chatId)
                userStates[chatId] = UserState.NONE
                showAvailablePartiesButton(chatId)
            }
            UserState.AWAITING_HOTEL_ROOM -> {
                val pendingPartyId = userService.getPendingPartyId(chatId) ?: return
                val hotelRoom = message.text?.toIntOrNull()
                if (hotelRoom != null) {
                    partyService.join(pendingPartyId, chatId)
                    bot.sendMessage(ChatId.fromId(chatId), "Вы успешно присоединились к вечеринке!")
                    userStates[chatId] = UserState.NONE
                    showAvailablePartiesButton(chatId)
                } else {
                    bot.sendMessage(ChatId.fromId(chatId), "Пожалуйста, введите корректный номер.")
                }
            }
            else -> bot.sendMessage(ChatId.fromId(chatId), "Неизвестная команда. Пожалуйста, используйте команды из меню.")
        }
    }

    private fun showAvailablePartiesButton(chatId: Long) {
        val inlineKeyboard = InlineKeyboardMarkup.createSingleButton(
            InlineKeyboardButton.CallbackData(text = "Показать доступные вечеринки", callbackData = "show_parties")
        )
        bot.sendMessage(ChatId.fromId(chatId), "Нажмите кнопку, чтобы увидеть доступные вечеринки.", replyMarkup = inlineKeyboard)
    }

    private fun showAvailableParties(chatId: Long) {
        val parties = partyService.getAllParties()
        val inlineKeyboard = InlineKeyboardMarkup.create(
            listOf(
                listOf(InlineKeyboardButton.CallbackData(text = "Создать вечеринку", callbackData = "create_party"))
            ) + parties.map {
                listOf(InlineKeyboardButton.CallbackData(text = it.name, callbackData = it.partyId.toString()))
            }
        )
        bot.sendMessage(ChatId.fromId(chatId), "Список доступных вечеринок:", replyMarkup = inlineKeyboard)
    }
}
