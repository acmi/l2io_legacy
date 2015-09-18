/*
 * Copyright (c) 2014 acmi
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
package acmi.l2.clientmod.unreal.classloader;

import acmi.l2.clientmod.unreal.core.Property;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Arrays;

public class SimpleL2Property implements L2Property{
    private final Property template;
    private final ObservableList<Object> backingList;

    public SimpleL2Property(Property template) {
        this.template = template;
        this.backingList = FXCollections.observableList(new ArrayList<>(Arrays.asList(new Object[template.arrayDimension])), o -> {
            if (o instanceof Observable)
                return new Observable[]{(Observable)o};
            return new Observable[]{};
        });
    }

    @Override
    public Property getTemplate() {
        return template;
    }

    @Override
    public int getSize() {
        return backingList.size();
    }

    @Override
    public Object getAt(int index) {
        return backingList.get(index);
    }

    @Override
    public void putAt(int index, Object value) {
        backingList.set(index, value);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        backingList.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        backingList.removeListener(listener);
    }

    @Override
    public String toString() {
        return getName() + "=" + (getSize() == 1 ? getAt(0) : backingList.toString());
    }
}
