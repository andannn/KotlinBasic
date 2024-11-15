package coroutines.sourcecode

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ScopeTest {

    private val testScope = TestScope()

    @Test
    fun scope_cancel_test() = testScope.runTest {
        val scope = CoroutineScope(Dispatchers.Default)

        var canceledCount = 0
        scope.launch {
            try {
                delay(1000)
            } catch (e: CancellationException) {
                canceledCount++
            }
        }
        scope.launch {
            try {
                delay(1000)
            } catch (e: CancellationException) {
                canceledCount++
            }
        }

        println(scope.coroutineContext)
//        scope.coroutineContext[Job]!!.cancel()
        scope.cancel()
        scope.coroutineContext[Job]!!.join()
        assertEquals(2, canceledCount)
    }

    @Test
    fun scope_cancel_test2() = testScope.runTest {


        val scope = CoroutineScope(Dispatchers.Default)

        var canceledCount = 0
        scope.launch {
            try {
                launch_sub_coroutine()
            } catch (e: CancellationException) {
                canceledCount++
            }
        }

        delay(1000)
        scope.coroutineContext.cancel()
//        scope.cancel()
        scope.coroutineContext[Job]!!.join()
        assertEquals(1, canceledCount)
    }

    private suspend fun launch_sub_coroutine() = coroutineScope {
        launch {
            while (true) {
                delay(100)
            }
        }
    }

    @Test
    fun failed_task_test() = testScope.runTest {
        var canceledCount = 0
        val job = launch {
            try {
                /**
                 *  When any child coroutine in this scope fails, this scope fails,
                 *  cancelling all the other children
                 */
                coroutineScope {
                    launch {
                        try {
                            delay(1000)
                        } catch (e: CancellationException) {
                            canceledCount++
                        }
                    }

                    launch {
                        delay(500)
                        throw IllegalStateException("some error")
                    }
                }
            } catch (e: IllegalStateException) {

            }
        }

        job.join()

        assertEquals(canceledCount, 1)
    }

    @Test
    fun exception_handler_test() = testScope.runTest {
        var calledCount = 0

        /**
         * All children coroutines (coroutines created in the context of another Job)
         * delegate handling of their exceptions to their parent coroutine,
         * which also delegates to the parent, and so on until the root,
         * so the CoroutineExceptionHandler installed in their context is never used.
         */
        val job = this.launch(
            CoroutineExceptionHandler { context, t ->
                calledCount++
            }
        ) {
            throw IllegalStateException("some error")
        }
        job.join()

        // failed with exception
    }

    @Test
    fun exception_handler_test_2() = testScope.runTest {
        var calledCount = 0

        /**
         * All children coroutines (coroutines created in the context of another Job)
         * delegate handling of their exceptions to their parent coroutine,
         * which also delegates to the parent, and so on until the root,
         * so the CoroutineExceptionHandler installed in their context is never used.
         */
        val job = CoroutineScope(Job()).launch(
            CoroutineExceptionHandler { context, t ->
                calledCount++
            }
        ) {
            throw IllegalStateException("some error")
        }
        job.join()

        // success
        assertEquals(1, calledCount)
    }

    @Test
    fun timeout_test() = testScope.runTest {
        val job = launch {
            try {
                withTimeout(100) {
                    delay(200)
                }

            } catch (e: TimeoutCancellationException) {
                println("timeout")
            }
        }
        job.join()
    }

    @Test
    fun cancellation_continuation_test() = testScope.runTest {
        suspendCancellableCoroutine { continutation ->
            continutation.invokeOnCancellation {
                println("cancelled")
            }
        }
    }

    @Test
    fun mutable_state_flow_test() = testScope.runTest {
        val stateFlow = MutableStateFlow(0)

        val job = launch {
            stateFlow.collect {
                println(it)

                if (it == 1) {
                    throw IllegalStateException("some error")
                }
            }
        }
        stateFlow.value = 1
        job.join()
    }
}