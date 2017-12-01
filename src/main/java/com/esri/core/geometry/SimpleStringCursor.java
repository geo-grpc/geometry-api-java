package com.esri.core.geometry;


import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SimpleStringCursor extends StringCursor {
    Iterator<String> m_stringIterator;
    int m_index;

    public SimpleStringCursor(String inputString) {
        this(Arrays.asList(inputString));
    }


    public SimpleStringCursor(String[] inputStringArray) {
        this(Arrays.asList(inputStringArray));
    }

    public SimpleStringCursor(List<String> inputStringArray) {
        m_stringIterator = inputStringArray.iterator();
        m_index = -1;
    }

    public int getID() {
        return m_index;
    }

    public String next() {
        if (m_stringIterator.hasNext()){
            m_index++;
            return m_stringIterator.next();
        }

        return null;
    }
}
