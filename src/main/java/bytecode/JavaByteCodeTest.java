package bytecode;

import java.util.List;

class JavaByteCodeTest {
    String a = "aa";
    int b = 1;
    long c = 2L;
    float d = 3.0f;
    double e = 4.0;
    boolean f = true;
    char g = 'g';
    byte h = 0x7;
    short i = 0x7;
    int j = 0x7;

    List<String> stringList;

    //65 new #18 <java/lang/Object>
    //68 dup
    //69 invokespecial #1 <java/lang/Object.<init> : ()V>
    //72 putfield #19 <bytecode/JavaByteCodeTest.obj : Ljava/lang/Object;>
    Object obj = new Object();

    static int staticFiled = 1;

    static final int staticFinalFiled = 1;

    static {
        int a = 1;
        int b = 2;
        int c = a + b;
    }

    public static void main(String[] args) {
    }

    public int returnInt1() {
        return 1;
    }

    public int returnInt100() {
        return 100;
    }

    public void voidMethod() {
    }

    public void voidMethodWithArgs(int a, String b) {
    }

    public void voidMethodWithArgsThrows(int a, String b) throws IllegalAccessError {
    }
}
