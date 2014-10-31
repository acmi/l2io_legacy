package acmi.l2.clientmod.unreal.classloader;

import acmi.l2.clientmod.unreal.Core.Field;
import acmi.l2.clientmod.unreal.Core.Function;
import acmi.l2.clientmod.unreal.Core.Struct;
import acmi.l2.clientmod.unreal.properties.PropertiesUtil;

import java.util.List;
import java.util.Optional;

public interface UnrealClassLoader {
    PropertiesUtil getPropertiesUtil();

    Struct getStruct(String structName) throws ClassLoadException;

    default Optional<Struct> getStructQuetly(String structName) {
        try {
            return Optional.of(getStruct(structName));
        } catch (ClassLoadException e) {
            return Optional.ofNullable(null);
        }
    }

    List<Field> getStructFields(String structName) throws ClassLoadException;

    default Optional<List<Field>> getStructFieldsQuetly(String structName) {
        try {
            return Optional.of(getStructFields(structName));
        } catch (ClassLoadException e) {
            return Optional.ofNullable(null);
        }
    }

    default Field getField(String field) throws ClassLoadException {
        int delimiterInd = field.lastIndexOf('.');
        String struct = field.substring(0, delimiterInd);
        String fieldName = field.substring(delimiterInd + 1);
        return getStructFields(struct).stream()
                .filter(f -> f.getEntry().getObjectName().getName().equalsIgnoreCase(fieldName))
                .findAny()
                .orElseThrow(() -> new ClassLoadException(String.format("Filed %s not found", field)));
    }

    default Optional<Field> getFieldQuetly(String field) {
        try {
            return Optional.of(getField(field));
        } catch (ClassLoadException e) {
            return Optional.ofNullable(null);
        }
    }

    Function getNativeFunction(int index) throws ClassLoadException;

    default Optional<Function> getNativeFunctionQuetly(int index) {
        try {
            return Optional.of(getNativeFunction(index));
        } catch (ClassLoadException e) {
            return Optional.ofNullable(null);
        }
    }

    default boolean isSubclass(String parent, String child) {
        if (parent.equalsIgnoreCase(child))
            return true;

        child = getSuperClass(child);

        return child != null && isSubclass(parent, child);
    }

    default String getSuperClass(String clazz) {
        Optional<Struct> childClass = getStructQuetly(clazz);
        if (childClass.isPresent()) {
            return childClass.get().getEntry().getObjectSuperClass().getObjectFullName();
        }
        return null;
    }
}
