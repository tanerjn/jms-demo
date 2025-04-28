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
package io.openliberty.guides.system.health;

import java.net.Socket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class SystemReadinessCheck implements HealthCheck {

    @Inject
    @ConfigProperty(name = "inventory.jms.host", defaultValue = "localhost")
    String inventoryJmsHost;

    @Inject
    @ConfigProperty(name = "inventory.jms.port", defaultValue = "7277")
    Integer inventoryJmsPort;

    @Override
    public HealthCheckResponse call() {
        boolean up = isReady();
        return HealthCheckResponse.named(
            this.getClass().getSimpleName()).status(up).build();
    }

    private boolean isReady() {
        try {
            Socket socket = new Socket(inventoryJmsHost, inventoryJmsPort);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
