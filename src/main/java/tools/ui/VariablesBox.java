package tools.ui;

/**
 * A simple record for holding variable information: a decimal value, a name,
 * and a maximum size limit. Records provide concise, immutable data carriers.
 *
 * @param decValue the integer decimal value
 * @param name     the descriptive name
 * @param maxSize  the maximum size
 */
public record VariablesBox(int decValue, String name, int maxSize) {
    /*
     * Records automatically generate:
     *  - a canonical constructor,
     *  - accessor methods (decValue(), name(), maxSize()),
     *  - equals(), hashCode(), and toString().
     *
     * If additional validation or methods are needed, you can define them here:
     *
     * public VariablesBox {
     *     // Validate parameters, e.g.:
     *     if (decValue < 0) {
     *         throw new IllegalArgumentException("decValue must be non-negative");
     *     }
     * }
     */
}
