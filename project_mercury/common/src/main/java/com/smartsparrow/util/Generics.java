package com.smartsparrow.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Generics {

    /**
     * Helper method that returns the parameterized class type for a {@link Class}. The method only works with classes
     * having a single parameterized type that implements a single interface
     *
     * @param arg the supplied class from which the parameterized type should be extracted
     * @return the parameterized class object
     * @throws ClassNotFoundException when either the parameterized class or the generic interface are not found
     */
    public static <T> Class parameterizedClassFor(Class<? extends T> arg) throws ClassNotFoundException {
        Type[] types = arg.getGenericInterfaces();
        if (types.length == 0) {
            throw new ClassNotFoundException();
        }
        Type superclass = types[0];
        try {
            Type[] generics = ((ParameterizedType) superclass).getActualTypeArguments();
            return Class.forName(generics[0].getTypeName());
        } catch (ClassCastException e) {
            throw new ClassNotFoundException("No parameterized type found");
        }
    }
}
