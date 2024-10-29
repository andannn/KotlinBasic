package coroutines.huo.chapter_3_4

import kotlinx.coroutines.CoroutineName
import org.junit.jupiter.api.Test
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 *
 * 1. removed是从acc中移除了element的CoroutineContext
 * 2. 如果removed是EmptyCoroutineContext，说明element是最后一个元素，直接返回element
 * 3. 否则判断removed中是否有ContinuationInterceptor，如果没有， 则将element添加到removed中
 * 4. 如果有ContinuationInterceptor，则将element添加到集合中， 并且将ContinuationInterceptor放到最后
 * ```
 *     public operator fun plus(context: CoroutineContext): CoroutineContext =
 *         if (context === EmptyCoroutineContext) this else // fast path -- avoid lambda creation
 *             context.fold(this) { acc, element ->
 *                 val removed = acc.minusKey(element.key)  ①
 *                 if (removed === EmptyCoroutineContext) element else {　　②
 *                     // make sure interceptor is always last in the context (and thus is fast to get when present)
 *                     val interceptor = removed[ContinuationInterceptor]  ③
 *                     if (interceptor == null) CombinedContext(removed, element) else {
 *                         val left = removed.minusKey(ContinuationInterceptor)  ④
 *                         if (left === EmptyCoroutineContext) CombinedContext(element, interceptor) else
 *                             CombinedContext(CombinedContext(left, element), interceptor)
 *                     }
 *                 }
 *             }
 * ```
 */
class CoroutineContextTest {

    @Test
    fun test() {
        val name1 = CoroutineName("Name_1")
        val name2 = CoroutineName("Name_2")
        val context = name1 + name2

        assert(context[CoroutineName] == name2)
        assert(context.minusKey(CoroutineName) == EmptyCoroutineContext)
    }

    @Test
    fun test2() {
        val element1 = CoroutineName("Name_1")
        val element2 = CoroutineName("Name_2")
        val element3 = CustomElement()
        val context = element1 + element2 + element3

        assert(context[CoroutineName] == element2)
        assert(context[CustomElement] == element3)
    }

    @Test
    fun test3() {
        val element1 = CustomInterceptor()
        val element2 = CoroutineName("Name_2")
        val element3 = CoroutineName("Name_3")
        var context = element1 + element2

        assert(context[ContinuationInterceptor] == element1)
        println(context)

        context += element3

        assert(context[ContinuationInterceptor] == element1)
        println(context)
    }

    /**
     *
     * 从context中取得ContinuationInterceptor是不能使用自己的key， 应该使用ContinuationInterceptor
     *
     * kotlin-stdlib-1.9.21-sources.jar!/commonMain/kotlin/coroutines/ContinuationInterceptor.kt
     * ```
     *     public override operator fun <E : CoroutineContext.Element> get(key: CoroutineContext.Key<E>): E? {
     *         // getPolymorphicKey specialized for ContinuationInterceptor key
     *         @OptIn(ExperimentalStdlibApi::class)
     *         if (key is AbstractCoroutineContextKey<*, *>) {
     *             @Suppress("UNCHECKED_CAST")
     *             return if (key.isSubKey(this.key)) key.tryCast(this) as? E else null
     *         }
     *         @Suppress("UNCHECKED_CAST")
     *         return if (ContinuationInterceptor === key) this as E else null
     *     }
     * ```
     */
    @Test
    fun test4() {
        val element1 = CustomInterceptor()
        val element2 = CoroutineName("Name_2")
        val element3 = CustomInterceptor2()
        val context = element1 + element2 + element3

        println(context)

        assert(context[ContinuationInterceptor] == element1)
//        assert(context[CustomInterceptor2] == element3)  // Assertion failed
    }
}

private class CustomElement : AbstractCoroutineContextElement(CustomElement) {

    public companion object Key : CoroutineContext.Key<CustomElement>

    override fun toString(): String = "CustomElement"
}

private class CustomInterceptor : ContinuationInterceptor {
    public companion object Key : CoroutineContext.Key<CustomInterceptor>

    override val key: CoroutineContext.Key<*>
        get() = CustomInterceptor

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        return continuation
    }
}

private class CustomInterceptor2 : ContinuationInterceptor {
    public companion object Key : CoroutineContext.Key<CustomInterceptor2>

    override val key: CoroutineContext.Key<*>
        get() = CustomInterceptor2

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        return continuation
    }
}