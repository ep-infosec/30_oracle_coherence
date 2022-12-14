/*
 * Copyright (c) 2000, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.tangosol.internal.util.stream.collectors;

import com.tangosol.io.AbstractEvolvable;
import com.tangosol.io.Evolvable;
import com.tangosol.io.ExternalizableLite;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;

import com.tangosol.util.function.Remote;

import com.tangosol.util.stream.RemoteCollector;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * Simple implementation of {@code RemoteCollector} that collects stream
 * entries into a supplied collection.
 *
 * @param <T> the type of elements to be collected
 * @param <C> the type of the collection to collect into
 *
 * @author as 20014.12.30
 * @since 12.2.1
 */
public class CollectionCollector<T, C extends Collection<T>>
        extends AbstractEvolvable
        implements RemoteCollector<T, C, C>, ExternalizableLite, PortableObject
    {
    // ---- constructors ----------------------------------------------------

    /**
     * Deserialization constructor.
     */
    public CollectionCollector()
        {
        }

    /**
     * Construct CollectionCollector instance.
     *
     * @param supplier         the supplier to use
     */
    public CollectionCollector(Supplier<C> supplier)
        {
        this(supplier, null);
        }

    /**
     * Construct CollectionCollector instance.
     *
     * @param supplier  the supplier to use
     * @param finisher  the finisher to use
     *
     * @since 22.09
     */
    public CollectionCollector(Supplier<C> supplier, Function<C, C> finisher)
        {
        m_supplier = supplier;
        m_finisher = finisher;
        }

    /**
     * Construct CollectionCollector instance.
     *
     * @param supplier         the supplier to use
     */
    public CollectionCollector(Remote.Supplier<C> supplier)
        {
        m_supplier = supplier;
        }

    // ---- Collector interface ---------------------------------------------

    @Override
    public Supplier<C> supplier()
        {
        return m_supplier;
        }

    @Override
    public BiConsumer<C, T> accumulator()
        {
        return Collection::add;
        }

    @Override
    public BinaryOperator<C> combiner()
        {
        return (r1, r2) ->
                   {
                   r1.addAll(r2);
                   return r1;
                   };
        }

    @Override
    public Function<C, C> finisher()
        {
        return m_finisher == null ? Function.identity() : m_finisher;
        }

    @Override
    public Set<Characteristics> characteristics()
        {
        return S_CHARACTERISTICS;
        }

    // ---- ExternalizableLite interface ------------------------------------

    @Override
    public void readExternal(DataInput in) throws IOException
        {
        m_supplier = ExternalizableHelper.readObject(in);
        try
            {
            m_finisher = ExternalizableHelper.readObject(in);
            }
        catch (EOFException ignore)
            {
            // the best we can do when reading an older version
            }
        }

    @Override
    public void writeExternal(DataOutput out) throws IOException
        {
        ExternalizableHelper.writeObject(out, m_supplier);
        ExternalizableHelper.writeObject(out, m_finisher);
        }

    // ---- PortableObject interface ----------------------------------------

    @Override
    public void readExternal(PofReader in) throws IOException
        {
        m_supplier = in.readObject(0);
        if (in.getVersionId() >= 1)
            {
            m_finisher = in.readObject(1);
            }
        }

    @Override
    public void writeExternal(PofWriter out) throws IOException
        {
        out.writeObject(0, m_supplier);
        out.writeObject(1, m_finisher);
        }

    // ---- Evolvable interface ---------------------------------------------

    public int getImplVersion()
        {
        return VERSION;
        }

    // ---- static members ----------------------------------------------------

    private static final int VERSION = 1;

    static final Set<Characteristics> S_CHARACTERISTICS = EnumSet.noneOf(Characteristics.class);

    // ---- data members ----------------------------------------------------

    @JsonbProperty("supplier")
    protected Supplier<C> m_supplier;

    @JsonbProperty("finisher")
    protected Function<C, C> m_finisher;
    }
