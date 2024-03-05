package coroutines;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class JavaCallKotlinSuspendFunctionTest {

    @Test
    public void java_call_kotlin_suspend_function() {
        Object result = CoroutineTestKt.noSuspend(new Continuation<Integer>() {
            @NotNull
            @Override
            public CoroutineContext getContext() {
                return null;
            }

            @Override
            public void resumeWith(@NotNull Object o) {

            }
        });
    }
}
