/*
 * Copyright (c) 2020, 2022 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.cdi;

import com.tangosol.net.Coherence;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.Session;

import com.tangosol.net.topic.NamedTopic;
import com.tangosol.net.topic.Publisher;
import com.tangosol.net.topic.Subscriber;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;

import jakarta.inject.Inject;

/**
 * A CDI producer for {@link com.tangosol.net.topic.NamedTopic}, {@link
 * com.tangosol.net.topic.Publisher} and {@link com.tangosol.net.topic.Subscriber}
 * instances.
 *
 * @author Jonathan Knight  2019.10.23
 * @since 20.06
 */
@ApplicationScoped
public class NamedTopicProducer
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create the {2link NamedTopicProducer}.
     *
     * @param beanManager  the CDI bean manager
     */
    @Inject
    public NamedTopicProducer(BeanManager beanManager)
        {
        f_beanManager = beanManager;
        }

    // ---- producer methods ------------------------------------------------

    /**
     * Produce a {@link NamedTopic} using the injection point member name for
     * the topic name and the default {@link ConfigurableCacheFactory} as the
     * source.
     *
     * @param injectionPoint  the injection point to inject the {@link NamedTopic} into
     * @param <V>             the type of the topic's elements
     *
     * @return an {@link NamedTopic} using the injection point member name for
     *         the topic name and the default {@link ConfigurableCacheFactory}
     *         as the source
     */
    @Produces
    <V> NamedTopic<V> getNonQualifiedNamedTopic(InjectionPoint injectionPoint)
        {
        return getNamedTopic(injectionPoint);
        }

    /**
     * Produce a {@link NamedTopic} using the name from the {@link Name @Name}
     * qualifier as the topic name and the name from the optional {@link
     * SessionName @SessionName} qualifier to identify the source {@link
     * ConfigurableCacheFactory}.
     * <p>
     * If no {@link SessionName @SessionName} qualifier is present the default {@link
     * ConfigurableCacheFactory} will be used as the source.
     *
     * @param injectionPoint  the injection point to inject the {@link NamedTopic} into
     * @param <V>             the type of the topic's elements
     *
     * @return a {@link NamedTopic} using the name from the {@link Name @Name}
     *         qualifier as the topic name and the name from the optional
     *         {@link SessionName @SessionName} qualifier
     */
    @Produces
    @Name("")
    @SessionName("")
    <V> NamedTopic<V> getNamedTopic(InjectionPoint injectionPoint)
        {
        String  sName        = null;
        String  sSession     = Coherence.DEFAULT_NAME;

        for (Annotation annotation : injectionPoint.getQualifiers())
            {
            if (Name.class.equals(annotation.annotationType()))
                {
                sName = ((Name) annotation).value();
                }
            else if (SessionName.class.equals(annotation.annotationType()))
                {
                sSession = ((SessionName) annotation).value();
                }
             }

        if (sName == null)
            {
            sName = injectionPoint.getMember().getName();
            }

        String  sSessionName = sSession;
        Session session      = f_beanManager.createInstance()
                                            .select(Session.class, Name.Literal.of(sSessionName))
                                            .get();

        return session.getTopic(sName);
        }

    /**
     * Produce a {@link Publisher} for a {@link NamedTopic} the injection point
     * member name for the topic name and the default {@link
     * ConfigurableCacheFactory} as the source.
     *
     * @param injectionPoint  the injection point to inject the {@link NamedTopic} into
     * @param <V>             the type of the topic's elements
     *
     * @return a {@link Publisher} for a {@link NamedTopic} using the injection
     *         point member name for the topic name and the default
     *         {@link ConfigurableCacheFactory} as the source
     */
    @Produces
    <V> Publisher<V> getNonQualifiedNamedTopicPublisher(InjectionPoint injectionPoint)
        {
        return getNamedTopicPublisher(injectionPoint);
        }

    /**
     * Produce a {@link Publisher} for a {@link NamedTopic} using the name from
     * the {@link Name @Name} qualifier as the topic name and the name from the
     * optional {@link SessionName @SessionName} qualifier to identify the source {@link
     * ConfigurableCacheFactory}.
     * <p>
     * If no {@link SessionName @SessionName} qualifier is present the default {@link
     * ConfigurableCacheFactory} will be used as the source.
     *
     * @param injectionPoint  the injection point to inject the {@link NamedTopic} into
     * @param <V>             the type of the topic's elements
     *
     * @return a {@link NamedTopic} using the name from the {@link Name @Name}
     *         qualifier as the topic name and the name from the optional
     *         {@link SessionName @SessionName} qualifier
     */
    @Produces
    @Name("")
    @SessionName("")
    <V> Publisher<V> getNamedTopicPublisher(InjectionPoint injectionPoint)
        {
        NamedTopic<V> topic = getNamedTopic(injectionPoint);
        return topic.createPublisher();
        }

    /**
     * Dispose of a {@link Publisher} bean.
     * <p>
     * Disposing of a {@link Publisher} will call {@link Publisher#close()}.
     *
     * @param publisher  the {@link Publisher} to dispose
     * @param <V>        the type of the topic elements
     */
    <V> void closePublisher(@Disposes Publisher<V> publisher)
        {
        publisher.close();
        }

    /**
     * Dispose of a {@link Publisher} bean.
     * <p>
     * Disposing of a {@link Publisher} will call {@link Publisher#close()}.
     *
     * @param publisher  the {@link Publisher} to dispose
     * @param <V>        the type of the topic elements
     */
    <V> void closeQualifiedPublisher(@Disposes @Name("") @SessionName("") Publisher<V> publisher)
        {
        publisher.close();
        }

    /**
     * Produce a {@link Subscriber} for a {@link NamedTopic} the injection point
     * member name for the topic name and the default {@link
     * ConfigurableCacheFactory} as the source.
     * <p>
     * The {@link Subscriber} produced will not be part of any subscriber
     * group.
     *
     * @param injectionPoint  the injection point to inject the {@link NamedTopic} into
     * @param <V>             the type of the topic's elements
     *
     * @return a {@link Subscriber} for a {@link NamedTopic} using the injection
     *         point member name for the topic name and the default
     *         {@link ConfigurableCacheFactory} as the source
     */
    @Produces
    <V> Subscriber<V> getNonQualifiedNamedTopicSubscriber(InjectionPoint injectionPoint)
        {
        return getNamedTopicSubscriber(injectionPoint);
        }

    /**
     * Produce a {@link Subscriber} to a {@link NamedTopic}.
     * <p>
     * The value from the {@link Name @Name} qualifier, if present, will be used as
     * the topic name. If no  {@link Name @Name} qualifier is present then the
     * injection point member name will be used as the topic name.
     * <p>
     * The name from the optional {@link SessionName @SessionName} qualifier will be used to
     * determine the source {@link ConfigurableCacheFactory}. If no {@link
     * SessionName} qualifier is present the default {@link
     * ConfigurableCacheFactory} will be used as the source.
     * <p>
     * The optional {@link SubscriberGroup} qualifier can be used to specify a
     * subscriber group that the {@link Subscriber} should belong to.
     *
     * @param injectionPoint  the injection point to inject the {@link NamedTopic} into
     * @param <V>             the type of the topic's elements
     *
     * @return a {@link NamedTopic} using the name from the {@link Name @Name}
     *         qualifier as the topic name and the name from the optional
     *         {@link SessionName @SessionName} qualifier
     */
    @Produces
    @Name("")
    @SessionName("")
    @SuppressWarnings("unchecked")
    <V> Subscriber<V> getNamedTopicSubscriber(InjectionPoint injectionPoint)
        {
        String sGroup = injectionPoint.getQualifiers()
                .stream()
                .filter(q -> SubscriberGroup.class.isAssignableFrom(q.getClass()))
                .map(q -> ((SubscriberGroup) q).value())
                .findFirst()
                .orElse(null);

        NamedTopic<V> topic = getNamedTopic(injectionPoint);
        return sGroup == null
               ? topic.createSubscriber()
               : topic.createSubscriber(Subscriber.Name.of(sGroup));
        }

    /**
     * Dispose of a {@link Subscriber} bean.
     * <p>
     * Disposing of a {@link Subscriber} will call {@link Subscriber#close()}.
     *
     * @param subscriber  the {@link Subscriber} to dispose
     * @param <V>         the type of the topic elements
     */
    <V> void closeSubscriber(@Disposes Subscriber<V> subscriber)
        {
        subscriber.close();
        }

    /**
     * Dispose of a {@link Subscriber} bean.
     * <p>
     * Disposing of a {@link Subscriber} will call {@link Subscriber#close()}.
     *
     * @param subscriber  the {@link Subscriber} to dispose
     * @param <V>         the type of the topic elements
     */
    <V> void closeQualifiedSubscriber(@Disposes @Name("") @SessionName("") Subscriber<V> subscriber)
        {
        subscriber.close();
        }

    // ----- data members ---------------------------------------------------

    /**
     * The CDI bean manager.
     */
    private final BeanManager f_beanManager;
    }
