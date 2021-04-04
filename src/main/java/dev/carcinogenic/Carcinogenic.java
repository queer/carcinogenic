package dev.carcinogenic;

import dev.carcinogenic.evil.Injector;
import dev.carcinogenic.evil.TrampolineGenerator;
import dev.carcinogenic.evil.TypeAnnihilator;
import dev.carcinogenic.evil.annotation.Trampoline;
import dev.carcinogenic.evil.inject.AInjector;
import dev.carcinogenic.test.C;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import net.bytebuddy.agent.ByteBuddyAgent;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author amy
 * @since 3/24/21.
 */
public final class Carcinogenic {
    private static final List<Injector> INJECTORS = List.of(new AInjector());

    private Carcinogenic() {
    }

    public static void main(final String[] args) throws Throwable {
        final var i = ByteBuddyAgent.install();


        // Open up some base Java modules / packages to allow all SORTS of horrors :D
        // java.lang
        System.out.println(">> can do evil: java.lang: " + i.isModifiableModule(String.class.getModule()));
        i.redefineModule(String.class.getModule(), Set.of(), Map.of(), Map.of("java.lang", Set.of(Carcinogenic.class.getModule())), Set.of(), Map.of());

        // java.lang.reflect
        System.out.println(">> can do evil: java.lang.reflect: " + i.isModifiableModule(Field.class.getModule()));
        i.redefineModule(Field.class.getModule(), Set.of(), Map.of(), Map.of("java.lang.reflect", Set.of(Carcinogenic.class.getModule())), Set.of(), Map.of());

        try(final var res = new ClassGraph().enableAllInfo().scan()) {
            for(final ClassInfo classInfo : res.getClassesWithMethodAnnotation(Trampoline.class.getName())) {
                final Class<?> cls = classInfo.loadClass();
                for(final var m : cls.getDeclaredMethods()) {
                    if(m.isAnnotationPresent(Trampoline.class)) {
                        m.setAccessible(true);
                        proxy(cls, m);
                    }
                }
            }
        }

        for(final Injector injector : INJECTORS) {
            i.addTransformer(injector, true);
        }
        final var classes = forceLoad();
        i.retransformClasses(classes);

        final var c = new C();
        System.out.println(">> annihilating: " + c);
        final var targetField = TypeAnnihilator.annihilate(c, "hidden", List.class, List.of("haha evil type murder"));
        System.out.println(">> annihilated: type = " + targetField.getType().getName() + ", value = " + targetField.get(c));

//        A.a();
//        A.a2();
    }

    private static void proxy(@Nonnull final Class<?> cls, @Nonnull final Method method) {
        final var evil = TrampolineGenerator.generateProxy(cls, method);
        System.out.println(">> agent: generated new evil class: " + evil.getSimpleName());
    }

    private static Class<?>[] forceLoad() {
        return INJECTORS.stream()
                .map(Injector::getClassToInject)
                .peek(c -> System.out.println(">> agent: agentmain: forcibly loading " + c))
                .collect(Collectors.toList())
                .toArray(Class[]::new);
    }
}
