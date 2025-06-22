package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.NID.NID_C;
import packets.Var.NID.NID_VBCMK;
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

public class Q_VBCO extends Variables {
    private NID_C nid_c;
    private NID_VBCMK nid_vbcmk;
    private T_VBC t_vbc;

    /** List of listeners that will be applied to all subvariables */
    private final List<BinaryChangeListener> subvariableListeners = new CopyOnWriteArrayList<>();

    public Q_VBCO() {
        super("Q_VBCO",
                1,
                "Kvalifikátor pro nastavení nebo odebrání VBC");

        // Add default listener for subvariables
        addDefaultSubvariableListener();

        initializeSubvariables();
    }

    /**
     * Adds a listener that will be automatically applied to all subvariables.
     */
    public Q_VBCO addSubvariableListener(BinaryChangeListener listener) {
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
        if (nid_vbcmk != null) nid_vbcmk.addBinaryChangeListener(listener);
        if (t_vbc != null) t_vbc.addBinaryChangeListener(listener);
    }

    private void removeListenerFromAllSubvariables(BinaryChangeListener listener) {
        if (nid_c != null) nid_c.removeBinaryChangeListener(listener);
        if (nid_vbcmk != null) nid_vbcmk.removeBinaryChangeListener(listener);
        if (t_vbc != null) t_vbc.removeBinaryChangeListener(listener);
    }

    private void applyAllListenersToVariable(Variables variable) {
        for (BinaryChangeListener listener : subvariableListeners) {
            variable.addBinaryChangeListener(listener);
        }
    }

    private void addDefaultSubvariableListener() {
        addSubvariableListener((source, oldValue, newValue) -> {
            System.out.println(String.format(
                    "Subvariable '%s' in Q_VBCO changed from '%s' to '%s'",
                    source.getName(), oldValue, newValue
            ));
        });
    }

    private void initializeSubvariables() {
        nid_c = new NID_C();
        nid_vbcmk = new NID_VBCMK();
        t_vbc = new T_VBC();

        // Apply all listeners to the newly created subvariables
        applyAllListenersToVariable(nid_c);
        applyAllListenersToVariable(nid_vbcmk);
        applyAllListenersToVariable(t_vbc);
    }

    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> s = new ArrayList<>();
        s.add("Zrušit");
        s.add("Nastavit");
        return s;
    }

    @Override
    public Variables deepCopy() {
        Q_VBCO tmp = new Q_VBCO();

        // Copy listeners first
        tmp.subvariableListeners.addAll(this.subvariableListeners);

        tmp.setBinValue(getBinValue());

        // Create new subvariables and apply listeners
        tmp.nid_c = new NID_C();
        tmp.nid_vbcmk = new NID_VBCMK();
        tmp.t_vbc = new T_VBC();

        if (this.nid_c != null) {
            tmp.nid_c.setBinValue(this.nid_c.getBinValue());
        }
        if (this.nid_vbcmk != null) {
            tmp.nid_vbcmk.setBinValue(this.nid_vbcmk.getBinValue());
        }
        if (this.t_vbc != null) {
            tmp.t_vbc.setBinValue(this.t_vbc.getBinValue());
        }

        tmp.applyAllListenersToVariable(tmp.nid_c);
        tmp.applyAllListenersToVariable(tmp.nid_vbcmk);
        tmp.applyAllListenersToVariable(tmp.t_vbc);

        return tmp;
    }

    @Override
    public Component getComponent(String com) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel ini = ((JPanel) super.getComponent(com));
        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

        Component NID_C_comp = nid_vbcmk.getComponent(com);
        Component NID_C_comp1 = nid_c.getComponent(com);
        Component NID_C_comp2 = t_vbc.getComponent(com);

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox.getSelectedIndex() == 1) {
                    NID_C_comp2.setVisible(true);
                } else {
                    NID_C_comp2.setVisible(false);
                }
            }
        });

        if (jComboBox.getSelectedIndex() == 1) {
            NID_C_comp2.setVisible(true);
        } else {
            NID_C_comp2.setVisible(false);
        }

        panel.add(jComboBox);
        panel.add(NID_C_comp);
        panel.add(NID_C_comp1);
        panel.add(NID_C_comp2);

        return setTitle(panel, getName());
    }

    @Override
    public Variables initValueSet(String[] s) {
        // Recreate subvariables and apply listeners
        nid_c = new NID_C();
        nid_vbcmk = new NID_VBCMK();
        t_vbc = new T_VBC();
        applyAllListenersToVariable(nid_c);
        applyAllListenersToVariable(nid_vbcmk);
        applyAllListenersToVariable(t_vbc);

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));
        nid_vbcmk.initValueSet(s);
        nid_c.initValueSet(s);

        if (getDecValue() == 1) {
            t_vbc.initValueSet(s);
        }
        return this;
    }

    @Override
    public String getFullData() {
        String tmp = getBinValue();
        tmp += nid_vbcmk.getFullData();
        tmp += nid_c.getFullData();
        if (getDecValue() == 1) {
            tmp += t_vbc.getFullData();
        }
        return tmp;
    }

    @Override
    public String getSimpleView() {
        String tmp = super.getSimpleView();
        tmp += nid_vbcmk.getSimpleView();
        tmp += nid_c.getSimpleView();
        if (getDecValue() == 1) {
            tmp += t_vbc.getSimpleView();
        }
        return tmp;
    }

    /**
     * Gets all subvariables as a list for convenient iteration.
     */
    public List<Variables> getAllSubvariables() {
        List<Variables> subvariables = new ArrayList<>();
        if (nid_c != null) subvariables.add(nid_c);
        if (nid_vbcmk != null) subvariables.add(nid_vbcmk);
        if (t_vbc != null) subvariables.add(t_vbc);
        return subvariables;
    }

    public int getSubvariableListenerCount() {
        return subvariableListeners.size();
    }

    public NID_C getNid_c() {
        return nid_c;
    }

    public NID_VBCMK getNid_vbcmk() {
        return nid_vbcmk;
    }

    public T_VBC getT_vbc() {
        return t_vbc;
    }
}