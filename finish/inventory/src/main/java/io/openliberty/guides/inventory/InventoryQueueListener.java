// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
// end::copyright[]
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

// tag::messageDriven[]
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(
        propertyName = "destinationLookup", propertyValue = "jms/InventoryQueue"),
    @ActivationConfigProperty(
        propertyName = "destinationType", propertyValue = "jakarta.jms.Queue")
})
// end::messageDriven[]
// tag::InventoryQueueListener[]
public class InventoryQueueListener implements MessageListener {

    private static Logger logger =
            Logger.getLogger(InventoryQueueListener.class.getName());

    // tag::InventoryManager[]
    @Inject
    private InventoryManager manager;
    //end::InventoryManager[]

    // tag::onMessage[]
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String json = textMessage.getText();
                // tag::systemLoad[]
                SystemLoad systemLoad = SystemLoad.fromJson(json);
                // end::systemLoad[]

                String hostname = systemLoad.hostname;
                Double recentLoad = systemLoad.recentLoad;
                // tag::InventoryManagerUpdate[]
                if (manager.getSystem(hostname).isPresent()) {
                    manager.updateCpuStatus(hostname, recentLoad);
                    logger.info("Host " + hostname + " was updated: " + recentLoad);
                } else {
                    manager.addSystem(hostname, recentLoad);
                    logger.info("Host " + hostname + " was added: " + recentLoad);
                }
                // end::InventoryManagerUpdate[]
            } else {
                logger.warning(
                    "Unsupported Message Type: " + message.getClass().getName());
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    // end::onMessage[]

}
// end::InventoryQueueListener[]
