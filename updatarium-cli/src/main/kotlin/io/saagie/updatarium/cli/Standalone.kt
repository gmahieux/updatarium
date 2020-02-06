/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019-2020 Pierre Leresteux.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.saagie.updatarium.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.path
import io.saagie.updatarium.Updatarium
import io.saagie.updatarium.cli.PersitEngine.MONGODB
import io.saagie.updatarium.cli.PersitEngine.NONE
import io.saagie.updatarium.persist.DefaultPersistEngine
import io.saagie.updatarium.persist.MongodbPersistEngine
import io.saagie.updatarium.persist.PersistConfig
import org.slf4j.event.Level
import java.nio.file.Files

enum class PersitEngine {
    NONE,
    MONGODB
}

class Standalone : CliktCommand(printHelpOnEmptyArgs = true) {

    // Execution options
    val changelog by argument(help = "Changelog path (file or directory)", name = "changelog").path(
        exists = true,
        readable = true,
        fileOkay = true
    )
    val tags by option(help = "Tags to execute (OR)", envvar = "UPDATARIUM_TAGS").multiple()
    val changelogsPattern by option(
        help = "Changelogs pattern regex (if --changelog is a directory) : `changelog(.*).kts` by default",
        envvar = "UPDATARIUM_CHANGELOGS_PATTERN"
    ).default("changelog(.*).kts")

    // PersistEngine options
    val persitEngine by option(help = "Choose the PersitEngine", envvar = "UPDATARIUM_PERSIST_ENGINE").choice(
        NONE.name,
        MONGODB.name
        ,ignoreCase = true
    ).default(NONE.name)

    // PersistEngineConfig
    val persistLoggerLevel by option(help = "Logger level (Default : INFO)", envvar = "UPDATARIUM_PERSIST_LOGLEVEL")
        .choice(Level.TRACE.name, Level.DEBUG.name, Level.INFO.name, Level.WARN.name, Level.ERROR.name,ignoreCase = true)
        .default(Level.INFO.name)

    val persistLogOnSuccess by option(
        "--logOnSuccess", help = "Log on success",
        envvar = "UPDATARIUM_PERSIST_LOGSUCCESS"
    ).flag()

    val persistLogOnError by option(
        "--logOnError", help = "Log on Error",
        envvar = "UPDATARIUM_PERSIST_LOGERROR"
    ).flag()

    override fun run() {
        val config = PersistConfig(
            level = org.apache.logging.log4j.Level.getLevel(persistLoggerLevel),
            onErrorStoreLogs = persistLogOnError,
            onSuccessStoreLogs = persistLogOnSuccess,
            layout = {event -> event.message?:""}
        )
        val updatarium = Updatarium(
            persistEngine = when (persitEngine) {
                MONGODB.name -> MongodbPersistEngine(config)
                else -> DefaultPersistEngine(config)
            }
        )

        when {
            Files.isDirectory(changelog) -> updatarium.executeChangelogs(changelog, changelogsPattern, tags)
            else -> updatarium.executeChangelog(changelog, tags)
        }
    }
}

fun main(args: Array<String>) = Standalone().main(args)
