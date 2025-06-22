package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.D.D_SECTIONTIMERSTOPLOC;
import packets.Var.T.T_SECTIONTIMER;
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

public class Q_SECTIONTIMER extends Variables {
    T_SECTIONTIMER t_sectiontimer;
    D_SECTIONTIMERSTOPLOC d_sectiontimerstoploc;

    /** List of listeners that will be applied to all subvariables */
    private final List<BinaryChangeListener> subvariableListeners = new CopyOnWriteArrayList<>();

    public Q_SECTIONTIMER() {
        super("Q_SECTIONTIMER",
                1,
                "Kvalifikátor označující, zda existuje časový limit oddílu související s oddílem");

        // Add default listener for subvariables
        addDefaultSubvariableListener();

        initializeSubvariables();
    }

    /**
     * Adds a listener that will be automatically applied to all subvariables.
     */
    public Q_SECTIONTIMER addSubvariableListener(BinaryChangeListener listener) {
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
        if (t_sectiontimer != null) t_sectiontimer.addBinaryChangeListener(listener);
        if (d_sectiontimerstoploc != null) d_sectiontimerstoploc.addBinaryChangeListener(listener);
    }

    private void removeListenerFromAllSubvariables(BinaryChangeListener listener) {
        if (t_sectiontimer != null) t_sectiontimer.removeBinaryChangeListener(listener);
        if (d_sectiontimerstoploc != null) d_sectiontimerstoploc.removeBinaryChangeListener(listener);
    }

    private void applyAllListenersToVariable(Variables variable) {
        for (BinaryChangeListener listener : subvariableListeners) {
            variable.addBinaryChangeListener(listener);
        }
    }

    private void addDefaultSubvariableListener() {
        addSubvariableListener((source, oldValue, newValue) -> {
            System.out.println(String.format(
                    "Subvariable '%s' in Q_SECTIONTIMER changed from '%s' to '%s'",
                    source.getName(), oldValue, newValue
            ));
        });
    }

    private void initializeSubvariables() {
        t_sectiontimer = new T_SECTIONTIMER();
        d_sectiontimerstoploc = new D_SECTIONTIMERSTOPLOC();

        // Apply all listeners to the newly created subvariables
        applyAllListenersToVariable(t_sectiontimer);
        applyAllListenersToVariable(d_sectiontimerstoploc);
    }

    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> s = new ArrayList<>();
        s.add("Není");
        s.add("Je");
        return s;
    }

    @Override
    public Component getComponent(String com) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel ini = ((JPanel) super.getComponent(com));
        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

        Component add = panel.add(t_sectiontimer.getComponent());
        Component add1 = panel.add(d_sectiontimerstoploc.getComponent());

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox.getSelectedIndex() == 1) {
                    add.setVisible(true);
                    add1.setVisible(true);
                } else {
                    add.setVisible(false);
                    add1.setVisible(false);
                }
                panel.updateUI();
            }
        });

        if (getDecValue() == 1) {
            add.setVisible(true);
            add1.setVisible(true);
        } else {
            add.setVisible(false);
            add1.setVisible(false);
        }

        panel.add(jComboBox);
        panel.add(add);
        panel.add(add1);

        return setTitle(panel, "Q_SECTIONTIMER");
    }

    @Override
    public Variables initValueSet(String[] s) {
        // Recreate subvariables and apply listeners
        t_sectiontimer = new T_SECTIONTIMER();
        d_sectiontimerstoploc = new D_SECTIONTIMERSTOPLOC();
        applyAllListenersToVariable(t_sectiontimer);
        applyAllListenersToVariable(d_sectiontimerstoploc);

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() != 0) {
            t_sectiontimer.initValueSet(s);
            d_sectiontimerstoploc.initValueSet(s);
        }

        return this;
    }

    @Override
    public Variables deepCopy() {
        Q_SECTIONTIMER tmp = new Q_SECTIONTIMER();

        // Copy listeners first
        tmp.subvariableListeners.addAll(this.subvariableListeners);

        tmp.setBinValue(getBinValue());

        // Create new subvariables and apply listeners
        tmp.t_sectiontimer = new T_SECTIONTIMER();
        tmp.d_sectiontimerstoploc = new D_SECTIONTIMERSTOPLOC();

        if (this.t_sectiontimer != null) {
            tmp.t_sectiontimer.setBinValue(this.t_sectiontimer.getBinValue());
        }
        if (this.d_sectiontimerstoploc != null) {
            tmp.d_sectiontimerstoploc.setBinValue(this.d_sectiontimerstoploc.getBinValue());
        }

        tmp.applyAllListenersToVariable(tmp.t_sectiontimer);
        tmp.applyAllListenersToVariable(tmp.d_sectiontimerstoploc);

        return tmp;
    }

    @Override
    public String getFullData() {
        if (getDecValue() == 0)
            return getBinValue();

        String tmp = "";
        tmp += getBinValue();
        tmp += t_sectiontimer.getFullData();
        tmp += d_sectiontimerstoploc.getFullData();

        return tmp;
    }

    @Override
    public String getSimpleView() {
        if (getDecValue() == 0)
            return super.getSimpleView();

        String tmp = "";
        tmp += super.getSimpleView();
        tmp += t_sectiontimer.getSimpleView();
        tmp += d_sectiontimerstoploc.getSimpleView();

        return tmp;
    }

    /**
     * Gets all subvariables as a list for convenient iteration.
     */
    public List<Variables> getAllSubvariables() {
        List<Variables> subvariables = new ArrayList<>();
        if (t_sectiontimer != null) subvariables.add(t_sectiontimer);
        if (d_sectiontimerstoploc != null) subvariables.add(d_sectiontimerstoploc);
        return subvariables;
    }

    public int getSubvariableListenerCount() {
        return subvariableListeners.size();
    }

    public T_SECTIONTIMER getT_sectiontimer() {
        return t_sectiontimer;
    }

    public D_SECTIONTIMERSTOPLOC getD_sectiontimerstoploc() {
        return d_sectiontimerstoploc;
    }
}