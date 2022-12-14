/*
 * Copyright (c) 2000, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.tangosol.internal.net.topic.impl.paged.management;

import com.tangosol.internal.net.management.model.AbstractModel;
import com.tangosol.internal.net.management.model.ModelAttribute;
import com.tangosol.internal.net.management.model.SimpleModelAttribute;

import com.tangosol.internal.net.topic.impl.paged.PagedTopic;
import com.tangosol.internal.net.topic.impl.paged.PagedTopicBackingMapManager;

import com.tangosol.internal.net.topic.impl.paged.statistics.PagedTopicStatistics;

import com.tangosol.util.LongArray;
import com.tangosol.util.SimpleLongArray;

import javax.management.DynamicMBean;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An MBean model for a {@link PagedTopic}
 *
 * @author Jonathan Knight 2022.09.10
 * @since 23.03
 */
public class PagedTopicModel
        extends AbstractModel<PagedTopicModel>
        implements DynamicMBean, PublishedMetrics
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Create a {@link PagedTopicModel}.
     *
     * @param pagedTopic  the topic this model represents
     */
    public PagedTopicModel(PagedTopic<?> pagedTopic)
        {
        super(MBEAN_DESCRIPTION);
        f_pagedTopic = pagedTopic;
        f_statistics = ((PagedTopicBackingMapManager) pagedTopic.getCacheService().getBackingMapManager())
                .getStatistics(pagedTopic.getName());

        // create the array of channel models
        int cChannel = pagedTopic.getChannelCount();
        for (int nChannel = 0; nChannel < cChannel; nChannel++)
            {
            f_aChannel.set(nChannel, new PagedTopicChannelModel(pagedTopic, nChannel));
            }

        // configure the attributes of the MBean (ordering does not matter)
        addAttribute(ATTRIBUTE_CHANNEL_COUNT);
        addAttribute(ATTRIBUTE_PAGE_CAPACITY);
        addAttribute(ATTRIBUTE_RETAIN_CONSUMED);
        addAttribute(ATTRIBUTE_ALLOW_UNOWNED_COMMITS);
        addAttribute(ATTRIBUTE_SUBSCRIBER_TIMEOUT);
        addAttribute(ATTRIBUTE_ELEMENT_CALCULATOR);
        addAttribute(ATTRIBUTE_RECONNECT_WAIT);
        addAttribute(ATTRIBUTE_RECONNECT_TIMEOUT);
        addAttribute(ATTRIBUTE_RECONNECT_RETRY);
        addAttribute(ATTRIBUTE_PUBLISHED_COUNT);
        addAttribute(ATTRIBUTE_PUBLISHED_MEAN);
        addAttribute(ATTRIBUTE_PUBLISHED_ONE_MINUTE);
        addAttribute(ATTRIBUTE_PUBLISHED_FIVE_MINUTE);
        addAttribute(ATTRIBUTE_PUBLISHED_FIFTEEN_MINUTE);
        addAttribute(ATTRIBUTE_CHANNEL_TABLE);
        }

    // ----- PagedTopicModel methods ----------------------------------------

    /**
     * Return the channel count for the topic.
     *
     * @return the channel count for the topic
     */
    protected int getChannelCount()
        {
        return f_pagedTopic.getChannelCount();
        }

    /**
     * Return the capacity of a page in the topic.
     *
     * @return the capacity of a page in the topic
     */
    public int getPageCapacity()
        {
        return f_pagedTopic.getDependencies().getPageCapacity();
        }

    /**
     * Returns {@code true} if this topic retains messages after they have been committed
     * or {@code false} if messages are removed after all known subscribers have committed
     * them.
     *
     * @return {@code true} if this topic retains messages after they have been committed
     *         or {@code false} if messages are removed after all known subscribers have
     *         committed them
     */
    public boolean isRetainConsumed()
        {
        return f_pagedTopic.getDependencies().isRetainConsumed();
        }

    /**
     * Returns {@code true} if the topic allows commits of a position in a channel to be
     * made by subscribers that do not own the channel.
     *
     * @return {@code true} if the topic allows commits of a position in a channel to be
     *         made by subscribers that do not own the channel
     */
    public boolean isAllowUnownedCommits()
        {
        return f_pagedTopic.getDependencies().isAllowUnownedCommits();
        }

    /**
     * Returns number of milliseconds within which a subscriber must issue a
     * heartbeat or be forcefully considered closed.
     *
     * @return number of milliseconds within which a subscriber must issue a
     *         heartbeat
     */
    public long getSubscriberTimeout()
        {
        return f_pagedTopic.getDependencies().getSubscriberTimeoutMillis();
        }

    /**
     * Return the calculator name used to calculate element sizes.
     *
     * @return the calculator name used to calculate element sizes
     */
    public String getElementCalculator()
        {
        return f_pagedTopic.getDependencies().getElementCalculator().getName();
        }

    /**
     * Return the amount of time publishers and subscribers will wait before attempting
     * to reconnect after being disconnected.
     *
     * @return the maximum amount of time publishers and subscribers will
     *         wait before attempting to reconnect after being disconnected
     */
    public long getReconnectWait()
        {
        return f_pagedTopic.getDependencies().getReconnectWaitMillis();
        }

    /**
     * Returns the maximum amount of time publishers and subscribers will
     * attempt to reconnect after being disconnected.
     *
     * @return the maximum amount of time publishers and subscribers will
     *         attempt to reconnect after being disconnected
     */
    public long getReconnectTimeout()
        {
        return f_pagedTopic.getDependencies().getReconnectTimeoutMillis();
        }

    /**
     * Return the amount of time publishers and subscribers will wait between
     * attempts to reconnect after being disconnected.
     *
     * @return the maximum amount of time publishers and subscribers will
     *         wait between attempts to reconnect after being disconnected
     */
    public long getReconnectRetry()
        {
        return f_pagedTopic.getDependencies().getReconnectRetryMillis();
        }

    /**
     * Return the {@link PagedTopicChannelModel} for a specific channel.
     * <p>
     * The channel parameter is a zero based index of channels and must be
     * greater than or equal to 0 and less than the channel count.
     *
     * @param nChannel  the channel to obtain the model for
     *
     * @return the {@link PagedTopicChannelModel} for the channel
     *
     * @throws IndexOutOfBoundsException if the channel parameter is less than zero
     *         or greater than or equal to the channel count
     */
    protected PagedTopicChannelModel getChannelModel(int nChannel)
        {
        PagedTopicChannelModel model = f_aChannel.get(nChannel);
        if (model == null)
            {
            f_lock.lock();
            try
                {
                model = f_aChannel.get(nChannel);
                if (model == null)
                    {
                    model = new PagedTopicChannelModel(f_pagedTopic, nChannel);
                    f_aChannel.set(nChannel, model);
                    }
                }
            finally
                {
                f_lock.unlock();
                }
            }
        return model;
        }

    // ----- PublishedMetrics methods ---------------------------------------

    @Override
    public long getPublishedCount()
        {
        return f_statistics.getPublishedCount();
        }

    @Override
    public double getPublishedFifteenMinuteRate()
        {
        return f_statistics.getPublishedFifteenMinuteRate();
        }

    @Override
    public double getPublishedFiveMinuteRate()
        {
        return f_statistics.getPublishedFiveMinuteRate();
        }

    @Override
    public double getPublishedOneMinuteRate()
        {
        return f_statistics.getPublishedOneMinuteRate();
        }

    @Override
    public double getPublishedMeanRate()
        {
        return f_statistics.getPublishedMeanRate();
        }

    // ----- constants ------------------------------------------------------

    /**
     * The MBean's description.
     */
    protected static final String MBEAN_DESCRIPTION = "A Coherence PagedTopic";

    /**
     * The channel count attribute.
     */
    protected static final ModelAttribute<PagedTopicModel> ATTRIBUTE_CHANNEL_COUNT =
            SimpleModelAttribute.intBuilder("ChannelCount", PagedTopicModel.class)
                    .withDescription("The number of channels in the topic")
                    .withFunction(PagedTopicModel::getChannelCount)
                    .metric(false)
                    .build();

    /**
     * The page capacity attribute.
     */
    protected static final ModelAttribute<PagedTopicModel> ATTRIBUTE_PAGE_CAPACITY =
            SimpleModelAttribute.intBuilder("PageCapacity", PagedTopicModel.class)
                    .withDescription("The capacity of a page")
                    .withFunction(PagedTopicModel::getPageCapacity)
                    .build();

    /**
     * The retain consumed attribute.
     */
    protected static final ModelAttribute<PagedTopicModel> ATTRIBUTE_RETAIN_CONSUMED =
            SimpleModelAttribute.intBuilder("RetainConsumed", PagedTopicModel.class)
                    .withDescription("Retain consumed values")
                    .withFunction(PagedTopicModel::isRetainConsumed)
                    .build();

    /**
     * The unowned commits attribute.
     */
    protected static final ModelAttribute<PagedTopicModel> ATTRIBUTE_ALLOW_UNOWNED_COMMITS =
            SimpleModelAttribute.intBuilder("AllowUnownedCommits", PagedTopicModel.class)
                    .withDescription("Allow Unowned Commits")
                    .withFunction(PagedTopicModel::isAllowUnownedCommits)
                    .build();

    /**
     * The subscriber timeout attribute.
     */
    protected static final ModelAttribute<PagedTopicModel> ATTRIBUTE_SUBSCRIBER_TIMEOUT =
            SimpleModelAttribute.intBuilder("SubscriberTimeout", PagedTopicModel.class)
                    .withDescription("Subscriber Timeout")
                    .withFunction(PagedTopicModel::getSubscriberTimeout)
                    .build();

    /**
     * The subscriber timeout attribute.
     */
    protected static final ModelAttribute<PagedTopicModel> ATTRIBUTE_ELEMENT_CALCULATOR =
            SimpleModelAttribute.intBuilder("ElementCalculator", PagedTopicModel.class)
                    .withDescription("Element Calculator")
                    .withFunction(PagedTopicModel::getElementCalculator)
                    .build();
    /**
     * The reconnect wait attribute.
     */
    protected static final ModelAttribute<PagedTopicModel> ATTRIBUTE_RECONNECT_WAIT =
            SimpleModelAttribute.intBuilder("ReconnectWait", PagedTopicModel.class)
                    .withDescription("Reconnect Wait")
                    .withFunction(PagedTopicModel::getReconnectWait)
                    .build();

    /**
     * The reconnect timeout attribute.
     */
    protected static final ModelAttribute<PagedTopicModel> ATTRIBUTE_RECONNECT_TIMEOUT =
            SimpleModelAttribute.intBuilder("ReconnectTimeout", PagedTopicModel.class)
                    .withDescription("Reconnect Timeout")
                    .withFunction(PagedTopicModel::getReconnectTimeout)
                    .build();

    /**
     * The reconnect retry attribute.
     */
    protected static final ModelAttribute<PagedTopicModel> ATTRIBUTE_RECONNECT_RETRY =
            SimpleModelAttribute.intBuilder("ReconnectRetry", PagedTopicModel.class)
                    .withDescription("Reconnect Retry")
                    .withFunction(PagedTopicModel::getReconnectRetry)
                    .build();

    /**
     * The published count attribute.
     */
    protected static final ModelAttribute<PagedTopicModel> ATTRIBUTE_PUBLISHED_COUNT =
            PublishedMetrics.ATTRIBUTE_COUNT.asBuilder(PagedTopicModel.class)
                    .withFunction(PagedTopicModel::getPublishedCount)
                    .build();

    /**
     * The published mean rate attribute.
     */
    protected static final ModelAttribute<PagedTopicModel> ATTRIBUTE_PUBLISHED_MEAN =
            PublishedMetrics.ATTRIBUTE_MEAN_RATE.asBuilder(PagedTopicModel.class)
                    .withFunction(PagedTopicModel::getPublishedMeanRate)
                    .build();

    /**
     * The published one-minute rate attribute.
     */
    protected static final ModelAttribute<PagedTopicModel> ATTRIBUTE_PUBLISHED_ONE_MINUTE =
            PublishedMetrics.ATTRIBUTE_ONE_MINUTE_RATE.asBuilder(PagedTopicModel.class)
                    .withFunction(PagedTopicModel::getPublishedOneMinuteRate)
                    .build();

    /**
     * The published five-minute rate attribute.
     */
    protected static final ModelAttribute<PagedTopicModel> ATTRIBUTE_PUBLISHED_FIVE_MINUTE =
            PublishedMetrics.ATTRIBUTE_FIVE_MINUTE_RATE.asBuilder(PagedTopicModel.class)
                    .withFunction(PagedTopicModel::getPublishedFiveMinuteRate)
                    .build();

    /**
     * The published fifteen-minute rate attribute.
     */
    protected static final ModelAttribute<PagedTopicModel> ATTRIBUTE_PUBLISHED_FIFTEEN_MINUTE =
            PublishedMetrics.ATTRIBUTE_FIFTEEN_MINUTE_RATE.asBuilder(PagedTopicModel.class)
                    .withFunction(PagedTopicModel::getPublishedFifteenMinuteRate)
                    .build();

    /**
     * The channel attributes table.
     */
    protected static final ModelAttribute<PagedTopicModel> ATTRIBUTE_CHANNEL_TABLE = new PagedTopicChannelTableModel();

    // ----- data members ---------------------------------------------------

    /**
     * The paged topic represented by this MBean.
     */
    private final PagedTopic<?> f_pagedTopic;

    /**
     * The topic statistics.
     */
    private final PagedTopicStatistics f_statistics;

    /**
     * The channel models.
     */
    @SuppressWarnings("unchecked")
    private final LongArray<PagedTopicChannelModel> f_aChannel = new SimpleLongArray();

    /**
     * The lock to use to synchronize access to internal state.
     */
    private final Lock f_lock = new ReentrantLock(true);
    }
