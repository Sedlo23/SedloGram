package packets.Interfaces;

/**
 * Defines the contract for a variable used within a packet.
 * <p>
 * Implementations of this interface should provide the variable's name, maximum size,
 * description, and representations in both binary and decimal forms, along with a simplified view.
 * </p>
 */
public interface IVariable {

    /**
     * Returns the name of the variable.
     *
     * @return the variable name as a String.
     */
    String getName();

    /**
     * Returns the maximum size of the variable.
     *
     * @return the maximum size as an integer.
     */
    int getMaxSize();

    /**
     * Returns a description of the variable.
     *
     * @return the description as a String.
     */
    String getDescription();

    /**
     * Returns the binary representation of the variable's value.
     *
     * @return a String containing the binary value.
     */
    String getBinValue();

    /**
     * Returns the decimal representation of the variable's value.
     *
     * @return the decimal value as an int.
     */
    int getDecValue();

    /**
     * Returns a simplified textual view of the variable.
     *
     * @return a simplified view as a String.
     */
    String getSimpleView();
}
