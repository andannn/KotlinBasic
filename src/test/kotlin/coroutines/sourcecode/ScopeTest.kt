package coroutines.sourcecode

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.suspendCoroutine
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
}