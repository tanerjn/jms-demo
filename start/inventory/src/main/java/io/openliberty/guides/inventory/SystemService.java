package io.openliberty.guides.system;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import com.sun.management.OperatingSystemMXBean;

import io.openliberty.guides.models.SystemLoad;
import jakarta.annotation.Resource;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.jms.JMSConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;

@Singleton
public class SystemService {

    private static final OperatingSystemMXBean OS_MEAN =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static String hostname = null;

    private static Logger logger = Logger.getLogger(SystemService.class.getName());

    @Inject
    @JMSConnectionFactory("InventoryConnectionFactory")
    private JMSContext context;

    @Resource(lookup = "jms/InventoryQueue")
    private Queue queue;

    private static String getHostname() {
        if (hostname == null) {
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                return System.getenv("HOSTNAME");
            }
        }
        return hostname;
    }

    @Schedule(second = "*/15", minute = "*", hour = "*", persistent = false)
    public void sendSystemLoad() {
        SystemLoad systemLoad = new SystemLoad(getHostname(),
                                    Double.valueOf(OS_MEAN.getCpuLoad()));
        context.createProducer().send(queue, systemLoad.toString());
        logger.info(systemLoad.toString());
    }
}
