/*
 * Copyright (c) 2000, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package processor;


import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;

import com.tangosol.util.InvocableMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;


/**
* A collection of functional tests for the various
* {@link InvocableMap.EntryProcessor} implementations that use the
* "near-pool-test2" cache and two cache servers.
*
* @author jh  2005.12.21
*
* @see InvocableMap
*/
public class NearPoolMultiEntryProcessorTests
        extends AbstractDistEntryProcessorTests
    {
    // ----- constructors ---------------------------------------------------

    /**
    * Default constructor.
    */
    public NearPoolMultiEntryProcessorTests()
        {
        super("near-pool-test2");
        }


    // ----- test lifecycle -------------------------------------------------

    /**
    * Initialize the test class.
    */
    @BeforeClass
    public static void startup()
        {
        CoherenceClusterMember member1 = startCacheServer("NearPoolMultiProcessorTests-1", "processor");
        CoherenceClusterMember member2 = startCacheServer("NearPoolMultiProcessorTests-2", "processor");
        m_listClusterMembers.add(member1);
        m_listClusterMembers.add(member2);
        }

    /**
    * Shutdown the test class.
    */
    @AfterClass
    public static void shutdown()
        {
        stopCacheServer("NearPoolMultiProcessorTests-1");
        stopCacheServer("NearPoolMultiProcessorTests-2");
        }
    }
