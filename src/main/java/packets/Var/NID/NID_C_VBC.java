package packets.Var.NID;

import tools.string.StringHelper;
import packets.Var.T.T_VBC;
import packets.Var.Variables;
import packets.Var.BinaryChangeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static tools.ui.GUIHelper.setTitle;

public class NID_C_VBC extends Variables {
    private T_VBC t_vbc;

    /** List of listeners that will be applied to all subvariables */
    private final List<BinaryChangeListener> subvariableListeners = new CopyOnWriteArrayList<>();

    public NID_C_VBC() {
        super("NID_C_VBC",
                1,
                "Kód používaný k identifikaci země nebo regionu, ve kterém se nachází skupina balíků, RBC nebo RIU. Nemusí se nutně řídit administrativními nebo politickými hranicemi.");

        // Add default listener for subvariables
        addDefaultSubvariableListener();

        initializeSubvariables();
    }

    /**
     * Adds a listener that will be automatically applied to all subvariables.
     *
     * @param listener the listener to add to all subvariables
     * @return this NID_C_VBC instance for chaining
     */
    public NID_C_VBC addSubvariableListener(BinaryChangeListener listener) {
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
        if (t_vbc != null) {
            t_vbc.addBinaryChangeListener(listener);
        }
    }

    /**
     * Removes a single listener from all current subvariables.
     *
     * @param listener the listener to remove
     */
    private void removeListenerFromAllSubvariables(BinaryChangeListener listener) {
        if (t_vbc != null) {
            t_vbc.removeBinaryChangeListener(listener);
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
                    "Subvariable '%s' in NID_C_VBC changed from '%s' to '%s'",
                    source.getName(), oldValue, newValue
            ));
        });
    }

    /**
     * Initializes all subvariables and applies listeners.
     */
    private void initializeSubvariables() {
        t_vbc = new T_VBC();

        // Apply all listeners to the newly created subvariable
        applyAllListenersToVariable(t_vbc);
    }

    @Override
    public Variables deepCopy() {
        NID_C_VBC tmp = new NID_C_VBC();

        // Copy listeners first
        tmp.subvariableListeners.addAll(this.subvariableListeners);

        tmp.setBinValue(getBinValue());

        // Create new subvariable and apply listeners
        tmp.t_vbc = new T_VBC();
        if (this.t_vbc != null) {
            tmp.t_vbc.setBinValue(this.t_vbc.getBinValue());
        }
        tmp.applyAllListenersToVariable(tmp.t_vbc);

        return tmp;
    }

    @Override
    public Component getComponent(String com) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JComboBox jComboBox = (JComboBox) ((JPanel) super.getComponent(com)).getComponent(1);
        Component NID_C_comp = t_vbc.getComponent(com);

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox.getSelectedIndex() == 1)
                    NID_C_comp.setVisible(true);
                else
                    NID_C_comp.setVisible(false);
            }
        });

        // Set initial visibility based on current selection
        if (jComboBox.getSelectedIndex() == 1)
            NID_C_comp.setVisible(true);
        else
            NID_C_comp.setVisible(false);

        panel.add(jComboBox);
        panel.add(NID_C_comp);

        return setTitle(panel, "Q_NEWCOUNTRY");
    }

    @Override
    public Variables initValueSet(String[] s) {
        // Recreate subvariable and apply listeners
        t_vbc = new T_VBC();
        applyAllListenersToVariable(t_vbc);

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() == 1)
            t_vbc.initValueSet(s);

        return this;
    }

    @Override
    public String getFullData() {
        String tmp = getBinValue();

        if (getDecValue() == 1)
            tmp += t_vbc.getFullData();

        return tmp;
    }

    @Override
    public String getSimpleView() {
        String tmp = super.getSimpleView();

        if (getDecValue() == 1)
            tmp += t_vbc.getSimpleView();

        return tmp;
    }

    /**
     * Gets all subvariables as a list for convenient iteration.
     *
     * @return a list containing all subvariables
     */
    public List<Variables> getAllSubvariables() {
        List<Variables> subvariables = new ArrayList<>();
        if (t_vbc != null) subvariables.add(t_vbc);
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
    public T_VBC getT_vbc() {
        return t_vbc;
    }
}