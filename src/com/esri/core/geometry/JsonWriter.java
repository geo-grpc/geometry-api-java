/*
 Copyright 1995-2013 Esri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 For additional information, contact:
 Environmental Systems Research Institute, Inc.
 Attn: Contracts Dept
 380 New York Street
 Redlands, California, USA 92373

 email: contracts@esri.com
 */
package com.esri.core.geometry;

abstract class JsonWriter {

    abstract Object getJson();

    abstract void startObject();

    abstract void startArray();

    abstract void endObject();

    abstract void endArray();

    abstract void addPairObject(String fieldName);

    abstract void addPairArray(String fieldName);

    abstract void addPairString(String fieldName, String v);

    abstract void addPairDouble(String fieldName, double v);

    abstract void addPairDoubleF(String fieldName, double v, int decimals);

    abstract void addPairInt(String fieldName, int v);

    abstract void addPairBoolean(String fieldName, boolean v);

    abstract void addPairNull(String fieldName);

    abstract void addValueObject();

    abstract void addValueArray();

    abstract void addValueString(String v);

    abstract void addValueDouble(double v);

    abstract void addValueDoubleF(double v, int decimals);

    abstract void addValueInt(int v);

    abstract void addValueBoolean(boolean v);

    abstract void addValueNull();

    protected interface Action {

        static final int accept = 0;
        static final int addContainer = 1;
        static final int popObject = 4;
        static final int popArray = 8;
        static final int addPair = 16;
        static final int addValue = 32;
    }

    protected interface State {

        static final int accept = 0;
        static final int start = 1;
        static final int objectStart = 2;
        static final int arrayStart = 3;
        static final int pairEnd = 4;
        static final int elementEnd = 6;
    }
}

final class JsonStringWriter extends JsonWriter {

    @Override
    Object getJson() {
        next_(Action.accept);
        return m_jsonString.toString();
    }

    @Override
    void startObject() {
        next_(Action.addContainer);
        m_jsonString.append('{');
        m_functionStack.add(State.objectStart);
    }

    @Override
    void startArray() {
        next_(Action.addContainer);
        m_jsonString.append('[');
        m_functionStack.add(State.arrayStart);
    }

    @Override
    void endObject() {
        next_(Action.popObject);
        m_jsonString.append('}');
    }

    @Override
    void endArray() {
        next_(Action.popArray);
        m_jsonString.append(']');
    }

    @Override
    void addPairObject(String fieldName) {
        next_(Action.addPair);
        appendQuote_(fieldName);
        m_jsonString.append(":");
        addValueObject_();
    }

    @Override
    void addPairArray(String fieldName) {
        next_(Action.addPair);
        appendQuote_(fieldName);
        m_jsonString.append(":");
        addValueArray_();
    }

    @Override
    void addPairString(String fieldName, String v) {
        next_(Action.addPair);
        appendQuote_(fieldName);
        m_jsonString.append(":");
        addValueString_(v);
    }

    @Override
    void addPairDouble(String fieldName, double v) {
        next_(Action.addPair);
        appendQuote_(fieldName);
        m_jsonString.append(":");
        addValueDouble_(v);
    }

    @Override
    void addPairDoubleF(String fieldName, double v, int decimals) {
        next_(Action.addPair);
        appendQuote_(fieldName);
        m_jsonString.append(":");
        addValueDoubleF_(v, decimals);
    }

    @Override
    void addPairInt(String fieldName, int v) {
        next_(Action.addPair);
        appendQuote_(fieldName);
        m_jsonString.append(":");
        addValueInt_(v);
    }

    @Override
    void addPairBoolean(String fieldName, boolean v) {
        next_(Action.addPair);
        appendQuote_(fieldName);
        m_jsonString.append(":");
        addValueBoolean_(v);
    }

    @Override
    void addPairNull(String fieldName) {
        next_(Action.addPair);
        appendQuote_(fieldName);
        m_jsonString.append(":");
        addValueNull_();
    }

    @Override
    void addValueObject() {
        next_(Action.addValue);
        addValueObject_();
    }

    @Override
    void addValueArray() {
        next_(Action.addValue);
        addValueArray_();
    }

    @Override
    void addValueString(String v) {
        next_(Action.addValue);
        addValueString_(v);
    }

    @Override
    void addValueDouble(double v) {
        next_(Action.addValue);
        addValueDouble_(v);
    }

    @Override
    void addValueDoubleF(double v, int decimals) {
        next_(Action.addValue);
        addValueDoubleF_(v, decimals);
    }

    @Override
    void addValueInt(int v) {
        next_(Action.addValue);
        addValueInt_(v);
    }

    @Override
    void addValueBoolean(boolean v) {
        next_(Action.addValue);
        addValueBoolean_(v);
    }

    @Override
    void addValueNull() {
        next_(Action.addValue);
        addValueNull_();
    }

    JsonStringWriter() {
        m_jsonString = new StringBuilder();
        m_functionStack = new AttributeStreamOfInt32(0);
        m_functionStack.add(State.accept);
        m_functionStack.add(State.start);
    }
    private StringBuilder m_jsonString;
    private AttributeStreamOfInt32 m_functionStack;

