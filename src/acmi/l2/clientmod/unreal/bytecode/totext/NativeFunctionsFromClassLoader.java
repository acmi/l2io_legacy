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
package acmi.l2.clientmod.unreal.bytecode.totext;

import acmi.l2.clientmod.unreal.UnrealException;
import acmi.l2.clientmod.unreal.classloader.UnrealClassLoader;

import java.util.Collection;

import static acmi.l2.clientmod.unreal.core.Function.Flag.*;

public class NativeFunctionsFromClassLoader implements NativeFunctionsSupplier {
    private UnrealClassLoader classLoader;

    public NativeFunctionsFromClassLoader(UnrealClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public NativeFunction apply(Integer integer) throws UnrealException {
        return classLoader.getNativeFunctionQuetly(integer)
                .map(function -> {
                    Collection<acmi.l2.clientmod.unreal.core.Function.Flag> flags = getFlags(function.functionFlags);
                    return new NativeFunction(integer, function.getFriendlyName(), flags.contains(PRE_OPERATOR), function.operatorPrecedence, flags.contains(OPERATOR));
                }).orElseThrow(() -> new UnrealException(String.format("Native function (%d) not found", integer)));
    }
}
