package com.firestartermc.tungsten.util;

import com.firestartermc.tungsten.Tungsten;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@ThreadSafe
public final class ConcurrentUtils {

    private ConcurrentUtils() {
    }

    public static void ensureMain(@NotNull Runnable runnable) {
        // if (!Sponge.getGame().getPlatform().) {
        Task.builder()
                .execute(runnable)
                .submit(Tungsten.INSTANCE);
        /* } else {
            runnable.run();
        } */
    }

    public static CompletableFuture<Void> callAsync(@NotNull HandledConsumer consumer) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                consumer.accept();
                return null;
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    public static <T> CompletableFuture<T> callAsync(@NotNull Callable<T> callable) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    @FunctionalInterface
    public interface HandledConsumer {
        void accept() throws Exception;
    }
}