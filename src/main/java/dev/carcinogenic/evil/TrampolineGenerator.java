package dev.carcinogenic.evil;

import dev.carcinogenic.util.DescParser;
import dev.carcinogenic.util.Meme;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static dev.carcinogenic.evil.Injector.$;

/**
 * @author amy
 * @since 3/30/21.
 */
public final class TrampolineGenerator implements Opcodes {
    private TrampolineGenerator() {
    }

    @Nonnull
    public static Class<?> loadEvilClass(final Class<?> cls, final String name, @Nonnull final byte[] bytes) {
        try {
            // Define the trampoline in the same classloader as the target
            // class. If the target can make that call, the trampoline can as
            // well.
            final var cl = cls.getClassLoader();
            final var define = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            define.setAccessible(true);
            final var evil = (Class<?>) define.invoke(cl, name, bytes, 0, bytes.length);
            System.err.println(">> installed new evil class: " + evil.getSimpleName());
            return evil;
        } catch(final NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Nonnull
    public static String name(@Nonnull final String owner, @Nonnull final String name, @Nonnull final String desc) {
        final String base;
        if(owner.contains("CarcinogenicTrampoline")) {
            base = owner;
        } else {
            base = "dev.carcinogenic.evil." + Meme.MEME + ".CarcinogenicTrampoline" + owner + "_$_" + name + "_$_" +
                    desc.replace(";", "")
                            .replace("<", "")
                            .replace(">", "")
                            .replace("(", "")
                            .replace(")", "");
        }
        return $(base);
    }

    @Nonnull
    @SuppressWarnings("UnusedReturnValue")
    public static Class<?> generateProxy(@Nonnull final Class<?> cls, @Nonnull final Method method) {
        final var desc = org.objectweb.asm.commons.Method.getMethod(method).getDescriptor();
        final var name = name(cls.getSimpleName(), method.getName(), desc);

        final var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(59, ACC_PUBLIC + ACC_SUPER, name, null, "java/lang/Object", null);

        {
            final var mw = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "trampoline", desc, null, null);
            mw.visitCode();

            final var params = DescParser.getMethodParams(desc);
            for(int i = 0; i < params.size(); i++) {
                final var p = params.get(i);
                mw.visitIntInsn(SIPUSH, i);
                switch(p) {
                    case "I", "Z", "C", "S", "B" -> mw.visitVarInsn(ILOAD, i);
                    case "L" -> mw.visitVarInsn(LLOAD, i);
                    case "F" -> mw.visitVarInsn(FLOAD, i);
                    case "D" -> mw.visitVarInsn(DLOAD, i);
                    default -> mw.visitVarInsn(ALOAD, i);
                }
            }

            mw.visitMethodInsn(INVOKESTATIC, $(cls), method.getName(), desc, false);
            mw.visitInsn(RETURN);
            mw.visitMaxs(params.size() * 2, params.size() * 2);
            mw.visitEnd();
        }

        cw.visitEnd();

        final var bytes = cw.toByteArray();
        CheckClassAdapter.verify(new ClassReader(bytes), false, new PrintWriter(System.err));

        return loadEvilClass(cls, name.replace('/', '.'), bytes);
    }
}
