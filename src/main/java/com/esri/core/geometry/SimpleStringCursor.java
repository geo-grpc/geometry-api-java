package com.esri.core.geometry;


import java.util.Arrays;
import java.util.List;

public class SimpleStringCursor extends StringCursor{
    String m_string;
    List<String> m_stringArray;

    int m_index;
    int m_count;

    public SimpleStringCursor(String inputString) {
        m_string = inputString;
        m_index = -1;
        m_count = 1;
    }


    public SimpleStringCursor(String[] inputStringArray) {
        m_stringArray = Arrays.asList(inputStringArray);
        m_index = -1;
        m_count = m_stringArray.size();
    }

    public SimpleStringCursor(List<String> inputStringArray) {
        m_stringArray = inputStringArray;
        m_index = -1;
        m_count = m_stringArray.size();
    }

    public int getID() {
        return m_index;
    }

    public String next() {
        if (m_index < m_count - 1) {
            m_index++;
            return m_string != null ? m_string : m_stringArray.get(m_index);
        }

        return null;
    }
}
