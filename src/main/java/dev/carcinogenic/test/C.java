package dev.carcinogenic.test;

/**
 * @author amy
 * @since 4/4/21.
 */
public class C {
    @SuppressWarnings("FieldCanBeLocal")
    private final int hidden = 5;

    @Override
    public String toString() {
        try {
            return "C: hidden = " + hidden + " of type: " + getClass().getDeclaredField("hidden").getType().getName();
        } catch(final NoSuchFieldException e) {
            e.printStackTrace();
            return "what";
        }
    }
}
