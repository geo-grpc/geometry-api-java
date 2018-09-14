package com.esri.core.geometry;

import java.nio.ByteBuffer;

public class OperatorImportFromWkbCursor extends GeometryCursor {

    ByteBufferCursor m_inputWkbBuffers;
    int m_importFlags;

    public OperatorImportFromWkbCursor(int importFlags, ByteBufferCursor wkbBuffers) {
        if (wkbBuffers == null)
            throw new GeometryException("invalid argument");

        m_importFlags = importFlags;
        m_inputWkbBuffers = wkbBuffers;
    }

    @Override
    public boolean hasNext() { return m_inputWkbBuffers != null && m_inputWkbBuffers.hasNext(); }

    @Override
    public Geometry next() {
        if (hasNext()) {
            return OperatorImportFromWkbLocal.local().execute(
                    m_importFlags,
                    Geometry.Type.Unknown,
                    m_inputWkbBuffers.next(),
                    null);
        }
        return null;
    }

    @Override
    public long getGeometryID() {
        return m_inputWkbBuffers.getByteBufferID();
    }
}
