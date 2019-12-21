package com.esri.core.geometry;

import java.util.ArrayDeque;

public class OperatorImportFromS2Cursor extends GeometryCursor {
    private ArrayDeque<Integer> m_s2ids;

    OperatorImportFromS2Cursor(ArrayDeque<Integer> s2ids) {
        m_s2ids = s2ids;
    }

    @Override
    public boolean hasNext() { return m_s2ids != null && m_s2ids.size() > 0; }

    @Override
    public Geometry next() {
        if (hasNext()) {
            return createS2Geometry(m_s2ids.pop());
        }
        return null;
    }



    protected static Geometry createS2Geometry(int s2Id) {
        return null;
    }
}
