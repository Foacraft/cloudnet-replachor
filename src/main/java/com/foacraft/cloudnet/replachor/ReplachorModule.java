package com.foacraft.cloudnet.replachor;

import com.foacraft.cloudnet.replachor.command.ReplachorCommand;
import com.foacraft.cloudnet.replachor.config.ConfigManager;
import com.foacraft.cloudnet.replachor.listener.ServiceListener;
import eu.cloudnetservice.driver.event.EventManager;
import eu.cloudnetservice.driver.module.ModuleLifeCycle;
import eu.cloudnetservice.driver.module.ModuleTask;
import eu.cloudnetservice.driver.module.driver.DriverModule;
import eu.cloudnetservice.driver.template.TemplateStorageProvider;
import eu.cloudnetservice.node.command.CommandProvider;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;

/**
 * CloudNet-Storage-Local-Versalize
 * com.foacraft.cloudnet.tmux.services.TmuxServicesModule
 *
 * @author scorez
 * @since 12/11/23 23:48.
 */
@Singleton
public class ReplachorModule extends DriverModule {

    @Getter
    private ConfigManager configManager;

    private EventManager eventManager;
    private TemplateStorageProvider templateStorageProvider;

    @ModuleTask(order = 10, lifecycle = ModuleLifeCycle.LOADED)
    public void loadModule(
            @NonNull EventManager eventManager,
            @NonNull TemplateStorageProvider templateStorageProvider,
            @NonNull CommandProvider commandProvider,
            @NonNull ConfigManager configManager
    ) {
        this.eventManager = eventManager;
        this.templateStorageProvider = templateStorageProvider;
        this.configManager = configManager;
        eventManager.registerListener(ServiceListener.class);
        commandProvider.register(ReplachorCommand.class);

        try {
            this.configManager.loadConfigs();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
