/*
 * Copyright (c) 2000, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package cdi;

import com.oracle.coherence.cdi.CoherenceExtension;
import com.oracle.coherence.cdi.ConfigUri;
import com.oracle.coherence.cdi.Name;
import com.oracle.coherence.cdi.NamedTopicProducer;
import com.oracle.coherence.cdi.Scope;
import com.oracle.coherence.cdi.SessionInitializer;
import com.oracle.coherence.cdi.SessionName;
import com.tangosol.net.topic.NamedTopic;
import com.tangosol.net.topic.Publisher;
import com.tangosol.net.topic.Subscriber;

import java.lang.annotation.Annotation;

import java.util.List;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.inject.Instance;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.hamcrest.Matchers;

import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration test for the {@link NamedTopicProducer} using the Weld JUnit
 * extension.
 *
 * @author Jonathan Knight  2019.10.23
 */
@ExtendWith(WeldJunit5Extension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NamedTopicProducerIT
    {
    @WeldSetup
    private final WeldInitiator weld = WeldInitiator.of(WeldInitiator.createWeld()
                                                              .addPackages(CoherenceExtension.class)
                                                              .addExtension(new CoherenceExtension())
                                                              .addBeanClass(TestServerCoherenceProducer.class)
                                                              .addBeanClass(SessionOne.class)
                                                              .addBeanClass(CtorBean.class)
                                                              .addBeanClass(DifferentSessionBean.class)
                                                              .addBeanClass(NamedTopicFieldsBean.class)
                                                              .addBeanClass(NamedTopicPublisherFieldsBean.class)
                                                              .addBeanClass(NamedTopicSubscriberFieldsBean.class)
                                                              .addBeanClass(RequestScopedPublishers.class)
                                                              .addBeanClass(RequestScopedSubscribers.class));

    @Inject
    private RequestContextController contextController;

    @Test
    @SuppressWarnings("rawtypes")
    void shouldGetDynamicNamedTopic()
        {
        Annotation           qualifier = Name.Literal.of("numbers");
        Instance<NamedTopic> instance  = weld.select(NamedTopic.class, qualifier);

        assertThat(instance.isResolvable(), is(true));
        assertThat(instance.get().getName(), is("numbers"));
        }

    @Test
    void shouldInjectNamedTopicUsingFieldName()
        {
        NamedTopicFieldsBean bean = weld.select(NamedTopicFieldsBean.class).get();
        assertThat(bean.getNumbers(), is(notNullValue()));
        assertThat(bean.getNumbers().getName(), is("numbers"));
        }

    @Test
    void shouldInjectQualifiedNamedTopic()
        {
        NamedTopicFieldsBean bean = weld.select(NamedTopicFieldsBean.class).get();
        assertThat(bean.getNamedTopic(), is(notNullValue()));
        assertThat(bean.getNamedTopic().getName(), is("numbers"));
        }

    @Test
    void shouldInjectNamedTopicWithGenerics()
        {
        NamedTopicFieldsBean bean = weld.select(NamedTopicFieldsBean.class).get();
        assertThat(bean.getGenericTopic(), is(notNullValue()));
        assertThat(bean.getGenericTopic().getName(), is("numbers"));
        }

    @Test
    void shouldInjectNamedTopicWithGenericValues()
        {
        NamedTopicFieldsBean bean = weld.select(NamedTopicFieldsBean.class).get();
        assertThat(bean.getGenericValues(), is(notNullValue()));
        assertThat(bean.getGenericValues().getName(), is("genericValues"));
        }

    @Test
    void shouldInjectTopicsFromDifferentSessions() throws Exception
        {
        DifferentSessionBean bean = weld.select(DifferentSessionBean.class).get();

        NamedTopic<String> defaultTopic = bean.getDefaultCcfNumbers();
        NamedTopic<String> specificTopic = bean.getSpecificCcfNumbers();

        assertThat(defaultTopic, is(notNullValue()));
        assertThat(defaultTopic.getName(), Matchers.is("numbers"));

        assertThat(specificTopic, is(notNullValue()));
        assertThat(specificTopic.getName(), Matchers.is("numbers"));

        assertThat(defaultTopic, is(not(sameInstance(specificTopic))));

        Subscriber<String> defaultSubscriber = bean.getDefaultSubscriber();
        CompletableFuture<Subscriber.Element<String>> defaultFuture = defaultSubscriber.receive();
        Subscriber<String> specificSubscriber = bean.getSpecificSubscriber();
        CompletableFuture<Subscriber.Element<String>> specificFuture = specificSubscriber.receive();

        bean.getDefaultPublisher().publish("value-one");
        bean.getSpecificPublisher().publish("value-two");

        Subscriber.Element<String> valueOne = defaultFuture.get(1, TimeUnit.MINUTES);
        Subscriber.Element<String> valueTwo = specificFuture.get(1, TimeUnit.MINUTES);

        assertThat(valueOne.getValue(), is("value-one"));
        assertThat(valueTwo.getValue(), is("value-two"));
        }

    @Test
    void testCtorInjection()
        {
        CtorBean bean = weld.select(CtorBean.class).get();

        assertThat(bean.getNumbers(), Matchers.notNullValue());
        assertThat(bean.getNumbers().getName(), Matchers.is("numbers"));
        }

    @Test
    void shouldCloseSubscriberOnScopeDeactivation()
        {
        // Activate RequestScope
        boolean activated = contextController.activate();
        assertThat(activated, is(true));

        // Obtain a RequestScoped bean that consequently has a RequestScoped Subscriber
        RequestScopedSubscribers bean = weld.select(RequestScopedSubscribers.class).get();
        Subscriber<String> subscriber = bean.getSubscriber();

        assertThat(subscriber.isActive(), is(true));

        // Deactivate the RequestScope
        contextController.deactivate();

        assertThat(subscriber.isActive(), is(false));
        }

    @Test
    void shouldCloseQualifiedSubscriberOnScopeDeactivation()
        {
        // Activate RequestScope
        boolean activated = contextController.activate();
        assertThat(activated, is(true));

        // Obtain a RequestScoped bean that consequently has a RequestScoped Subscriber
        RequestScopedSubscribers bean = weld.select(RequestScopedSubscribers.class).get();
        Subscriber<String> subscriber = bean.getQualifiedSubscriber();

        assertThat(subscriber.isActive(), is(true));

        // Deactivate the RequestScope
        contextController.deactivate();

        assertThat(subscriber.isActive(), is(false));
        }

    @Test
    void shouldClosePublisherOnScopeDeactivation()
        {
        // Activate RequestScope
        boolean activated = contextController.activate();
        assertThat(activated, is(true));

        // Obtain a RequestScoped bean that consequently has a RequestScoped Publisher
        RequestScopedPublishers bean = weld.select(RequestScopedPublishers.class).get();
        AtomicBoolean isClosed = new AtomicBoolean(false);
        Publisher<String> publisher = bean.getPublisher();

        publisher.onClose(() -> isClosed.set(true));

        // Deactivate the RequestScope
        contextController.deactivate();

        assertThat(isClosed.get(), is(true));
        }

    @Test
    void shouldCloseQualifiedPublisherOnScopeDeactivation()
        {
        // Activate RequestScope
        boolean activated = contextController.activate();
        assertThat(activated, is(true));

        // Obtain a RequestScoped bean that consequently has a RequestScoped Publisher
        RequestScopedPublishers bean = weld.select(RequestScopedPublishers.class).get();
        AtomicBoolean isClosed = new AtomicBoolean(false);
        Publisher<String> publisher = bean.getQualifiedPublisher();

        publisher.onClose(() -> isClosed.set(true));

        // Deactivate the RequestScope
        contextController.deactivate();

        assertThat(isClosed.get(), is(true));
        }

    // ----- test beans -----------------------------------------------------

    @ApplicationScoped
    @Named("test-session")
    @Scope("Test")
    @ConfigUri("test-config.xml")
    private static class SessionOne
            implements SessionInitializer
        {
        }

    @RequestScoped
    private static class RequestScopedSubscribers
        {
        @Inject
        private Subscriber<String> numbers;

        @Inject
        @Name("numbers")
        private Subscriber<String> qualifiedSubscriber;

        Subscriber<String> getSubscriber()
            {
            return numbers;
            }

        Subscriber<String> getQualifiedSubscriber()
            {
            return qualifiedSubscriber;
            }
        }

    @RequestScoped
    private static class RequestScopedPublishers
        {
        @Inject
        private Publisher<String> numbers;

        @Inject
        @Name("numbers")
        private Publisher<String> qualifiedPublisher;

        Publisher<String> getPublisher()
            {
            return numbers;
            }

        Publisher<String> getQualifiedPublisher()
            {
            return qualifiedPublisher;
            }
        }

    @ApplicationScoped
    @SuppressWarnings("rawtypes")
    private static class NamedTopicFieldsBean
        {
        @Inject
        private NamedTopic numbers;

        @Inject
        @Name("numbers")
        private NamedTopic namedTopic;

        @Inject
        @Name("numbers")
        private NamedTopic<Integer> genericTopic;

        @Inject
        private NamedTopic<List<String>> genericValues;

        public NamedTopic getNumbers()
            {
            return numbers;
            }

        public NamedTopic getNamedTopic()
            {
            return namedTopic;
            }

        public NamedTopic<Integer> getGenericTopic()
            {
            return genericTopic;
            }

        public NamedTopic<List<String>> getGenericValues()
            {
            return genericValues;
            }
        }

    @ApplicationScoped
    @SuppressWarnings("rawtypes")
    private static class NamedTopicPublisherFieldsBean
        {
        @Inject
        private Publisher numbers;

        @Inject
        @Name("numbers")
        private Publisher namedTopic;

        @Inject
        @Name("numbers")
        private Publisher<Integer> genericTopic;

        @Inject
        private Publisher<List<String>> genericValues;

        public Publisher getNumbers()
            {
            return numbers;
            }

        public Publisher getPublisher()
            {
            return namedTopic;
            }

        public Publisher<Integer> getGenericPublisher()
            {
            return genericTopic;
            }

        public Publisher<List<String>> getGenericValuesPublisher()
            {
            return genericValues;
            }
        }

    @ApplicationScoped
    @SuppressWarnings("rawtypes")
    private static class NamedTopicSubscriberFieldsBean
        {
        @Inject
        private Subscriber numbers;

        @Inject
        @Name("numbers")
        private Subscriber namedTopic;

        @Inject
        @Name("numbers")
        private Subscriber<Integer> genericTopic;

        @Inject
        private Subscriber<List<String>> genericValues;

        public Subscriber getNumbers()
            {
            return numbers;
            }

        public Subscriber getSubscriber()
            {
            return namedTopic;
            }

        public Subscriber<Integer> getGenericSubscriber()
            {
            return genericTopic;
            }

        public Subscriber<List<String>> getGenericValuesSubscriber()
            {
            return genericValues;
            }
        }

    @ApplicationScoped
    private static class DifferentSessionBean
        {
        @Inject
        @Name("numbers")
        private NamedTopic<String> defaultCcfNumbers;

        @Inject
        @Name("numbers")
        private Publisher<String> defaultPublisher;

        @Inject
        @Name("numbers")
        private Subscriber<String> defaultSubscriber;

        @Inject
        @Name("numbers")
        @SessionName("test-session")
        private NamedTopic<String> specificCcfNumbers;

        @Inject
        @Name("numbers")
        @SessionName("test-session")
        private Publisher<String> specificPublisher;

        @Inject
        @Name("numbers")
        @SessionName("test-session")
        private Subscriber<String> specificSubscriber;

        public NamedTopic<String> getDefaultCcfNumbers()
            {
            return defaultCcfNumbers;
            }

        public NamedTopic<String> getSpecificCcfNumbers()
            {
            return specificCcfNumbers;
            }

        public Publisher<String> getDefaultPublisher()
            {
            return defaultPublisher;
            }

        public Subscriber<String> getDefaultSubscriber()
            {
            return defaultSubscriber;
            }

        public Publisher<String> getSpecificPublisher()
            {
            return specificPublisher;
            }

        public Subscriber<String> getSpecificSubscriber()
            {
            return specificSubscriber;
            }
        }

    @ApplicationScoped
    private static class CtorBean
        {
        private final NamedTopic<Integer> numbers;

        @Inject
        CtorBean(@Name("numbers") NamedTopic<Integer> topic)
            {
            this.numbers = topic;
            }

        NamedTopic<Integer> getNumbers()
            {
            return numbers;
            }
        }
    }
