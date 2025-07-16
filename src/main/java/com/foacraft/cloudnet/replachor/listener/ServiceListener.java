package com.foacraft.cloudnet.replachor.listener;

import com.foacraft.cloudnet.replachor.replacer.ReplaceManager;
import com.google.common.collect.Sets;
import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.driver.service.ServiceLifeCycle;
import eu.cloudnetservice.node.event.service.CloudServicePostLifecycleEvent;
import eu.cloudnetservice.node.event.service.CloudServicePostPrepareEvent;
import eu.cloudnetservice.node.event.service.CloudServicePostProcessStartEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;

/**
 * CloudNet-Replachor
 * com.foacraft.cloudnet.replachor.listener.ServiceListener
 *
 * @author scorez
 * @since 7/15/25 14:12.
 */
@Singleton
public class ServiceListener {

    private final static Logger logger = LoggerFactory.getLogger(ServiceListener.class);
    private final Set<UUID> triggeredServices = Sets.newConcurrentHashSet();

    private final ReplaceManager replaceManager;

    @Inject
    public ServiceListener(@NonNull ReplaceManager replaceManager) {
        this.replaceManager = replaceManager;
    }

    @EventListener
    public void e(CloudServicePostPrepareEvent e) {
        replaceManager.process(e.service());
        triggeredServices.add(e.service().serviceId().uniqueId());
    }

    @EventListener
    public void e(CloudServicePostLifecycleEvent e) {
        if (e.newLifeCycle() == ServiceLifeCycle.DELETED || e.newLifeCycle() == ServiceLifeCycle.STOPPED) {
            triggeredServices.remove(e.serviceInfo().serviceId().uniqueId());
        }
    }

    @EventListener
    public void e(CloudServicePostProcessStartEvent e) {
        if (triggeredServices.contains(e.service().serviceId().uniqueId())) {
            return;
        }
        logger.info(
            "A service was abnormal to start, reexecute the processer of replacement with name={}, uniqueId={}",
            e.service().serviceId().name(),
            e.service().serviceId().uniqueId()
        );
        // Because the CloudNet exist a problem that service might be change the unique id during the post-start to .
        replaceManager.process(e.service());
    }

}
