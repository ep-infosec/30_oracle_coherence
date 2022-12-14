/*
 * Copyright (c) 2000, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.tangosol.internal.util.collection;

import com.tangosol.io.ExternalizableLite;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.WrapperCollections;

import com.tangosol.util.function.Remote;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.json.bind.annotation.JsonbProperty;

/**
 * A wrapper Collection that controls wrapped set's serialization.
 *
 * @param <E> the element type
 *
 * @author as  2014.11.19
 * @since 12.2.1
 */
@SuppressWarnings({"unchecked", "SuspiciousToArrayCall", "NullableProblems"})
public class PortableCollection<E>
        extends WrapperCollections.AbstractWrapperCollection<E>
        implements PortableObject, ExternalizableLite
    {
    // ---- constructors ----------------------------------------------------

    /**
     * Default constructor.
     */
    public PortableCollection()
        {
        }

    /**
     * Construct PortableCollection instance with a specified supplier.
     *
     * @param supplier  a wrapped set supplier
     */
    public PortableCollection(Remote.Supplier<Collection<E>> supplier)
        {
        m_supplier = supplier;
        }

    /**
     * Return the supplier for this collection.
     *
     * @return the supplier.
     */
    public Remote.Supplier<Collection<E>> getSupplier()
        {
        return m_supplier == null ? DEFAULT_SUPPLIER : m_supplier;
        }

    @Override
    protected Collection<E> getDelegate()
        {
        return m_colDelegate == null ? (m_colDelegate = getSupplier().get()) : m_colDelegate;
        }

    // ----- Object methods -------------------------------------------------

    @Override
    public boolean equals(Object o)
        {
        if (o instanceof PortableCollection)
            {
            return getDelegate().equals(((PortableCollection) o).getDelegate());
            }
        else
            {
            return getDelegate().equals(o);
            }
        }

    // ---- ExternalizableLite implementation -------------------------------

    @Override
    public void readExternal(DataInput in) throws IOException
        {
        m_supplier = ExternalizableHelper.readObject(in);
        ExternalizableHelper.readCollection(in, getDelegate(), null);
        }

    @Override
    public void writeExternal(DataOutput out) throws IOException
        {
        ExternalizableHelper.writeObject(out, m_supplier);
        ExternalizableHelper.writeCollection(out, getDelegate());
        }

    // ---- PortableObject implementation -----------------------------------

    @Override
    public void readExternal(PofReader in) throws IOException
        {
        m_supplier    = in.readObject(0);
        m_colDelegate = in.readCollection(1, getDelegate());
        }

    @Override
    public void writeExternal(PofWriter out) throws IOException
        {
        out.writeObject    (0, m_supplier);
        out.writeCollection(1, getDelegate());
        }

    // ---- static members -------------------------------------------------

    /**
     * Default supplier for the wrapped Collection instance, used for
     * interoperability with .NET and C++ clients which are not able to handle
     * lambda-based Java suppliers.
     */
    protected static final Remote.Supplier DEFAULT_SUPPLIER = ArrayList::new;

    // ---- data members ----------------------------------------------------

    /**
     * Supplier for the wrapped Collection instance.
     */
    @JsonbProperty("supplier")
    protected Remote.Supplier<Collection<E>> m_supplier;
    }
