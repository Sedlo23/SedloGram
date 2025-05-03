package packets.Var;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tools.crypto.ArithmeticalFunctions;
import tools.ui.InputJCombobox;
import tools.string.StringHelper;
import packets.Interfaces.IVariable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static tools.ui.GUIHelper.addLabel;

/**
 * Represents a variable used in a packet, providing both binary and decimal representations,
 * a UI component for editing, and a simplified textual view.
 */
public class Variables implements IVariable {

    private static final Logger LOG = LogManager.getLogger(Variables.class);

    // Constants (unused in current implementation, but kept for potential future use)
    public static final int WIDTH = 0;
    public static final int HEIGHT = 0;
    public static final int LARGE_TEXT_SIZE = 0;

    /** Reference to a combo box that may be used in the UI component. */
    public JComboBox<String> jComboBoxRR;

    /** The binary value as a string. */
    protected String binValue;

    /** The name of the variable. */
    private String name;

    /** Maximum allowed size (number of bits) for this variable. */
    private int maxSize;

    /** A description of this variable. */
    private String description;

    /**
     * An optional condition variable. When set, certain operations are skipped unless
     * this variable's decimal value equals the stored "var" value.
     */
    private Variables cond;

    /** Stores an integer for condition checks. */
    private int var;

    /** Cache for UI components keyed by string. */
    private final Map<String, Component> componentCache = new HashMap<>();

    /**
     * Constructs a Variables instance with the specified name, maximum size, and description.
     *
     * @param name        the variable's name
     * @param maxSize     the maximum bit-length
     * @param description a textual description of the variable
     */
    public Variables(String name, int maxSize, String description) {
        
        this.name = name;
        this.maxSize = maxSize;
        this.description = description;
        this.binValue = "0";
    }

    @Override
    public String getName() {
        String varName = name + " (" + getMaxSize()+") ";
        
        return varName;
    }

    /**
     * Sets the variable's name.
     *
     * @param name the new name
     * @return this Variables instance for chaining
     */
    public Variables setName(String name) {
        
        this.name = name;
        return this;
    }

    @Override
    public int getMaxSize() {
        
        return maxSize;
    }

    /**
     * Sets the maximum size.
     *
     * @param maxSize the maximum bit-length
     */
    public void setMaxSize(int maxSize) {
        
        this.maxSize = maxSize;
    }

    @Override
    public String getDescription() {
        
        return description;
    }

    @Override
    public String getBinValue() {
        String padded = StringHelper.padLeft(binValue, getMaxSize(), '0');
        
        return padded;
    }

    /**
     * Sets the binary value.
     *
     * @param binValue the new binary string value
     */
    public void setBinValue(String binValue) {
        
        if (binValue.length() > maxSize) {
            
            try {
                throw new Exception("MaxBin reached");
            } catch (Exception e) {
                
                e.printStackTrace();
            }
        }
        this.binValue = binValue;
    }

    @Override
    public int getDecValue() {
        int value = (int) ArithmeticalFunctions.bin2Dec(getBinValue());
        
        return value;
    }

    /**
     * Creates a deep copy of this Variables instance.
     *
     * @return a new Variables object with the same binary value and condition settings, or null on error.
     */
    public Variables deepCopy() {
        
        try {
            Class<?> clazz = Class.forName(this.getClass().getName());
            Constructor<?> cons = clazz.getConstructor();
            Object object = cons.newInstance();
            if (object instanceof Variables copy) {
                copy.setBinValue(getBinValue());
                if (cond != null) {
                    copy.setCond(cond, var);
                }
                
                return copy;
            }
        } catch (Exception e) {
            
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the UI component for editing this variable.
     *
     * @return a Swing {@link Component} representing this variable
     */
    public Component getComponent() {
        return getComponent("");
    }

    private JPanel component;

    /**
     * Returns a UI component for this variable, with an optional comment appended to the label.
     *
     * @param comment additional text to display alongside the variable's name
     * @return the constructed component
     */
    public Component getComponent(String comment) {
        

        // Generate combo box items (all possible decimal values up to 2^maxSize)
        ArrayList<String> comboItems = getCombo();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(comboItems.toArray(new String[0]));
        String selectedItem = comboItems.get(getDecValue());
        model.setSelectedItem(selectedItem);

        // Create a combo box with the generated model.
        JComboBox<String> jComboBox = new JComboBox<>(model);
        jComboBox.setLightWeightPopupEnabled(true);

        JLabel jLabel = new JLabel();

        // Add an action listener to update the binary value when a new item is selected.
        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = jComboBox.getSelectedIndex();
                setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(index), getMaxSize()));
                if (cond != null && cond.getDecValue() != var) {
                    jComboBox.setVisible(false);
                }
                jLabel.setVisible(false);
            }
        });

        // Build the label text
        String labelText = getName();
        if (!comment.isBlank()) {
            labelText += " (" + comment + ")";
        }

        jComboBox.setEditable(true);
        jComboBoxRR = jComboBox;
        component = (JPanel) addLabel(jComboBox, labelText, getDescription(), jLabel);
        new InputJCombobox(jComboBox);
        return component;
    }

    /**
     * Initializes this variable's value set using the provided string array.
     * If a condition is set (via cond and var) and not met, the current instance is returned unmodified.
     *
     * @param s an array of strings representing the binary value
     * @return this Variables instance, updated if the condition is met
     */
    public Variables initValueSet(String[] s) {
        
        if (cond != null && cond.getDecValue() != var) {
            
            return this;
        }
        String trimmed = StringHelper.TrimAR(s, getMaxSize());
        setBinValue(trimmed);
        return this;
    }

    /**
     * Returns the full binary data if conditions are met; otherwise, returns an empty string.
     *
     * @return the full binary data or an empty string.
     */
    public String getFullData() {
        if (cond != null && cond.getDecValue() != var) {
            
            return "";
        }
        String binVal = getBinValue();
        
        return binVal;
    }

    /**
     * Sets a condition and associated variable value for this instance.
     *
     * @param cond the condition variable
     * @param i    the associated variable value
     * @return this Variables instance for chaining
     */
    public Variables setCond(Variables cond, int i) {
        
        this.cond = cond;
        this.var = i;
        return this;
    }

    /**
     * Generates a list of possible decimal values (as strings) for the variable,
     * ranging from 0 to 2^(maxSize) - 1.
     *
     * @return an ArrayList of possible values as Strings
     */
    public ArrayList<String> getCombo() {
        
        ArrayList<String> values = new ArrayList<>();
        int upperBound = (int) Math.pow(2, getMaxSize());
        IntStream.range(0, upperBound).forEach(n -> values.add(String.valueOf(n)));
        return values;
    }

    @Override
    public String getSimpleView() {
        if (cond != null && cond.getDecValue() != var) {
            
            return "";
        }
        String simpleView = getName() + "=" + getDecValue() + "\n";
        
        return simpleView;
    }

    /**
     * Returns a string meaning based on the current decimal value.
     *
     * @return a string from the generated combo list corresponding to the decimal value.
     */
    public String getStringMeaning() {
        
        return getCombo().get(getDecValue());
    }

    /**
     * Retrieves a cached component by key.
     *
     * @param key the key associated with the component
     * @return the cached component, or null if not cached
     */
    public Component getCachedComponent(String key) {
        
        return componentCache.get(key);
    }

    /**
     * Caches a component with the given key.
     *
     * @param key       the key to associate with the component
     * @param component the component to cache
     */
    public void cacheComponent(String key, Component component) {
        
        componentCache.put(key, component);
    }


}
