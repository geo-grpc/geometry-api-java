package com.esri.core.geometry;


import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleStringCursor extends StringCursor {
    ArrayDeque<String> m_arrayDeque;
    ArrayDeque<Long> m_ids;
    long m_current_id = -1L;

    @Deprecated
    public SimpleStringCursor(String inputString) {
        m_arrayDeque = new ArrayDeque<>(1);
        m_arrayDeque.push(inputString);
    }

    public SimpleStringCursor(String inputString, long id) {
        m_arrayDeque = new ArrayDeque<>(1);
        m_arrayDeque.push(inputString);
        m_ids = new ArrayDeque<>(1);
        m_ids.push(id);
    }

    @Deprecated
    public SimpleStringCursor(String[] inputStringArray) {
        m_arrayDeque = Arrays.stream(inputStringArray).collect(Collectors.toCollection(ArrayDeque::new));
    }

    @Deprecated
    public SimpleStringCursor(List<String> inputStringArray) {
        m_arrayDeque = new ArrayDeque<>(inputStringArray);
    }

    public SimpleStringCursor(ArrayDeque<String> arrayDeque, ArrayDeque<Long> ids) {
        m_ids = ids;
        m_arrayDeque = arrayDeque;
    }

    public long getID() {
        return m_current_id;
    }

    public boolean hasNext() { return m_arrayDeque.size() > 0; }

    void __incrementID() {
        if (m_ids != null && !m_ids.isEmpty())
            m_current_id = m_ids.pop();
        else
            m_current_id++;
    }

    public String next() {
        if (hasNext()) {
            __incrementID();
            return m_arrayDeque.pop();
        }

        return null;
    }
}
