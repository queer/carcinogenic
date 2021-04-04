package dev.carcinogenic.evil;

import org.immutables.value.Value.Immutable;

/**
 * @author amy
 * @since 3/31/21.
 */
@Immutable
public interface Call {
    String owner();

    String name();

    String desc();
}
