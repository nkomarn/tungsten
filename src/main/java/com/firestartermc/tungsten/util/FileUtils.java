package com.firestartermc.tungsten.util;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;

public class FileUtils {

    /**
     * Attempts to load a resource from a {@link Plugin}'s data folder. If the
     * resource does not exist in the data folder, this utility attempts to fetch
     * the resource from the plugin's JAR file and copy it to the data folder.
     *
     * @param dataDirectory The plugin's data directory.
     * @param resource      The resource's full file name, with extension.
     * @return The resource file after loading and/or copying.
     * @since 1.0
     */
    public static Optional<File> loadOrSaveResource(@NotNull File dataDirectory, @NotNull String resource) {
        if (!dataDirectory.exists()) {
            dataDirectory.mkdir();
        }

        File file = new File(dataDirectory, resource);

        if (!file.exists()) {
            try {
                InputStream fileStream = FileUtils.class.getResourceAsStream(resource);

                if (fileStream == null) {
                    return Optional.empty();
                }

                Files.copy(fileStream, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Optional.of(file);
    }
}
