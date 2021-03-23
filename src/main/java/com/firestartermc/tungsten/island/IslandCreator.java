package com.firestartermc.tungsten.island;

import com.firestartermc.tungsten.util.Region;
import com.firestartermc.tungsten.util.model.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class IslandCreator {

    private final IslandGrid grid;

    public IslandCreator(@NotNull IslandGrid grid) {
        this.grid = grid;
    }

    @NotNull
    private CompletableFuture<Pair<Long, Long>> findNextAvailableRegion() {
        do {
            int x = ThreadLocalRandom.current().nextInt(0, 30000000);
            int z = ThreadLocalRandom.current().nextInt(0, 30000000);
            Region region = Region.fromBlock(x, z);

            
        } while (true); // might wanna put a limit on this
    }
}
