/*
 * The MIT License
 *
 * Copyright 2016 Manuel Montenegro (mmontene@ucm.es).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package es.ucm.pdfmeta.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 *
 * @author Manuel Montenegro (mmontene@ucm.es)
 */
public class MetadataProperty<T> {
    private final String name;
    private T value;
    private final List<PropertyListener> listeners;
    

    public MetadataProperty(String name, T value) {
        this.name = name;
        this.value = value;
        this.listeners = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T newValue) {
        T oldValue = this.value;
        this.value = newValue;
        firePropertyChanged(oldValue, newValue);
    }
    
    public void addPropertyListener(PropertyListener pl) {
        this.listeners.add(pl);
    }
    
    public boolean removePropertyListener(PropertyListener pl) {
        return this.listeners.remove(pl);
    }
    
    protected void firePropertyChanged(T oldValue, T newValue) {
        listeners.forEach(pl -> 
                pl.propertyChanged(this.name, oldValue, newValue));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.name);
        hash = 61 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MetadataProperty<?> other = (MetadataProperty<?>) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }
    
}
