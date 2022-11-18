package mercury.common;

public class Variables {

    public static final String ID = "id";

    /**
     * Enclose the variable name in the citrus context interpolation string format.
     *
     * @param name the variable name to interpolate
     * @return a {@link String}
     */
    public static String interpolate(String name) {
        return String.format("${%s}", name);
    }

    /**
     * Build a variable named combined with the entity name. This method only return
     * the variable name, to access the variable in the citrus context use {@link Variables#interpolate(String)}.
     *
     * @param entityName the name of the entity to store the variable for
     * @param name the name of the actual variable
     * @return a {@link String}
     */
    public static String nameFrom(String entityName, String name) {
        return String.format("%s_%s", entityName, name);
    }

    /**
     * Enclose {@link Variables#nameFrom(String, String)} returned value with the citrus context interpolation
     * string format
     * @param accountName the name of the user that requires accessing the variable
     * @param name the variable name
     * @return a {@link String}
     */
    public static String interpolate(String accountName, String name) {
        return interpolate(nameFrom(accountName, name));
    }
}
