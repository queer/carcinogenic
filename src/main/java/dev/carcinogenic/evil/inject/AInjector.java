package dev.carcinogenic.evil.inject;

import dev.carcinogenic.evil.Injector;
import dev.carcinogenic.evil.TrampolineGenerator;
import dev.carcinogenic.test.A;
import dev.carcinogenic.test.B;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * @author amy
 * @since 3/30/21.
 */
public class AInjector extends Injector {
    public AInjector() {
        super(A.class);
    }

    @Override
    protected void inject(final ClassReader cr, final ClassNode cn) {
        System.err.println(">> agent: injector: rewriting trampoline caller");

        cn.methods.forEach(mn -> {
            for(int i = 0; i < mn.instructions.size(); i++) {
                final var insn = mn.instructions.get(i);
                switch(insn.getOpcode()) {
                    case INVOKEVIRTUAL, INVOKESTATIC, INVOKEDYNAMIC -> {
                        final var minsn = (MethodInsnNode) insn;
                        if(minsn.owner.equals($(B.class))) {
                            final var split = minsn.owner.split("/");
                            minsn.owner = TrampolineGenerator.name(split[split.length - 1], minsn.name, minsn.desc);
                            minsn.name = "trampoline";
                        }
                    }
                    default -> {
                    }
                }
            }
        });

        System.err.println(">> agent: injector: rewrote trampoline caller");
    }
}
