/*
 * Copyright (c) 2000, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package logging;

import com.oracle.bedrock.testsupport.deferred.Eventually;

import com.oracle.coherence.common.base.Logger;
import com.oracle.coherence.testing.AbstractFunctionalTest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.StringWriter;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Functional test of the Log4j 2 logging functionality.
 *
 * @author shing.wai.chan  2018.08.10
 */
public class Log4j2Tests extends AbstractFunctionalTest
    {

    @BeforeClass
    public static void _startup()
        {
        System.setProperty("test.log.level", "9");
        System.setProperty("test.log", "log4j2");

        System.setProperty("coherence.override", "logging-coherence-override.xml");
        System.setProperty("coherence.cacheconfig", "logging-cache-config.xml");

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config  = context.getConfiguration();

        PatternLayout layout = PatternLayout.newBuilder()
                .withPattern("log4j2: %msg%n").build();

        WriterAppender writerAppender = WriterAppender.newBuilder().setName("writeAppender")
                .setTarget(stringWriter).setLayout(layout).build();
        writerAppender.start();
        config.addAppender(writerAppender);

        AppenderRef ref = AppenderRef.createAppenderRef("writeAppender", null, null);

        LoggerConfig loggerConfig = config.getLoggerConfig("");

        loggerConfig.addAppender(writerAppender, null, null);
        context.updateLoggers();

        AbstractFunctionalTest._startup();
        }

    // ----- test methods ---------------------------------------------------

    /**
     * Ensure
     *  1. messages are filtered based on destination log level.
     *  2. messages are logged based on the destination log level.
     */
    @Test
    public void testLogLevel()
        {
        String sMessage_info   = "This is a INFO message";
        String sMessage_finest = "This is a TRACE message";
        String sMessage_change = "Change logging level from INFO to TRACE";

        // The log level for Log4j logging is INFO,
        // it should override default coherence log level (FINE).
        assertFalse(Logger.isEnabled(Logger.FINE));

        // FINEST level message should not be logged
        Logger.finest(sMessage_finest);

        // INFO level message is logged
        Logger.info(sMessage_info);

        // wait for the logger to wake
        Eventually.assertDeferred(() -> isLogged(sMessage_info), is(true));
        assertFalse(isLogged(sMessage_finest));

        Logger.info(sMessage_change);
        Logger.setLoggingLevel(Logger.FINEST);
        assertTrue(Logger.isEnabled(Logger.FINE));

        // FINEST level message should now be logged
        Logger.finest(sMessage_finest);
        Eventually.assertDeferred(() -> isLogged(sMessage_finest), is(true));
        }

    /**
     * Ensure "Started cluster" is in the log.
     */
    @Test
    public void testStartedCluster()
        {
        Eventually.assertDeferred(() -> isLogged("log4j2: Started cluster"), is(true));
        }

    @AfterClass
    public static void _shutdown()
        {
        System.clearProperty("test.log.level");
        System.clearProperty("test.log");
        System.clearProperty("coherence.override");
        System.clearProperty("coherence.cacheconfig");
        }

    /**
     * Helper method to check if a given string is logged.
     */
    public boolean isLogged(String sMsg)
        {
        return stringWriter.getBuffer().indexOf(sMsg) != -1;
        }

    // ----- data members ---------------------------------------------------

    /**
     * A StringWriter stores all the log string.
     */
    private static StringWriter stringWriter = new StringWriter();
    }
