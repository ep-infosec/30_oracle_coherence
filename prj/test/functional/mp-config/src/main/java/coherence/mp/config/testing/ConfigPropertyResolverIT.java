/*
 * Copyright (c) 2000, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package coherence.mp.config.testing;

import com.oracle.coherence.mp.config.ConfigPropertyResolver;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.DefaultCacheServer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests for {@link ConfigPropertyResolver}.
 *
 * @author Aleks Seovic  2019.10.11
 */
class ConfigPropertyResolverIT
    {
    @BeforeAll
    static void setup()
        {
        System.setProperty("coherence.member", "sysprop01");
        System.setProperty("coherence.distributed.localstorage", "false");
        System.setProperty("coherence.cluster", "test");
        }

    @Test
    void testPropertyResolution()
        {
        ConfigPropertyResolver resolver = new ConfigPropertyResolver();

        assertThat(resolver.getProperty("coherence.cluster"), is("test"));
        assertThat(resolver.getEnv("coherence.role"), is("proxy"));
        assertThat(resolver.getEnv("coherence.member"), is("sysprop01"));
        assertThat(resolver.getProperty("coherence.distributed.localstorage"), is("false"));
        }

    @Test
    void testPropertyApplication()
        {
        try
            {
            DefaultCacheServer.startServerDaemon().waitForServiceStart();
            Cluster cluster = CacheFactory.ensureCluster();
            assertThat(cluster.getClusterName(), is("test"));
            assertThat(cluster.getLocalMember().getMemberName(), is("sysprop01"));
            assertThat(cluster.getLocalMember().getRoleName(), is("proxy"));
            }
        finally
            {
            DefaultCacheServer.getInstance().shutdownServer();
            }
        }
    }
