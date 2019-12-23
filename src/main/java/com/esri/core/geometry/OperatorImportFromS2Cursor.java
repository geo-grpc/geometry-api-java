package com.esri.core.geometry;

import com.google.common.geometry.S2Cell;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLngRect;

import java.util.ArrayDeque;

public class OperatorImportFromS2Cursor extends GeometryCursor {
    private ArrayDeque<Long> m_s2ids;
    private double m_maxDeviation;

    OperatorImportFromS2Cursor(ArrayDeque<Long> s2ids, double maxDeviation) {
        m_maxDeviation = maxDeviation;
        m_s2ids = s2ids;
    }

    @Override
    public boolean hasNext() { return m_s2ids != null && m_s2ids.size() > 0; }

    @Override
    public Geometry next() {
        if (hasNext()) {
            return createS2Geometry(m_s2ids.pop(), m_maxDeviation);
        }
        return null;
    }


    static Geometry createS2Geometry(long s2Id, double maxDeviation) {
        S2CellId cellId = new S2CellId(s2Id);
        S2Cell cell = new S2Cell(cellId);
        S2LatLngRect latLngRect = cell.getRectBound();
        
        // TODO geodetic max deviation densify
        return new Envelope(
                latLngRect.lngLo().degrees(),
                latLngRect.lngHi().degrees(),
                latLngRect.latLo().degrees(),
                latLngRect.latHi().degrees());
    }
}
