/*
 * Copyright (c) 2020, 2022 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.cdi.server;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.OperationalContext;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * A CDI producer that produces instances of the singleton Coherence {@link
 * Cluster}.
 *
 * @author Jonathan Knight  2019.11.20
 * @since 20.06
 */
@ApplicationScoped
public class ClusterProducer
    {
    /**
     * Produces the singleton Coherence {@link Cluster} instance.
     *
     * @return the singleton Coherence {@link Cluster} instance
     */
    @Produces
    public Cluster getCluster()
        {
        return CacheFactory.ensureCluster();
        }

    /**
     * Produces the singleton Coherence {@link OperationalContext} instance.
     *
     * @return the singleton Coherence {@link OperationalContext} instance
     */
    @Produces
    public OperationalContext getOperationalContext()
        {
        return (OperationalContext) CacheFactory.getCluster();
        }
    }
