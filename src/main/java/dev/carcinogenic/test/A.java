package dev.carcinogenic.test;

/**
 * @author amy
 * @since 3/30/21.
 */
public class A {
    public static void a() {
        System.out.println("CALLING INTO B");
        B.b();
    }

    public static void a2() {
        B.b2("test");
    }
}
