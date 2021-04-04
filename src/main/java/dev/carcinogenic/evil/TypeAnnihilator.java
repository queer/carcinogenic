package dev.carcinogenic.evil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author amy
 * @since 4/4/21.
 */
public final class TypeAnnihilator {
    private TypeAnnihilator() {
    }

    @Nonnull
    public static <T> Field annihilate(@Nonnull final Object c, @Nonnull final String field,
                                       @Nonnull final Class<T> type, @Nullable final T value) throws Throwable {
        final var l = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
        MethodHandle privateGetDeclaredFields = null;
        final var modHandle = l.findVarHandle(Field.class, "modifiers", int.class);
        // Get evil from class
        for(final var m : Class.class.getDeclaredMethods()) {
            if(m.getName().startsWith("privateGetDeclaredFields")) {
                final var innerLookup = MethodHandles.privateLookupIn(Class.class, MethodHandles.lookup());
                privateGetDeclaredFields = innerLookup.unreflect(m);
            }
        }
        if(privateGetDeclaredFields == null) {
            throw new IllegalStateException("no class mh");
        }

        Field target = null;
        for(final var f : (Field[]) privateGetDeclaredFields.invoke(c.getClass(), false)) {
            if(f.getName().equals(field)) {
                f.setAccessible(true);
                // Literally just bitwise away `final`
                modHandle.set(f, f.getModifiers() & ~Modifier.FINAL);
                target = f;
                break;
            }
        }
        if(target == null) {
            throw new IllegalStateException("target not found");
        }

        final var vh = l.findVarHandle(Field.class, "type", Class.class);

        vh.set(target, type);
        target.set(c, value);

        return target;
    }
}
