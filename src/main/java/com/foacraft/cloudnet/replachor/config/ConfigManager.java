package com.foacraft.cloudnet.replachor.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.yaml.YamlFormat;
import com.foacraft.cloudnet.replachor.ReplachorModule;
import com.foacraft.cloudnet.replachor.config.entry.Replacement;
import com.foacraft.cloudnet.replachor.config.entry.ReplacerGroup;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CloudNet-Replachor
 * com.foacraft.cloudnet.replachor.config.ConfigManager
 *
 * @author scorez
 * @since 7/15/25 15:48.
 */
@Singleton
public class ConfigManager {

    @Getter
    private final ReplachorModule replachorModule;
    private final Path baseDir;

    @Getter
    private List<ReplacerGroup> replacerGroups;

    @Inject
    public ConfigManager(@NotNull ReplachorModule replachorModule) {
        this.replachorModule = replachorModule;
        this.baseDir = replachorModule.moduleWrapper().dataDirectory();
    }

    public void loadConfigs() throws IOException {
        if (Files.notExists(baseDir)) {
            Files.createDirectories(baseDir);
            var examplePath = baseDir.resolve("example.yml");
            Files.createFile(examplePath);
            try (InputStream in = ReplachorModule.class.getResourceAsStream("/example.yml")) {
                Files.copy(in, examplePath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        this.replacerGroups = new ArrayList<>();
        Files.list(baseDir).forEach(path -> {
            ConfigFormat format = YamlFormat.defaultInstance();
            var config = CommentedFileConfig.builder(path, format).build();
            config.load();

            for (Config.Entry entry : config.entrySet()) {
                String id = entry.getKey();
                Config groupSection = entry.getValue();

                List<String> servicePatterns = groupSection.get("service-patterns");
                List<String> pathsMatchers = groupSection.get("paths-matchers");

                var replacements = new ArrayList<Replacement>();
                Config replacementsSection = groupSection.get("replacements");
                if (replacementsSection == null) {
                    continue;
                }
                for (Config.Entry replacementEntry : replacementsSection.entrySet()) {
                    String placeholder = replacementEntry.getKey();
                    Object value = replacementEntry.getValue();

                    Replacement replacement;
                    if (value instanceof List<?> rawList) {
                        List<String> contentList = rawList.stream().map(String::valueOf).collect(Collectors.toList());
                        replacement = new Replacement(placeholder, Replacement.Type.RANDOM, contentList);
                    } else {
                        replacement = new Replacement(placeholder, Replacement.Type.SINGLE, List.of(String.valueOf(value)));
                    }
                    replacements.add(replacement);
                }

                ReplacerGroup replacerGroup = new ReplacerGroup(id, servicePatterns, pathsMatchers, replacements);
                replacerGroups.add(replacerGroup);
            }
        });
    }

}
