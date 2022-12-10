/*
 * Copyright (c) 2000, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.docker;

import com.oracle.bedrock.options.Diagnostics;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.ApplicationConsole;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.console.NullApplicationConsole;
import com.oracle.bedrock.runtime.options.Argument;
import org.junit.jupiter.api.Assumptions;

/**
 * Test Docker image names utils.
 *
 * @author Jonathan Knight 2022.08.01
 */
public class ImageNames
    {
    /**
     * Verify the assumptions needed to run tests.
     */
    public static void verifyTestAssumptions()
        {
        String[] asImageName = getImageNames();
        Assumptions.assumeTrue(asImageName.length  > 0, "Skipping test, "
                + PROP_IMAGES + " property not set");
        Assumptions.assumeTrue(imagesExist(), "Skipping test, one or more images not present: " + IMAGE_NAMES);
        }

    /**
     * Verify that the images being tested is already present.
     *
     * @return {@code true} if the images being tested is present.
     */
    public static boolean imagesExist()
        {
        if (s_fImagesExist == null)
            {
            String[] asImageName = getImageNames();
            if (asImageName.length == 0)
                {
                s_fImagesExist = false;
                }
            for (String sImage : asImageName)
                {
                if (!imageExists(sImage))
                    {
                    s_fImagesExist = false;
                    }
                }
            s_fImagesExist = true;
            }
        return s_fImagesExist;
        }

    /**
     * Return the names of the test images.
     *
     * @return the names of the test images
     */
    public static String[] getImageNames()
        {
        if (s_asImageName == null)
            {
            if (IMAGE_NAMES != null && !IMAGE_NAMES.isBlank())
                {
                s_asImageName = IMAGE_NAMES.split(",");
                }
            else
                {
                s_asImageName = new String[0];
                }
            }
        return s_asImageName;
        }

    /**
     * Verify the assumptions needed to run Graal tests.
     */
    public static void verifyGraalTestAssumptions()
        {
        String[] asImageName = getGraalImageNames();
        Assumptions.assumeTrue(asImageName.length  > 0, "Skipping test, "
                + PROP_GRAAL_IMAGES + " property not set");
        Assumptions.assumeTrue(graalImagesExist(), "Skipping test, one or more images not present: " + PROP_GRAAL_IMAGES);
        }

    /**
     * Verify that the Graal images being tested is already present.
     *
     * @return {@code true} if the Graal images being tested is present.
     */
    public static boolean graalImagesExist()
        {
        if (s_fGraalImagesExist == null)
            {
            String[] asImageName = getGraalImageNames();
            if (asImageName.length == 0)
                {
                s_fGraalImagesExist = false;
                }
            for (String sImage : asImageName)
                {
                if (!imageExists(sImage))
                    {
                    s_fGraalImagesExist = false;
                    }
                }
            s_fGraalImagesExist = true;
            }
        return s_fGraalImagesExist;
        }

    /**
     * Return the names of the Graal test images.
     *
     * @return the names of the Graal test images
     */
    public static String[] getGraalImageNames()
        {
        if (s_asGraalImageName == null)
            {
            if (IMAGE_NAMES != null && !GRAAL_IMAGE_NAMES.isBlank())
                {
                s_asGraalImageName = GRAAL_IMAGE_NAMES.split(",");
                }
            else
                {
                s_asGraalImageName = new String[0];
                }
            }
        return s_asGraalImageName;
        }

    /**
     * Verify that the image being tested is already present.
     *
     * @param sImage  the name of the image
     *
     * @return {@code true} if the image being tested is present.
     */
    public static boolean imageExists(String sImage)
        {
        String sDocker = "docker.io/";
        if (sImage.startsWith(sDocker))
            {
            sImage = sImage.substring(sDocker.length());
            }

        Platform platform = LocalPlatform.get();

        try (Application app = platform.launch("docker",
                                               Argument.of("inspect"),
                                               Argument.of(sImage),
                                               NullApplicationConsole.builder()))
            {
            int exitCode = app.waitFor();
            return exitCode == 0;
            }
        }

    /**
     * The system property containing the image names.
     */
    public static final String PROP_IMAGES = "test.image.names";

    /**
     * The name of the Graal image.
     */
    public static final String PROP_GRAAL_IMAGES = "graal.image.names";

    /**
     * The names of the images to test, set by the {@link #PROP_IMAGES} System property.
     */
    private static final String IMAGE_NAMES = System.getProperty(PROP_IMAGES);

    private static String[] s_asImageName;

    private static Boolean s_fImagesExist;

    /**
     * The names of the Graal images to test, set by the {@link #PROP_GRAAL_IMAGES} System property.
     */
    private static final String GRAAL_IMAGE_NAMES = System.getProperty(PROP_GRAAL_IMAGES);

    private static String[] s_asGraalImageName;

    private static Boolean s_fGraalImagesExist;
    }
