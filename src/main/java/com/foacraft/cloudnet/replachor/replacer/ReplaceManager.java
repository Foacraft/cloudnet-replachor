package com.foacraft.cloudnet.replachor.replacer;

import com.foacraft.cloudnet.replachor.config.ConfigManager;
import com.foacraft.cloudnet.replachor.config.entry.Replacement;
import com.foacraft.cloudnet.replachor.config.entry.ReplacerGroup;
import eu.cloudnetservice.node.service.CloudService;
import eu.cloudnetservice.modules.bridge.BridgeServiceHelper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;

/**
 * CloudNet-Replachor
 * com.foacraft.cloudnet.replachor.replacer.ReplaceManager
 *
 * @author scorez
 * @since 7/16/25 14:52.
 */
@Singleton
public class ReplaceManager {

    private static final Logger logger = LoggerFactory.getLogger(ReplaceManager.class);

    private final PathSearcher pathSearcher;
    private final ConfigManager configManager;

    @Inject
    public ReplaceManager(@NonNull PathSearcher pathSearcher, @NonNull ConfigManager configManager) {
        this.pathSearcher = pathSearcher;
        this.configManager = configManager;
    }

    public void process(CloudService service) {
        var groups = filterGroups(service.serviceId().name());
        if (groups.isEmpty()) {
            return;
        }
        groups.forEach(group -> {
            pathSearcher.search(service.directory(), group.pathsMatchers()).forEach(path -> {
                try {
                    var fileContent = Files.readString(path);
                    for (Replacement replacement : group.replacements()) {
                        if (replacement.type() == Replacement.Type.SINGLE) {
                            var content = replacement.content().stream().findFirst().get();
                            fileContent = fileContent.replace(
                                replacement.placeholder(),
                                BridgeServiceHelper.fillCommonPlaceholders(content, null, service.serviceInfo())
                            );
                        } else {
                            var random = new Random();
                            var content = replacement.content().get(random.nextInt(fileContent.length()));
                            fileContent = fileContent.replace(
                                replacement.placeholder(),
                                BridgeServiceHelper.fillCommonPlaceholders(content, null, service.serviceInfo())
                            );
                        }
                    }
                    Files.writeString(path, fileContent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public List<ReplacerGroup> filterGroups(String name) {
        return configManager.getReplacerGroups().stream()
                .filter(group -> group.servicePatterns().stream().anyMatch(name::matches))
                .toList();
    }
}
