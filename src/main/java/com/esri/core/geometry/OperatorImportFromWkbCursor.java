package com.esri.core.geometry;

import java.nio.ByteBuffer;

public class OperatorImportFromWkbCursor extends GeometryCursor {

    ByteBufferCursor m_inputWkbBuffers;
    int m_importFlags;
    int m_index;

    public OperatorImportFromWkbCursor(int importFlags, ByteBufferCursor wkbBuffers) {
        m_index = -1;
        if (wkbBuffers == null)
            throw new GeometryException("invalid argument");

        m_importFlags = importFlags;
        m_inputWkbBuffers = wkbBuffers;
    }

    @Override
    public Geometry next() {
        ByteBuffer wkbBuffer = m_inputWkbBuffers.next();
        if (wkbBuffer != null) {
            m_index = m_inputWkbBuffers.getByteBufferID();
            return OperatorImportFromWkbLocal.local().execute(m_importFlags, Geometry.Type.Unknown, wkbBuffer, null);
        }
        return null;
    }

    @Override
    public int getGeometryID() {
        return m_index;
    }
}
