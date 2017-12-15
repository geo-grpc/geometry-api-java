package com.esri.core.geometry;


import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleStringCursor extends StringCursor {
    ArrayDeque<String> m_arrayDeque;
    int m_index = -1;

    public SimpleStringCursor(String inputString) {
        this(Arrays.asList(inputString));
    }


    public SimpleStringCursor(String[] inputStringArray) {
        m_arrayDeque = Arrays.stream(inputStringArray).collect(Collectors.toCollection(ArrayDeque::new));
    }

    public SimpleStringCursor(List<String> inputStringArray) {
        m_arrayDeque = new ArrayDeque<>(inputStringArray);
    }

    public SimpleStringCursor(ArrayDeque<String> arrayDeque) {
        m_arrayDeque = arrayDeque;
    }

    public int getID() {
        return m_index;
    }

    public boolean hasNext() { return m_arrayDeque.size() > 0; }

    public String next() {
        if (this.hasNext()) {
            m_index++;
            return m_arrayDeque.pop();
        }

        return null;
    }
}
