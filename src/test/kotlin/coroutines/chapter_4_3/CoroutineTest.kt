package coroutines.chapter_4_3

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

sealed class Status {
    class Created(val continuation: Continuation<Unit>) : Status()

    class Yielded<P>(val continuation: Continuation<P>) : Status()

    class Resumed<R>(val continuation: Continuation<R>) : Status()

    data object Dead : Status()
}

interface CoroutineScope<P, R> {
    val parameter: P?

    suspend fun yield(value: R): P
}

class Coroutine<P, R>(
    private val tag: String = "",
    override val context: CoroutineContext = EmptyCoroutineContext,
    private val block: suspend CoroutineScope<P, R>.(P) -> R
) : Continuation<R> , CoroutineScope<P, R> {

    companion object {
        fun <P, R> create(
            tag: String = "",
            context: CoroutineContext = CoroutineName("Coroutine_$tag"),
            block: suspend CoroutineScope<P, R>.(P) -> R
        ): Coroutine<P, R> {
            return Coroutine(tag = tag,context = context, block = block)
        }
    }
    private var startParameter: P? = null

    private val status: AtomicReference<Status>

    val isActive: Boolean
        get() = status.get() != Status.Dead

    init {
        val coroutineBlock: suspend CoroutineScope<P, R>.() -> R = { block(startParameter!!) }
        val start = coroutineBlock.createCoroutine(this, this)
        status = AtomicReference(Status.Created(start))
    }

    suspend fun resumeCoroutine(value: P): R = suspendCoroutine { continuation ->
        val previousState = status.getAndUpdate {
            when (it) {
                is Status.Created -> {
                    startParameter = value
                    Status.Resumed(continuation)
                }
                Status.Dead -> error("Dead")
                is Status.Resumed<*> -> {
                    error("Already resumed $tag")
                }
                is Status.Yielded<*> -> {
                    Status.Resumed(continuation)
                }
            }
        }

        when (previousState) {
            is Status.Created -> previousState.continuation.resume(Unit)
            is Status.Yielded<*> ->{
                (previousState as Status.Yielded<P>).continuation.resume(value)
            }
            else -> error("Invalid state")
        }
    }

    override fun resumeWith(result: Result<R>) {
        val previousState = status.getAndUpdate {
            when (it) {
                is Status.Created -> error("Not started")
                is Status.Resumed<*> -> Status.Dead
                is Status.Yielded<*> -> error("Not resumed $tag")
                Status.Dead -> error("Dead")
            }
        }

        (previousState as? Status.Resumed<R>)?.continuation?.resumeWith(result)
    }

    override val parameter: P? = null

    override suspend fun yield(value: R): P = suspendCoroutine { continuation ->
        val previousStatus = status.getAndUpdate {
            when (it) {
                is Status.Created -> error("Not started")
                is Status.Resumed<*> -> {
                    Status.Yielded(continuation)
                }
                is Status.Yielded<*> -> error("Already yielded")
                Status.Dead -> error("Dead")
            }
        }

        (previousStatus as? Status.Resumed<R>)?.continuation?.resume(value)
    }
}


class CoroutineTest {

    @Test
    fun run_test(): Unit = runBlocking {

        val producer = Coroutine.create<Unit, Int>(tag = "Producer") {
            for (i in 0..2) {
                println("Produced: $i")
                yield(i)
            }
            200
        }

        val consumer = Coroutine.create<Int, Unit>(tag = "Consumer") { parameter ->
            println(parameter)

            for (i in 0..2) {
                val value = yield(Unit)
                println("Consumed: $value")
            }
        }

        launch {
            while (producer.isActive && consumer.isActive) {
                val result = producer.resumeCoroutine(Unit)
                consumer.resumeCoroutine(result)
            }
        }
    }
}