    private void addValueObject_() {
        m_jsonString.append('{');
        m_functionStack.add(State.objectStart);
    }

    private void addValueArray_() {
        m_jsonString.append('[');
        m_functionStack.add(State.arrayStart);
    }

    private void addValueString_(String v) {
        appendQuote_(v);
    }

    private void addValueDouble_(double v) {
        if (NumberUtils.isNaN(v)) {
            addValueNull_();
            return;
        }

        StringUtils.appendDouble(v, 17, m_jsonString);
    }

    private void addValueDoubleF_(double v, int decimals) {
        if (NumberUtils.isNaN(v)) {
            addValueNull_();
            return;
        }

        StringUtils.appendDoubleF(v, decimals, m_jsonString);
    }

    private void addValueInt_(int v) {
        m_jsonString.append(v);
    }

    private void addValueBoolean_(boolean v) {
        if (v) {
            m_jsonString.append("true");
        } else {
            m_jsonString.append("false");
        }
    }

    private void addValueNull_() {
        m_jsonString.append("null");
    }

    private void next_(int action) {
        switch (m_functionStack.getLast()) {
            case State.accept:
                accept_(action);
                break;
            case State.start:
                start_(action);
                break;
            case State.objectStart:
                objectStart_(action);
                break;
            case State.arrayStart:
                arrayStart_(action);
                break;
            case State.pairEnd:
                pairEnd_(action);
                break;
            case State.elementEnd:
                elementEnd_(action);
                break;
            default:
                throw new GeometryException("internal error");
        }
    }

    private void accept_(int action) {
        if (action != Action.accept) {
            throw new GeometryException("invalid call");
        }
    }

    private void start_(int action) {
        if (action == Action.addContainer) {
            m_functionStack.removeLast();
        } else {
            throw new GeometryException("invalid call");
        }
    }

    private void objectStart_(int action) {
        m_functionStack.removeLast();

        if (action == Action.addPair) {
            m_functionStack.add(State.pairEnd);
        } else if (action != Action.popObject) {
            throw new GeometryException("invalid call");
        }
    }

    private void pairEnd_(int action) {
        if (action == Action.addPair) {
            m_jsonString.append(',');
        } else if (action == Action.popObject) {
            m_functionStack.removeLast();
        } else {
            throw new GeometryException("invalid call");
        }
    }

    private void arrayStart_(int action) {
        m_functionStack.removeLast();

        if (action == Action.addValue) {
            m_functionStack.add(State.elementEnd);
        } else if (action != Action.popArray) {
            throw new GeometryException("invalid call");
        }
    }

    private void elementEnd_(int action) {
        if (action == Action.addValue) {
            m_jsonString.append(',');
        } else if (action == Action.popArray) {
            m_functionStack.removeLast();
        } else {
            throw new GeometryException("invalid call");
        }
    }

    private void appendQuote_(String string) {
        int count = 0;
        int start = 0;
        int end = string.length();

        m_jsonString.append('"');

        for (int i = 0; i < end; i++) {
            switch (string.charAt(i)) {
                case '"':
                    if (count > 0) {
                        m_jsonString.append(string, start, start + count);
                        count = 0;
                    }
                    m_jsonString.append("\\\"");
                    start = i + 1;
                    break;
                case '\\':
                    if (count > 0) {
                        m_jsonString.append(string, start, start + count);
                        count = 0;
                    }
                    m_jsonString.append("\\\\");
                    start = i + 1;
                    break;
                case '/':
                    if (i > 0 && string.charAt(i - 1) == '<') {
                        if (count > 0) {
                            m_jsonString.append(string, start, start + count);
                            count = 0;
                        }
                        m_jsonString.append("\\/");
                        start = i + 1;
                    } else {
                        count++;
                    }
                    break;
                case '\b':
                    if (count > 0) {
                        m_jsonString.append(string, start, start + count);
                        count = 0;
                    }
                    m_jsonString.append("\\b");
                    start = i + 1;
                    break;
                case '\f':
                    if (count > 0) {
                        m_jsonString.append(string, start, start + count);
                        count = 0;
                    }
                    m_jsonString.append("\\f");
                    start = i + 1;
                    break;
                case '\n':
                    if (count > 0) {
                        m_jsonString.append(string, start, start + count);
                        count = 0;
                    }
                    m_jsonString.append("\\n");
                    start = i + 1;
                    break;
                case '\r':
                    if (count > 0) {
                        m_jsonString.append(string, start, start + count);
                        count = 0;
                    }
                    m_jsonString.append("\\r");
                    start = i + 1;
                    break;
                case '\t':
                    if (count > 0) {
                        m_jsonString.append(string, start, start + count);
                        count = 0;
                    }
                    m_jsonString.append("\\t");
                    start = i + 1;
                    break;
                default:
                    count++;
                    break;
            }
        }

        if (count > 0) {
            m_jsonString.append(string, start, start + count);
        }

        m_jsonString.append('"');
    }
}
