package packets.Var;

/**
 * Functional interface for listening to binary value changes in Variables.
 */
@FunctionalInterface
public interface BinaryChangeListener {
    /**
     * Called when the binary value of a Variables instance changes.
     *
     * @param source   the Variables instance that changed
     * @param oldValue the previous binary value
     * @param newValue the new binary value
     */
    void onBinaryValueChanged(Variables source, String oldValue, String newValue);
}