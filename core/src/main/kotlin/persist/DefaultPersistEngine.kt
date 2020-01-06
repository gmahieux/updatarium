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
package persist

import dsl.ChangeSet
import dsl.Status
import persist.PersistEngine

/**
 * This is a basic implementation of the PersistEngine.
 * It will store nothing and just let you execute the changeset.
 *
 * :warning: please do not use this in production if you don't want to replay all changesets.
 */
class DefaultPersistEngine : PersistEngine() {
    override fun checkConnection() {
        logger.warn { "***********************" }
        logger.warn { "*NO PERSIST ENGINE !!!*" }
        logger.warn { "***********************" }
    }

    override fun notAlreadyExecuted(changeSetId: String): Boolean = true
    override fun lock(changeSet: ChangeSet) {
        logger.info { "${changeSet.id} marked as ${Status.EXECUTE}" }
    }

    override fun unlock(changeSet: ChangeSet, status: Status) {
        logger.info { "${changeSet.id} marked as ${status}" }
    }
}
