/*
 * Copyright (c) 2000, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package cdi;

import com.oracle.coherence.inject.Injectable;
import cdi.data.Account;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.InvocableMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;

import jakarta.inject.Inject;

import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration test for the {@link Injectable} using the Weld JUnit
 * extension.
 *
 * @author Aleks Seovic  2020.04.03
*/
@ExtendWith(WeldJunit5Extension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InjectableIT
    {
    @WeldSetup
    private final WeldInitiator weld = WeldInitiator.of(WeldInitiator.createWeld()
                                                          .addBeanClass(CurrencyConverter.class)
                                                          .addBeanClass(TestObservers.class));

    @Test
    void testNsfWithdrawal()
        {
        ConfigurableCacheFactory ccf = CacheFactory.getCacheFactoryBuilder()
                .getConfigurableCacheFactory("injectable-config.xml", null);
        ccf.activate();

        NamedCache<String, Account> accounts = ccf.ensureCache("accounts", null);
        accounts.put("X", new Account("X", 5000L));

        accounts.invoke("X", new CreditAccount(100L));
        assertThat(accounts.get("X").getBalance(), is(-5000L));
        }

    // ---- helper classes --------------------------------------------------

    @Dependent
    public static class CreditAccount
            implements InvocableMap.EntryProcessor<String, Account, Long>, Injectable
        {
        private long amount;

        @Inject
        private CurrencyConverter currencyConverter;

        public CreditAccount()
            {
            }

        public CreditAccount(long amount)
            {
            this.amount = amount;
            }

        @Override
        public Long process(InvocableMap.Entry<String, Account> entry)
            {
            Account account = entry.getValue();
            account.withdraw(currencyConverter.convert(amount));
            entry.setValue(account);
            return account.getBalance();
            }
        }

    @ApplicationScoped
    public static class CurrencyConverter
        {
        public long convert(long amount)
            {
            return amount * 100;
            }
        }

    @ApplicationScoped
    public static class TestObservers
        {
        private void onAccountOverdrawn(@Observes Account.Overdrawn event)
            {
            System.out.println(event);
            assertThat(event.getAmount(), is(5000L));
            }
        }
    }
