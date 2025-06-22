package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.NID.NID_C;
import packets.Var.NID.NID_RBC;
import packets.Var.NID.NID_TEXTMESSAGE;
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

public class Q_TEXTREPORT extends Variables {
    private NID_C nid_c;
    private NID_RBC nid_rbc;
    private NID_TEXTMESSAGE nid_textmessage;

    /** List of listeners that will be applied to all subvariables */
    private final List<BinaryChangeListener> subvariableListeners = new CopyOnWriteArrayList<>();

    public Q_TEXTREPORT() {
        super("Q_TEXTREPORT",
                1,
                "Kvalifikátor pro hlášení potvrzení textu řidičem");

        // Add default listener for subvariables
        addDefaultSubvariableListener();

        initializeSubvariables();
    }

    /**
     * Adds a listener that will be automatically applied to all subvariables.
     *
     * @param listener the listener to add to all subvariables
     * @return this Q_TEXTREPORT instance for chaining
     */
    public Q_TEXTREPORT addSubvariableListener(BinaryChangeListener listener) {
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
        if (nid_c != null) nid_c.addBinaryChangeListener(listener);
        if (nid_rbc != null) nid_rbc.addBinaryChangeListener(listener);
        if (nid_textmessage != null) nid_textmessage.addBinaryChangeListener(listener);
    }

    /**
     * Removes a single listener from all current subvariables.
     *
     * @param listener the listener to remove
     */
    private void removeListenerFromAllSubvariables(BinaryChangeListener listener) {
        if (nid_c != null) nid_c.removeBinaryChangeListener(listener);
        if (nid_rbc != null) nid_rbc.removeBinaryChangeListener(listener);
        if (nid_textmessage != null) nid_textmessage.removeBinaryChangeListener(listener);
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
                    "Subvariable '%s' in Q_TEXTREPORT changed from '%s' to '%s'",
                    source.getName(), oldValue, newValue
            ));
        });
    }

    /**
     * Initializes all subvariables and applies listeners.
     */
    private void initializeSubvariables() {
        nid_c = new NID_C();
        nid_rbc = new NID_RBC();
        nid_textmessage = new NID_TEXTMESSAGE();

        // Apply all listeners to the newly created subvariables
        applyAllListenersToVariable(nid_c);
        applyAllListenersToVariable(nid_rbc);
        applyAllListenersToVariable(nid_textmessage);
    }

    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> s = new ArrayList<>();
        s.add("Není vyžadováno");
        s.add("Vyžaduje");
        return s;
    }

    @Override
    public Variables deepCopy() {
        Q_TEXTREPORT tmp = new Q_TEXTREPORT();

        // Copy listeners first
        tmp.subvariableListeners.addAll(this.subvariableListeners);

        tmp.setBinValue(getBinValue());

        // Create new subvariables and apply listeners
        tmp.nid_c = new NID_C();
        tmp.nid_rbc = new NID_RBC();
        tmp.nid_textmessage = new NID_TEXTMESSAGE();

        if (this.nid_c != null) {
            tmp.nid_c.setBinValue(this.nid_c.getBinValue());
        }
        if (this.nid_rbc != null) {
            tmp.nid_rbc.setBinValue(this.nid_rbc.getBinValue());
        }
        if (this.nid_textmessage != null) {
            tmp.nid_textmessage.setBinValue(this.nid_textmessage.getBinValue());
        }

        tmp.applyAllListenersToVariable(tmp.nid_c);
        tmp.applyAllListenersToVariable(tmp.nid_rbc);
        tmp.applyAllListenersToVariable(tmp.nid_textmessage);

        return tmp;
    }

    @Override
    public Component getComponent(String com) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel ini = ((JPanel) super.getComponent(com));
        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

        Component NID_C_comp = nid_textmessage.getComponent(com);
        Component NID_C_comp1 = nid_c.getComponent(com);
        Component NID_C_comp2 = nid_rbc.getComponent(com);

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox.getSelectedIndex() == 1) {
                    NID_C_comp.setVisible(true);
                    NID_C_comp1.setVisible(true);
                    NID_C_comp2.setVisible(true);
                } else {
                    NID_C_comp.setVisible(false);
                    NID_C_comp1.setVisible(false);
                    NID_C_comp2.setVisible(false);
                }
            }
        });

        if (jComboBox.getSelectedIndex() == 1) {
            NID_C_comp.setVisible(true);
            NID_C_comp1.setVisible(true);
            NID_C_comp2.setVisible(true);
        } else {
            NID_C_comp.setVisible(false);
            NID_C_comp1.setVisible(false);
            NID_C_comp2.setVisible(false);
        }

        panel.add(jComboBox);
        panel.add(NID_C_comp);
        panel.add(NID_C_comp1);
        panel.add(NID_C_comp2);

        return setTitle(panel, "Q_TEXTREPORT");
    }

    @Override
    public Variables initValueSet(String[] s) {
        // Recreate subvariables and apply listeners
        nid_c = new NID_C();
        nid_rbc = new NID_RBC();
        nid_textmessage = new NID_TEXTMESSAGE();
        applyAllListenersToVariable(nid_c);
        applyAllListenersToVariable(nid_rbc);
        applyAllListenersToVariable(nid_textmessage);

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() == 1) {
            nid_textmessage.initValueSet(s);
            nid_c.initValueSet(s);
            nid_rbc.initValueSet(s);
        }
        return this;
    }

    @Override
    public String getFullData() {
        String tmp = getBinValue();

        if (getDecValue() == 1) {
            tmp += nid_textmessage.getFullData();
            tmp += nid_c.getFullData();
            tmp += nid_rbc.getFullData();
        }
        return tmp;
    }

    @Override
    public String getSimpleView() {
        String tmp = super.getSimpleView();

        if (getDecValue() == 1) {
            tmp += nid_textmessage.getSimpleView();
            tmp += nid_c.getSimpleView();
            tmp += nid_rbc.getSimpleView();
        }
        return tmp;
    }

    /**
     * Gets all subvariables as a list for convenient iteration.
     *
     * @return a list containing all subvariables
     */
    public List<Variables> getAllSubvariables() {
        List<Variables> subvariables = new ArrayList<>();
        if (nid_c != null) subvariables.add(nid_c);
        if (nid_rbc != null) subvariables.add(nid_rbc);
        if (nid_textmessage != null) subvariables.add(nid_textmessage);
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

    // Getters for the subvariables (useful for testing and specific access)
    public NID_C getNid_c() {
        return nid_c;
    }

    public NID_RBC getNid_rbc() {
        return nid_rbc;
    }

    public NID_TEXTMESSAGE getNid_textmessage() {
        return nid_textmessage;
    }
}