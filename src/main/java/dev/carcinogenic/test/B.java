package dev.carcinogenic.test;

import dev.carcinogenic.evil.annotation.Trampoline;

import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 3/30/21.
 */
public class B {
    @Trampoline
    public static void b() {
        System.out.println("B CALLED :D");
        new Exception().printStackTrace();
    }

    @Trampoline
    public static void b2(@Nonnull final String test) {
        System.out.println("B GOT INPUT: " + test);
        new Exception().printStackTrace();
    }
}
