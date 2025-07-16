package com.foacraft.cloudnet.replachor.command;

import com.foacraft.cloudnet.replachor.ReplachorModule;
import eu.cloudnetservice.node.command.annotation.CommandAlias;
import eu.cloudnetservice.node.command.annotation.Description;
import eu.cloudnetservice.node.command.source.CommandSource;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.jetbrains.annotations.NotNull;

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

    @Inject
    public ReplachorCommand(
        @NonNull ReplachorModule replachorModule
    ) {
        this.replachorModule = replachorModule;
    }

    @Command("replacer reload")
    public void reload(@NotNull CommandSource source) {
        // TODO
        source.sendMessage("Replachor was reloaded!");
    }
}