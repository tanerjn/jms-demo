package io.openliberty.guides.inventory;

import io.openliberty.guides.models.SystemLoad;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;

import java.util.logging.Logger;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(
        propertyName = "destinationLookup", propertyValue = "jms/InventoryQueue"),
    @ActivationConfigProperty(
        propertyName = "destinationType", propertyValue = "jakarta.jms.Queue")
})
public class InventoryQueueListener implements MessageListener {

    private static Logger logger =
            Logger.getLogger(InventoryQueueListener.class.getName());

    @Inject
    private InventoryManager manager;

    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String json = textMessage.getText();
                SystemLoad systemLoad = SystemLoad.fromJson(json);

                String hostname = systemLoad.hostname;
                Double recentLoad = systemLoad.recentLoad;
                if (manager.getSystem(hostname).isPresent()) {
                    manager.updateCpuStatus(hostname, recentLoad);
                    logger.info("Host " + hostname + " was updated: " + recentLoad);
                } else {
                    manager.addSystem(hostname, recentLoad);
                    logger.info("Host " + hostname + " was added: " + recentLoad);
                }
            } else {
                logger.warning(
                    "Unsupported Message Type: " + message.getClass().getName());
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
