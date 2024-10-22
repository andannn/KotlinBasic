package coroutines.chapter_4_1

import org.junit.jupiter.api.Test
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface Generator<T> {
    operator fun iterator(): Iterator<T>
}

sealed class State {
    class NotReady(val continuation: Continuation<Unit>) : State()
    class Ready<T>(val continuation: Continuation<Unit>, val nextValue: T) : State()

    data object Done : State()
}

class GeneratorIterator<T>(
    private val block: suspend GeneratorScope<T>.(T) -> Unit,
    private val paramter: T,
) : GeneratorScope<T>, Iterator<T>, Continuation<Any?>, Generator<T> {
    private var state: State

    init {
        val coroutineBlock: suspend GeneratorScope<T>.() -> Unit = { block(paramter) }
        val start = coroutineBlock.createCoroutine(this, this)
        state = State.NotReady(start)
    }

    override fun hasNext(): Boolean {
        resume()
        return state != State.Done
    }

    private fun resume() {
        when (val currentState = state) {
            is State.NotReady -> currentState.continuation.resume(Unit)
            else -> Unit
        }
    }

    override fun next(): T {
        return when (val currentState = state) {
            is State.NotReady -> {
                resume()
                return next()
            }

            is State.Ready<*> -> {
                state = State.NotReady(currentState.continuation)
                (currentState as State.Ready<T>).nextValue
            }

            State.Done -> error("3")
        }
    }

    override val context: CoroutineContext get() = EmptyCoroutineContext

    override fun resumeWith(result: Result<Any?>) {
        state = State.Done
        result.getOrThrow()
    }

    override suspend fun yield(value: T) = suspendCoroutine { cont ->
        state = when (state) {
            is State.NotReady -> {
                State.Ready(continuation = cont, nextValue = value)
            }

            State.Done -> error("0")
            is State.Ready<*> -> error("0")
        }
    }

    override fun iterator(): Iterator<T> = this
}

interface GeneratorScope<T> {
    suspend fun yield(value: T)
}

fun <T> generator(block: suspend GeneratorScope<T>.(T) -> Unit): (T) -> Generator<T> {
    return { parameter: T ->
        GeneratorIterator(block = block, paramter = parameter)
    }
}


class GeneratorTest {

    @Test
    fun generator_test() {
        val nums = generator<Int> { start ->
            for (i in 0..5) {
                yield(start + i)
            }
        }

        val gen = nums(0)

        for (j in gen) {
            println(j)
        }
    }
}
