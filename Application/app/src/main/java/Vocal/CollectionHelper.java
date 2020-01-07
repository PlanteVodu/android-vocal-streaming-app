// **********************************************************************
//
// Copyright (c) 2003-2017 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************
//
// Ice version 3.7.0
//
// <auto-generated>
//
// Generated from file `Coll.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>
//

package Vocal;

public final class CollectionHelper
{
    public static void write(com.zeroc.Ice.OutputStream ostr, Track[] v)
    {
        if(v == null)
        {
            ostr.writeSize(0);
        }
        else
        {
            ostr.writeSize(v.length);
            for(int i0 = 0; i0 < v.length; i0++)
            {
                Track.ice_write(ostr, v[i0]);
            }
        }
    }

    public static Track[] read(com.zeroc.Ice.InputStream istr)
    {
        final Track[] v;
        final int len0 = istr.readAndCheckSeqSize(8);
        v = new Track[len0];
        for(int i0 = 0; i0 < len0; i0++)
        {
            v[i0] = Track.ice_read(istr);
        }
        return v;
    }

    public static void write(com.zeroc.Ice.OutputStream ostr, int tag, java.util.Optional<Track[]> v)
    {
        if(v != null && v.isPresent())
        {
            write(ostr, tag, v.get());
        }
    }

    public static void write(com.zeroc.Ice.OutputStream ostr, int tag, Track[] v)
    {
        if(ostr.writeOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            int pos = ostr.startSize();
            CollectionHelper.write(ostr, v);
            ostr.endSize(pos);
        }
    }

    public static java.util.Optional<Track[]> read(com.zeroc.Ice.InputStream istr, int tag)
    {
        if(istr.readOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            istr.skip(4);
            Track[] v;
            v = CollectionHelper.read(istr);
            return java.util.Optional.of(v);
        }
        else
        {
            return java.util.Optional.empty();
        }
    }
}
