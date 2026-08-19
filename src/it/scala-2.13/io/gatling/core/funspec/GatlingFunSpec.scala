/*
 * Copyright 2011-2024 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// ponytail: Gatling 3.13 removed `io.gatling.core.funspec.GatlingFunSpec` (it existed up to 3.12).
// Our IT specs rely on it for the spec/before/after pattern. The underlying Simulation hooks
// (before/after/setUp/private[gatling] params) are unchanged in 3.13, so we re-vendor the class here.
package io.gatling.core.funspec

import scala.collection.mutable.ListBuffer

import io.gatling.core.Predef._
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.Protocol
import io.gatling.core.structure.ChainBuilder

abstract class GatlingFunSpec extends Simulation {

  def protocolConf: Protocol

  def spec(actionBuilder: ActionBuilder): ListBuffer[ActionBuilder] = specs += actionBuilder

  private[this] val specs = new ListBuffer[ActionBuilder]

  private[this] lazy val testScenario = scenario(this.getClass.getSimpleName)
    .exec(new ChainBuilder(specs.reverse.toList))

  private def setupRegisteredSpecs() = {
    require(specs.nonEmpty, "At least one spec needs to be defined")
    setUp(testScenario.inject(atOnceUsers(1)))
      .protocols(protocolConf)
      .assertions(forAll.failedRequests.percent.is(0))
  }

  override private[gatling] def params(configuration: GatlingConfiguration) = {
    setupRegisteredSpecs()
    super.params(configuration)
  }
}
