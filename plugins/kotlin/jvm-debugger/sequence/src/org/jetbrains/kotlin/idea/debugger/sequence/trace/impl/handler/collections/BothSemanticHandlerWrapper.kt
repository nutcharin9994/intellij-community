// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.idea.debugger.sequence.trace.impl.handler.collections

import com.intellij.debugger.streams.core.trace.IntermediateCallHandler
import com.intellij.debugger.streams.core.trace.TerminatorCallHandler
import com.intellij.debugger.streams.core.trace.dsl.Dsl
import com.intellij.debugger.streams.core.wrapper.IntermediateStreamCall
import com.intellij.debugger.streams.core.wrapper.TerminatorStreamCall

/**
 * Unlike java streams, most operations in kotlin collections are intermediate and terminal simultaneously.
 * To avoid using of the same code in two places we will implement common logic in the {@link BothSemanticsHandler}.
 */
class BothSemanticHandlerWrapper(private val handler: BothSemanticsHandler) {
    fun createIntermediateHandler(order: Int, call: IntermediateStreamCall, dsl: Dsl): IntermediateCallHandler =
        CollectionIntermediateHandler(order, call, dsl, handler)

    fun createTerminatorHandler(call: TerminatorStreamCall, resultExpression: String, dsl: Dsl): TerminatorCallHandler =
        CollectionTerminatorHandler(call, resultExpression, dsl, handler)
}
