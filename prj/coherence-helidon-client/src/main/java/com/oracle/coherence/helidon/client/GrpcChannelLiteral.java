/*
 * Copyright (c) 2000, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.helidon.client;

import io.helidon.microprofile.grpc.client.GrpcChannel;

import java.lang.annotation.Annotation;

/**
 * A {@link GrpcChannel} annotation literal.
 *
 * @author Jonathan Knight  2019.11.28
 * @since 20.06
 */
public class GrpcChannelLiteral
    {
    /**
     * Create a {@link GrpcChannel} annotation literal.
     *
     * @param sName  the channel name
     *
     * @return {@link GrpcChannel} annotation literal
     */
    public static Annotation of(String sName)
        {
        Class<GrpcChannel> type = GrpcChannel.class;
        return new GrpcChannel()
            {
            @Override
            public Class<GrpcChannel> annotationType()
                {
                return type;
                }

            @Override
            public String name()
                {
                return sName;
                }
            };
        }
    }
