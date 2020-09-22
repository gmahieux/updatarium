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
package io.saagie.updatarium.engine.bash

import java.io.File
import java.lang.ProcessBuilder.Redirect
import java.time.Duration
import java.util.concurrent.TimeUnit.MILLISECONDS
import mu.KLogger

class BashEngine(private val logger: KLogger) {
    fun runCommand(
        script: String,
        workingDir: String = ".",
        timeout: Duration = Duration.ofMinutes(1)
    ) {

        val proc = ProcessBuilder(listOf("bash", "-c", script))
            .directory(File(workingDir))
            .redirectOutput(Redirect.PIPE)
            .redirectError(Redirect.PIPE)
            .redirectErrorStream(true)
            .start().apply { waitFor(timeout.toMillis(), MILLISECONDS) }

        val out = proc.inputStream.bufferedReader().readText()
        when {
            proc.exitValue() == 0 -> logger.info { out.dropLast(1) }
            else -> throw Exception("Command '$script' execution error $out")
        }
    }
}
