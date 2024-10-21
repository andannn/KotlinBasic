package coroutines.chapter_3_3

import org.junit.jupiter.api.Test
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CoroutineTest {

    @Test
    fun coroutine_constructor_test() {
        val continuation = suspend {
            "AAA"
        }.createCoroutine(object : Continuation<String> {
            override val context: CoroutineContext
                get() = EmptyCoroutineContext

            override fun resumeWith(result: Result<String>) {
                println("result $result")
            }
        })

        continuation.resume(Unit)
    }
}

suspend fun noSuspend() = suspendCoroutine<Int> { continuation ->
    continuation.resume(10)
}
