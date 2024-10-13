package coroutines

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class ChannelTest {

    @Test
    fun `RENDEZVOUS channel`(): Unit = runBlocking {

        // no buffer, every send must wait the collector finish.
        val channel = Channel<Int>(
            capacity = RENDEZVOUS,
            onBufferOverflow = BufferOverflow.SUSPEND,
        )

        collectChannel(channel)
    }

    @Test
    fun `DROP_LATEST channel`(): Unit = runBlocking {
        // no buffer, drop the latest item (never suspend).
        val channel = Channel<Int>(
            capacity = RENDEZVOUS,
            onBufferOverflow = BufferOverflow.DROP_LATEST,
        )

        // only first item collected
        collectChannel(channel)
    }

    @Test
    fun `UNLIMITED channel`(): Unit = runBlocking {
        // no-limited buffer, send the signal to buffer (never suspend).
        val channel = Channel<Int>(
            capacity = UNLIMITED,
            onBufferOverflow = BufferOverflow.SUSPEND,
        )

        collectChannel(channel)
    }


    @Test
    fun `BUFFERED channel`(): Unit = runBlocking {
        // 64 buffer, send the signal to buffer, and suspend when buffer is full.
        val channel = Channel<Int>(
            capacity = BUFFERED,
        )

        collectChannel(channel, count = 65)
    }

    @Test
    fun `CONFLATED channel`(): Unit = runBlocking {
        // same with onBufferOverflow = DROP_OLDEST.
        val channel = Channel<Int>(
            capacity = CONFLATED,
        )

        collectChannel(channel)
    }


    private suspend fun collectChannel(channel: Channel<Int>, count: Int = 4) = coroutineScope {
        launch {
            List(count) { it }.forEach {
                println("send E $it")
                channel.send(it)
                println("send X $it")
            }
            channel.close()
        }

        launch {
            for (item in channel) {
                println("collector 1: receive $item")
                delay(100)
            }
        }

        launch {
            for (item in channel) {
                println("collector 2: receive $item")
                delay(300)
            }
        }
    }
}