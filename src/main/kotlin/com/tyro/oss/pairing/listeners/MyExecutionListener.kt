/**
 * Copyright 2021 Tyro Payments Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tyro.oss.pairing.listeners

import com.intellij.execution.ExecutionListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.diagnostic.Logger
import com.tyro.oss.pairing.handler.ExecutionOperationEvent

class MyExecutionListener(
    private val runOperationCallBack: (ExecutionOperationEvent) -> Unit
) : ExecutionListener {
    companion object {
        private val LOG = Logger.getInstance(MyExecutionListener::class.java)
    }

    override fun processStartScheduled(executorId: String, env: ExecutionEnvironment) {
        val runnerAndConfigurationSettings = env.runnerAndConfigurationSettings
        runOperationCallBack(
            ExecutionOperationEvent(
            runnerAndConfigurationSettings!!.configuration.project.name,
            runnerAndConfigurationSettings.configuration.name)
        )
    }
}
