package com.esri.core.geometry;


public class SimpleStringCursor extends StringCursor{
    String m_string;
    String[] m_stringArray;

    int m_index;
    int m_count;

    public SimpleStringCursor(String inputString) {
        m_string = inputString;
        m_index = -1;
        m_count = 1;
    }


    public SimpleStringCursor(String[] inputStringArray) {
        m_stringArray = inputStringArray;
        m_index = -1;
        m_count = m_stringArray.length;
    }

    public int getID() {
        return m_index;
    }

    public String next() {
        if (m_index < m_count - 1) {
            m_index++;
            return m_string != null ? m_string : m_stringArray[m_index];
        }

        return null;
    }
}
