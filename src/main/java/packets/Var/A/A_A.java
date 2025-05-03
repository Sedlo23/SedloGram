package packets.Var.A;

import packets.Var.Variables;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Abstract class A_A represents a specific type of variable that provides a custom
 * combo box of acceleration values in m/s². It extends the base {@link Variables}
 * class and overrides {@link #getCombo()} to supply a formatted list of options.
 */
public abstract class A_A extends Variables {

    /**
     * Constructs a new A_A variable.
     *
     * @param name        the variable's name
     * @param maxSize     the maximum bit-length for the variable
     * @param description a description of the variable
     */
    public A_A(String name, int maxSize, String description) {
        super(name, maxSize, description);
    }

    /**
     * Generates a list of combo box items representing acceleration values.
     * <p>
     * The list contains 61 acceleration values from "0.00 m/s²" to "3.00 m/s²"
     * (in increments of 0.05 m/s²), followed by three additional options:
     * "Cíl v CSM", "Doba indikace", and "Bez zobrazení".
     * </p>
     *
     * @return an {@link ArrayList} of {@link String} objects for use in a combo box.
     */
    @Override
    public ArrayList<String> getCombo() {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        ArrayList<String> comboItems = new ArrayList<>();

        // Generate acceleration values from 0.00 to 3.00 m/s² (inclusive)
        for (int i = 0; i < 61; i++) {
            double acceleration = i * 0.05;
            comboItems.add(decimalFormat.format(acceleration) + " m/s²");
        }

        // Add additional options
        comboItems.add("Cíl v CSM");
        comboItems.add("Doba indikace");
        comboItems.add("Bez zobrazení");

        return comboItems;
    }
}
