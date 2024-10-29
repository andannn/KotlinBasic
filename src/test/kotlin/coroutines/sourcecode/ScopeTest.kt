package coroutines.sourcecode

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
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
}