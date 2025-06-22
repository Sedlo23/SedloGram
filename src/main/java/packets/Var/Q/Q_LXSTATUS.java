package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.V.V_LX;
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

public class Q_LXSTATUS extends Variables {
    private V_LX nid_c;
    private Q_STOPLX nid_c2;

    /** List of listeners that will be applied to all subvariables */
    private final List<BinaryChangeListener> subvariableListeners = new CopyOnWriteArrayList<>();

    public Q_LXSTATUS() {
        super("Q_LXSTATUS",
                1, "Indicates whether the LX is protected or not"
        );

        // Add default listener for subvariables
        addDefaultSubvariableListener();

        initializeSubvariables();
    }

    /**
     * Adds a listener that will be automatically applied to all subvariables.
     */
    public Q_LXSTATUS addSubvariableListener(BinaryChangeListener listener) {
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
        if (nid_c2 != null) nid_c2.addBinaryChangeListener(listener);
    }

    private void removeListenerFromAllSubvariables(BinaryChangeListener listener) {
        if (nid_c != null) nid_c.removeBinaryChangeListener(listener);
        if (nid_c2 != null) nid_c2.removeBinaryChangeListener(listener);
    }

    private void applyAllListenersToVariable(Variables variable) {
        for (BinaryChangeListener listener : subvariableListeners) {
            variable.addBinaryChangeListener(listener);
        }
    }

    private void addDefaultSubvariableListener() {
        addSubvariableListener((source, oldValue, newValue) -> {
            System.out.println(String.format(
                    "Subvariable '%s' in Q_LXSTATUS changed from '%s' to '%s'",
                    source.getName(), oldValue, newValue
            ));
        });
    }

    private void initializeSubvariables() {
        nid_c = new V_LX();
        nid_c2 = new Q_STOPLX();

        // Apply all listeners to the newly created subvariables
        applyAllListenersToVariable(nid_c);
        applyAllListenersToVariable(nid_c2);
    }

    @Override
    public Variables deepCopy() {
        Q_LXSTATUS tmp = new Q_LXSTATUS();

        // Copy listeners first
        tmp.subvariableListeners.addAll(this.subvariableListeners);

        tmp.setBinValue(getBinValue());

        // Create new subvariables and apply listeners
        tmp.nid_c = new V_LX();
        tmp.nid_c2 = new Q_STOPLX();

        if (this.nid_c != null) {
            tmp.nid_c.setBinValue(this.nid_c.getBinValue());
        }
        if (this.nid_c2 != null) {
            tmp.nid_c2.setBinValue(this.nid_c2.getBinValue());
        }

        tmp.applyAllListenersToVariable(tmp.nid_c);
        tmp.applyAllListenersToVariable(tmp.nid_c2);

        return tmp;
    }

    @Override
    public Component getComponent(String com) {
        JPanel panel = new JPanel();

        JPanel ini = ((JPanel) super.getComponent(com));
        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

        Component NID_C_comp = nid_c.getComponent(com);
        Component NID_C_comp2 = nid_c2.getComponent(com);

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox.getSelectedIndex() == 1) {
                    NID_C_comp.setVisible(true);
                    NID_C_comp2.setVisible(true);
                } else {
                    NID_C_comp.setVisible(false);
                    NID_C_comp2.setVisible(false);
                }
            }
        });

        if (jComboBox.getSelectedIndex() == 1) {
            NID_C_comp.setVisible(true);
            NID_C_comp2.setVisible(true);
        } else {
            NID_C_comp.setVisible(false);
            NID_C_comp2.setVisible(false);
        }

        panel.add(jComboBox);
        panel.add(NID_C_comp);
        panel.add(NID_C_comp2);

        return setTitle(panel, "Q_LXSTATUS");
    }

    @Override
    public Variables initValueSet(String[] s) {
        // Recreate subvariables and apply listeners
        nid_c = new V_LX();
        nid_c2 = new Q_STOPLX();
        applyAllListenersToVariable(nid_c);
        applyAllListenersToVariable(nid_c2);

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() == 1) {
            nid_c.initValueSet(s);
            nid_c2.initValueSet(s);
        }

        return this;
    }

    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> s = new ArrayList<>();
        s.add("Zabeznečený");
        s.add("Nezabeznečený");
        return s;
    }

    @Override
    public String getFullData() {
        String tmp = getBinValue();

        if (getDecValue() == 1) {
            tmp += nid_c.getFullData();
            tmp += nid_c2.getFullData();
        }

        return tmp;
    }

    @Override
    public String getSimpleView() {
        String tmp = super.getSimpleView();

        if (getDecValue() == 1) {
            tmp += nid_c.getSimpleView();
            tmp += nid_c2.getSimpleView();
        }

        return tmp;
    }

    /**
     * Gets all subvariables as a list for convenient iteration.
     */
    public List<Variables> getAllSubvariables() {
        List<Variables> subvariables = new ArrayList<>();
        if (nid_c != null) subvariables.add(nid_c);
        if (nid_c2 != null) subvariables.add(nid_c2);
        return subvariables;
    }

    public int getSubvariableListenerCount() {
        return subvariableListeners.size();
    }

    public V_LX getNid_c() {
        return nid_c;
    }

    public Q_STOPLX getNid_c2() {
        return nid_c2;
    }
}