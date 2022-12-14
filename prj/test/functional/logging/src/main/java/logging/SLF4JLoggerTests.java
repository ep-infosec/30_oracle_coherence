/*
 * Copyright (c) 2000, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package logging;

import com.tangosol.net.CacheFactory;

import com.oracle.bedrock.testsupport.deferred.Eventually;

import com.oracle.coherence.testing.AbstractFunctionalTest;

import logging.impl.DebugLogger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.is;

/**
 * Functional test of the SLF4J logger.
 *
 * @author jh  2012.01.23
 */
public class SLF4JLoggerTests
        extends AbstractFunctionalTest
    {
    // ----- test lifecycle -------------------------------------------------

    @BeforeClass
    public static void _startup()
        {
        System.setProperty("test.log", "slf4j");
        System.setProperty("test.log.name", "SLF4J");
        System.setProperty("coherence.override", "logging-coherence-override.xml");
        System.setProperty("coherence.cacheconfig", "logging-cache-config.xml");
        }

    @BeforeClass
    public static void _shutdown()
        {
        }

    @Before
    public void setupLogger()
        {
        DebugLogger logger = (DebugLogger) LoggerFactory.getLogger("SLF4J");
        logger.resetLogOutput();
        m_logger = logger;
        }

    // ----- test methods ---------------------------------------------------

    @Test
    public void testLogLevel0()
        {
        DebugLogger logger = m_logger;
        CacheFactory.log(DebugLogger.CAPTURE_PREFIX + "This is log level 0", 0);

        Eventually.assertDeferred(() -> logger.getLogOutput(), is("Info SLF4J - This is log level 0\n"));
        }

    @Test
    public void testLogLevel1()
        {
        DebugLogger logger = m_logger;
        CacheFactory.log(DebugLogger.CAPTURE_PREFIX + "This is log level 1", 1);

        Eventually.assertDeferred(() -> logger.getLogOutput(), is("Error SLF4J - This is log level 1\n"));
        }

    @Test
    public void testLogLevel2()
        {
        DebugLogger logger = m_logger;
        CacheFactory.log(DebugLogger.CAPTURE_PREFIX + "This is log level 2", 2);

        Eventually.assertDeferred(() -> logger.getLogOutput(), is("Warn SLF4J - This is log level 2\n"));
        }

    @Test
    public void testLogLevel3()
        {
        DebugLogger logger = m_logger;
        CacheFactory.log(DebugLogger.CAPTURE_PREFIX + "This is log level 3", 3);

        Eventually.assertDeferred(() -> logger.getLogOutput(), is("Info SLF4J - This is log level 3\n"));
        }

    @Test
    public void testLogLevel4()
        {
        DebugLogger logger = m_logger;
        CacheFactory.log(DebugLogger.CAPTURE_PREFIX + "This is log level 4", 4);

        Eventually.assertDeferred(() -> logger.getLogOutput(), is("Info SLF4J - This is log level 4\n"));
        }

    @Test
    public void testLogLevel5()
        {
        DebugLogger logger = m_logger;
        CacheFactory.log(DebugLogger.CAPTURE_PREFIX + "This is log level 5", 5);

        Eventually.assertDeferred(() -> logger.getLogOutput(), is("Debug SLF4J - This is log level 5\n"));
        }

    @Test
    public void testLogLevel6()
        {
        DebugLogger logger = m_logger;
        CacheFactory.log(DebugLogger.CAPTURE_PREFIX + "This is log level 6", 6);

        Eventually.assertDeferred(() -> logger.getLogOutput(), is("Debug SLF4J - This is log level 6\n"));
        }

    @Test
    public void testLogLevel7()
        {
        DebugLogger logger = m_logger;
        CacheFactory.log(DebugLogger.CAPTURE_PREFIX + "This is log level 7", 7);

        Eventually.assertDeferred(() -> logger.getLogOutput(), is("Trace SLF4J - This is log level 7\n"));
        }

    // ----- data members ---------------------------------------------------

    private DebugLogger m_logger;
    }
