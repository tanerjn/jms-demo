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
package io.openliberty.guides.models;

import java.util.Objects;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

public class SystemLoad {

    private static final Jsonb JSONB = JsonbBuilder.create();

    public String hostname;
    public Double recentLoad;

    public SystemLoad(String hostname, Double cpuRecentLoad) {
        this.hostname = hostname;
        this.recentLoad = cpuRecentLoad;
    }

    public SystemLoad() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SystemLoad)) {
            return false;
        }
        SystemLoad sl = (SystemLoad) o;
        return Objects.equals(hostname, sl.hostname)
               && Objects.equals(recentLoad, sl.recentLoad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostname, recentLoad);
    }

    @Override
    public String toString() {
        return JSONB.toJson(this);
    }

    public static SystemLoad fromJson(String json) {
        return JSONB.fromJson(json, SystemLoad.class);
    }
}
