package com.personik.partyplanner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PartyPlannerApplication

fun main(args: Array<String>) {
    runApplication<PartyPlannerApplication>(*args)
}
