/*
 * Copyright (c) 2000, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.cdi;

import com.tangosol.net.Coherence;

import jakarta.enterprise.util.Nonbinding;

import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A qualifier annotation used when injecting Coherence resource to indicate
 * that those resource should be obtained from a specific {@link
 * com.tangosol.net.ConfigurableCacheFactory}.
 *
 * @author Jonathan Knight  2019.10.20
 * @since 20.06
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Scope
    {
    /**
     * The scope name or URI used to identify a specific {@link
     * com.tangosol.net.ConfigurableCacheFactory}.
     *
     * @return the scope name or URI used to identify a specific
     *         {@link com.tangosol.net.ConfigurableCacheFactory}
     */
    @Nonbinding String value() default DEFAULT;

    /**
     * Predefined constant for system scope.
     */
    String DEFAULT = Coherence.DEFAULT_SCOPE;

    /**
     * Predefined constant for system scope.
     */
    String SYSTEM = Coherence.SYSTEM_SCOPE;

    // ---- inner class: Literal --------------------------------------------

    /**
     * An annotation literal for the {@link Scope} annotation.
     */
    class Literal
            extends AnnotationLiteral<Scope>
            implements Scope
        {
        /**
         * Construct {@code Literal} instacne.
         *
         * @param sValue  the scope name or URI used to identify a specific
         *                {@link com.tangosol.net.ConfigurableCacheFactory}
         */
        private Literal(String sValue)
            {
            m_sValue = sValue == null ? Scope.DEFAULT : sValue;
            }

        /**
         * Create a {@link Scope.Literal}.
         *
         * @param sValue  the scope name or URI used to identify a specific
         *                {@link com.tangosol.net.ConfigurableCacheFactory}
         *
         * @return a {@link Scope.Literal} with the specified URI
         */
        public static Literal of(String sValue)
            {
            return new Literal(sValue);
            }

        /**
         * Obtain the name value.
         *
         * @return the name value
         */
        public String value()
            {
            return m_sValue;
            }

        // ---- data members ------------------------------------------------

        /**
         * The value for this literal.
         */
        private final String m_sValue;
        }
    }
