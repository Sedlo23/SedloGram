package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.T.T_LSSMA;
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

public class Q_LSSMA extends Variables {
    private T_LSSMA nid_c;

    /** List of listeners that will be applied to all subvariables */
    private final List<BinaryChangeListener> subvariableListeners = new CopyOnWriteArrayList<>();

    public Q_LSSMA() {
        super("Q_LSSMA",
                1,
                "Tento kvalifikátor určuje, zda má palubní počítač zapnout/vypnout zobrazení nejnižší kontrolované rychlosti v rámci MA.");

        // Add default listener for subvariables
        addDefaultSubvariableListener();

        initializeSubvariables();
    }

    /**
     * Adds a listener that will be automatically applied to all subvariables.
     */
    public Q_LSSMA addSubvariableListener(BinaryChangeListener listener) {
        if (listener != null) {
            subvariableListeners.add(listener);
            applyListenerToAllSubvariables(listener);
        }
        return this;
    }

    /**
     * Removes a listener from all subvariables and from the subvariable listeners list.
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

    private void applyListenerToAllSubvariables(BinaryChangeListener listener) {
        if (nid_c != null) nid_c.addBinaryChangeListener(listener);
    }

    private void removeListenerFromAllSubvariables(BinaryChangeListener listener) {
        if (nid_c != null) nid_c.removeBinaryChangeListener(listener);
    }

    private void applyAllListenersToVariable(Variables variable) {
        for (BinaryChangeListener listener : subvariableListeners) {
            variable.addBinaryChangeListener(listener);
        }
    }

    private void addDefaultSubvariableListener() {
        addSubvariableListener((source, oldValue, newValue) -> {
            System.out.println(String.format(
                    "Subvariable '%s' in Q_LSSMA changed from '%s' to '%s'",
                    source.getName(), oldValue, newValue
            ));
        });
    }

    private void initializeSubvariables() {
        nid_c = new T_LSSMA();

        // Apply all listeners to the newly created subvariable
        applyAllListenersToVariable(nid_c);
    }

    @Override
    public Variables deepCopy() {
        Q_LSSMA tmp = new Q_LSSMA();

        // Copy listeners first
        tmp.subvariableListeners.addAll(this.subvariableListeners);

        tmp.setBinValue(getBinValue());

        // Create new subvariable and apply listeners
        tmp.nid_c = new T_LSSMA();
        if (this.nid_c != null) {
            tmp.nid_c.setBinValue(this.nid_c.getBinValue());
        }
        tmp.applyAllListenersToVariable(tmp.nid_c);

        return tmp;
    }

    @Override
    public Component getComponent(String com) {
        JPanel panel = new JPanel();

        JPanel ini = ((JPanel) super.getComponent(com));
        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

        Component NID_C_comp = ((JPanel)nid_c.getComponent(com)).getComponent(1);

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox.getSelectedIndex() == 1)
                    NID_C_comp.setVisible(true);
                else
                    NID_C_comp.setVisible(false);
            }
        });

        if (jComboBox.getSelectedIndex() == 1)
            NID_C_comp.setVisible(true);
        else
            NID_C_comp.setVisible(false);

        panel.add(jComboBox);
        panel.add(NID_C_comp);

        return setTitle(panel, getName());
    }

    @Override
    public Variables initValueSet(String[] s) {
        // Recreate subvariable and apply listeners
        nid_c = new T_LSSMA();
        applyAllListenersToVariable(nid_c);

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() == 1)
            nid_c.initValueSet(s);

        return this;
    }

    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> s = new ArrayList<>();
        s.add("Vypnout");
        s.add("Zapnout");
        return s;
    }

    @Override
    public String getFullData() {
        String tmp = getBinValue();

        if (getDecValue() == 1)
            tmp += nid_c.getFullData();

        return tmp;
    }

    @Override
    public String getSimpleView() {
        String tmp = super.getSimpleView();

        if (getDecValue() == 1)
            tmp += nid_c.getSimpleView();

        return tmp;
    }

    /**
     * Gets all subvariables as a list for convenient iteration.
     */
    public List<Variables> getAllSubvariables() {
        List<Variables> subvariables = new ArrayList<>();
        if (nid_c != null) subvariables.add(nid_c);
        return subvariables;
    }

    public int getSubvariableListenerCount() {
        return subvariableListeners.size();
    }

    public T_LSSMA getNid_c() {
        return nid_c;
    }
}