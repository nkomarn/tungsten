package com.firestartermc.tungsten.util.model;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

/**
 * A generic, immutable tuple implementation.
 *
 * @param <L> The left value.
 * @param <R> The right value.
 * @author SpaceDelta
 * @since 1.0
 */
@Immutable
public class Pair<L, R> {

    private final L left;
    private final R right;

    private Pair(@NotNull L left, @NotNull R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Pair<L, R> of(@NotNull L left, @NotNull R right) {
        return new Pair<>(left, right);
    }

    @NotNull
    public L getLeft() {
        return left;
    }

    @NotNull
    public R getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(left, pair.left) && Objects.equals(right, pair.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public String toString() {
        return "Pair{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }
}