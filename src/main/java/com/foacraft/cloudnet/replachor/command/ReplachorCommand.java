package com.foacraft.cloudnet.replachor.command;

import com.foacraft.cloudnet.replachor.ReplachorModule;
import com.foacraft.cloudnet.replachor.config.ConfigManager;
import eu.cloudnetservice.node.command.annotation.CommandAlias;
import eu.cloudnetservice.node.command.annotation.Description;
import eu.cloudnetservice.node.command.source.CommandSource;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * CloudNet-Replachor
 * com.foacraft.cloudnet.replachor.command.ReplachorCommand
 *
 * @author scorez
 * @since 5/21/25 01:19.
 */
@Singleton
@CommandAlias({"replacer", "replachor"})
@Permission("replachor.command")
@Description("control the configuration of replacer.")
public class ReplachorCommand {

    private ReplachorModule replachorModule;
    private ConfigManager configManager;

    @Inject
    public ReplachorCommand(
        @NonNull ReplachorModule replachorModule,
        @NonNull ConfigManager configManager
    ) {
        this.replachorModule = replachorModule;
        this.configManager = configManager;
    }

    @Command("replacer reload")
    public void reload(@NotNull CommandSource source) {
        try {
            this.configManager.loadConfigs();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        source.sendMessage("Replachor was reloaded!");
    }

    @Command("replacer info")
    public void info(@NotNull CommandSource source) {
        // TODO
        source.sendMessage("Replacements below:");
        configManager.getReplacerGroups().forEach(group -> {
            source.sendMessage(" -> " + group.id());
            source.sendMessage("    servicePatterns:");
            group.servicePatterns().forEach(s -> source.sendMessage("        " + s));
            source.sendMessage("    pathsMatchers:");
            group.pathsMatchers().forEach(path -> source.sendMessage("        " + path));
            source.sendMessage("    replacements:");
            group.replacements().forEach(replacement -> {
                source.sendMessage("        " + replacement.placeholder() + "(" + replacement.type() + ") -> " + replacement.content());
            });
        });
    }
}