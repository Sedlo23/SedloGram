package packets.Var.M;

import tools.string.StringHelper;
import packets.Var.NID.NID_NTC;
import packets.Var.Variables;
import packets.Var.BinaryChangeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static tools.ui.GUIHelper.addLabel;

public class M_LEVELTR extends Variables {
    private NID_NTC nid_ntc;

    /** List of listeners that will be applied to all subvariables */
    private final List<BinaryChangeListener> subvariableListeners = new CopyOnWriteArrayList<>();

    public M_LEVELTR() {
        super("M_LEVELTR",
                3,
                "Požadovaná úroveň");

        // Add default listener for subvariables
        addDefaultSubvariableListener();

        initializeSubvariables();
    }

    /**
     * Adds a listener that will be automatically applied to all subvariables.
     *
     * @param listener the listener to add to all subvariables
     * @return this M_LEVELTR instance for chaining
     */
    public M_LEVELTR addSubvariableListener(BinaryChangeListener listener) {
        if (listener != null) {
            subvariableListeners.add(listener);
            applyListenerToAllSubvariables(listener);
        }
        return this;
    }

    /**
     * Removes a listener from all subvariables and from the subvariable listeners list.
     *
     * @param listener the listener to remove
     * @return true if the listener was found and removed
     */
    public boolean removeSubvariableListener(BinaryChangeListener listener) {
        boolean removed = subvariableListeners.remove(listener);

        if (removed) {
            removeListenerFromAllSubvariables(listener);
        }

        return removed;
    }

    /**
     * Clears all subvariable listeners.
     */
    public void clearSubvariableListeners() {
        for (BinaryChangeListener listener : subvariableListeners) {
            removeListenerFromAllSubvariables(listener);
        }
        subvariableListeners.clear();
    }

    /**
     * Applies a single listener to all current subvariables.
     *
     * @param listener the listener to apply
     */
    private void applyListenerToAllSubvariables(BinaryChangeListener listener) {
        if (nid_ntc != null) {
            nid_ntc.addBinaryChangeListener(listener);
        }
    }

    /**
     * Removes a single listener from all current subvariables.
     *
     * @param listener the listener to remove
     */
    private void removeListenerFromAllSubvariables(BinaryChangeListener listener) {
        if (nid_ntc != null) {
            nid_ntc.removeBinaryChangeListener(listener);
        }
    }

    /**
     * Applies all registered subvariable listeners to a specific variable.
     *
     * @param variable the variable to apply listeners to
     */
    private void applyAllListenersToVariable(Variables variable) {
        for (BinaryChangeListener listener : subvariableListeners) {
            variable.addBinaryChangeListener(listener);
        }
    }

    /**
     * Adds a default listener that logs changes in subvariables.
     */
    private void addDefaultSubvariableListener() {
        addSubvariableListener((source, oldValue, newValue) -> {
            System.out.println(String.format(
                    "Subvariable '%s' in M_LEVELTR changed from '%s' to '%s'",
                    source.getName(), oldValue, newValue
            ));
        });
    }

    /**
     * Initializes all subvariables and applies listeners.
     */
    private void initializeSubvariables() {
        nid_ntc = new NID_NTC();

        // Apply all listeners to the newly created subvariable
        applyAllListenersToVariable(nid_ntc);
    }

    @Override
    public Variables deepCopy() {
        M_LEVELTR tmp = new M_LEVELTR();

        // Copy listeners first
        tmp.subvariableListeners.addAll(this.subvariableListeners);

        tmp.setBinValue(getBinValue());

        // Create new subvariable and apply listeners
        tmp.nid_ntc = new NID_NTC();
        if (this.nid_ntc != null) {
            tmp.nid_ntc.setBinValue(this.nid_ntc.getBinValue());
        }
        tmp.applyAllListenersToVariable(tmp.nid_ntc);

        return tmp;
    }

    @Override
    public Component getComponent(String com) {
        JPanel panel = new JPanel(new GridBagLayout());

        // Configure GridBag constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 0.5;
        gbc.weighty = 0.5;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;  // Make components fill horizontal space
        gbc.insets = new Insets(5, 5, 5, 5);      // Add padding around components

        JComboBox jComboBox = (JComboBox) ((JPanel) super.getComponent(com)).getComponent(1);
        Component NID_C_comp = nid_ntc.getComponent(com);
        JComboBox jComboBox1 = (JComboBox) (((JPanel) NID_C_comp).getComponent(1));

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox.getSelectedIndex() == 1)
                    jComboBox1.setVisible(true);
                else
                    jComboBox1.setVisible(false);
            }
        });

        // Set initial visibility based on current selection
        if (jComboBox.getSelectedIndex() == 1)
            jComboBox1.setVisible(true);
        else
            jComboBox1.setVisible(false);

        panel.add(jComboBox);
        panel.add(jComboBox1);

        return addLabel(panel, "M_LEVELTR", "", new JLabel());
    }

    @Override
    public Variables initValueSet(String[] s) {
        // Recreate subvariable and apply listeners
        nid_ntc = new NID_NTC();
        applyAllListenersToVariable(nid_ntc);

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() == 1)
            nid_ntc.initValueSet(s);

        return this;
    }

    @Override
    public String getFullData() {
        String tmp = getBinValue();

        if (getDecValue() == 1)
            tmp += nid_ntc.getFullData();

        return tmp;
    }

    @Override
    public String getSimpleView() {
        String tmp = super.getSimpleView();

        if (getDecValue() == 1)
            tmp += nid_ntc.getSimpleView();

        return tmp;
    }

    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> s = new ArrayList<>();

        s.add(0, "Level 0");
        s.add(1, "Level NTC");
        s.add(2, "Level 1");
        s.add(3, "Level 2");
        s.add(4, "Level 3");
        s.add(5, "NOT_USED");
        s.add(6, "NOT_USED");
        s.add(7, "NOT_USED");

        return s;
    }

    /**
     * Gets all subvariables as a list for convenient iteration.
     *
     * @return a list containing all subvariables
     */
    public List<Variables> getAllSubvariables() {
        List<Variables> subvariables = new ArrayList<>();
        if (nid_ntc != null) subvariables.add(nid_ntc);
        return subvariables;
    }

    /**
     * Gets the number of registered subvariable listeners.
     *
     * @return the number of subvariable listeners
     */
    public int getSubvariableListenerCount() {
        return subvariableListeners.size();
    }

    // Getter for the subvariable (useful for testing and specific access)
    public NID_NTC getNid_ntc() {
        return nid_ntc;
    }
}