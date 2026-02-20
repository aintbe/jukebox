package com.jukebox.relay

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class RelayApplication

fun main(args: Array<String>) {
    runApplication<RelayApplication>(*args)
}
