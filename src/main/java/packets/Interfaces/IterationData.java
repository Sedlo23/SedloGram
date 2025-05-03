package packets.Interfaces;

import packets.Var.Variables;
import java.util.ArrayList;
import java.util.List;

/**
 * IterationData is an ArrayList of {@link Variables} representing a single iteration's data.
 * It also holds a reference to its parent container and an iteration name.
 * The {@code toString()} method returns the iteration name concatenated with its index within the parent list.
 */
public class IterationData extends ArrayList<Variables> {

    /** The parent container that holds this IterationData. */
    private final List<?> parent;

    /** The name of this iteration, used in its string representation. */
    private final String iterName;

    /**
     * Constructs a new IterationData with the specified parent container and a default iteration name "Iter".
     *
     * @param parent the parent container containing this IterationData
     */
    public IterationData(List<?> parent) {
        this(parent, "Iter");
    }

    /**
     * Constructs a new IterationData with the specified parent container and iteration name.
     *
     * @param parent   the parent container containing this IterationData
     * @param iterName the name for this iteration
     */
    public IterationData(List<?> parent, String iterName) {
        this.parent = parent;
        this.iterName = iterName;
    }

    /**
     * Returns a string representation of this iteration, combining the iteration name
     * with its index in the parent container.
     *
     * @return a string in the format "iterName index"
     */
    @Override
    public String toString() {
        return iterName + " " + parent.indexOf(this);
    }
}
