package flow

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.coroutineContext
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FlowTest {

    private val testScope = TestScope()

    /**
     * From the implementation point of view, it means that all flow implementations should only emit from the same coroutine.
     * This constraint is efficiently enforced by the default flow builder.
     * The flow builder should be used if the flow implementation does not start any coroutines.
     */
    @Test
    fun emit_from_different_coroutine_test() = testScope.runTest {
        val myFlow = flow {
            launch(CoroutineName("emitter coroutine")) {
                emit(1)
            }
        }

        launch(CoroutineName("collector coroutine")) {
            myFlow.collect {
                println(it)
            }
        }
    }

    @Test
    fun change_context_of_flow_test() = testScope.runTest {
        val myFlow = flow {
            suspendCoroutine {
                println("flow on coroutine: ${it.context[CoroutineName]}")
                it.resume(1L)
            }
            emit(1)
        }.flowOn(CoroutineName("flow on coroutine"))

        launch(CoroutineName("collector coroutine")) {
            myFlow.collect {
                println(it)
            }
        }
    }

    @Test
    fun catch_exception_test() = testScope.runTest {
        val myFlow = flow {
            emit(1)
            throw IllegalStateException("some error")
        }

        myFlow
            .catch {
                println(it.message)
                emit(0)
            }.collect {
                println(it)
            }
    }

    @Test
    fun buffer_test() = testScope.runTest {
        flowOf("A", "B", "C")
            .onEach { println("1$it") }
            .buffer(capacity = RENDEZVOUS)
            .collect { println("2$it") }
    }

    @Test
    fun channel_flow_test() = testScope.runTest {
        channelFlow {
            launch {
                send(1)
            }
            send(2)
            send(3)
        }.collect {
            println(it)
        }
    }

    @Test
    fun distinct_test() = testScope.runTest {
        /**
         * when first value or different value is emitted, new value is emit to flow.
         */
        flowOf(1)
            .distinctUntilChanged()
            .collect {
                println(it)
            }

        flowOf(2, 2, 3, 4)
            .distinctUntilChanged()
            .collect {
                println(it)
            }
    }
}