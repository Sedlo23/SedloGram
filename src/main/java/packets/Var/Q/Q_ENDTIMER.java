package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.D.D_ENDTIMERSTARTLOC;
import packets.Var.T.T_ENDTIMER;
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

public class Q_ENDTIMER extends Variables {
    T_ENDTIMER t_endtimer;
    D_ENDTIMERSTARTLOC d_endtimerstartloc;

    /** List of listeners that will be applied to all subvariables */
    private final List<BinaryChangeListener> subvariableListeners = new CopyOnWriteArrayList<>();

    public Q_ENDTIMER() {
        super("Q_ENDTIMER",
                1,
                "Kvalifikátor označující, zda pro sekci End v MA existují informace o časovači koncové sekce.");

        // Add default listener for subvariables
        addDefaultSubvariableListener();

        initializeSubvariables();
    }

    /**
     * Adds a listener that will be automatically applied to all subvariables.
     */
    public Q_ENDTIMER addSubvariableListener(BinaryChangeListener listener) {
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
        if (t_endtimer != null) t_endtimer.addBinaryChangeListener(listener);
        if (d_endtimerstartloc != null) d_endtimerstartloc.addBinaryChangeListener(listener);
    }

    private void removeListenerFromAllSubvariables(BinaryChangeListener listener) {
        if (t_endtimer != null) t_endtimer.removeBinaryChangeListener(listener);
        if (d_endtimerstartloc != null) d_endtimerstartloc.removeBinaryChangeListener(listener);
    }

    private void applyAllListenersToVariable(Variables variable) {
        for (BinaryChangeListener listener : subvariableListeners) {
            variable.addBinaryChangeListener(listener);
        }
    }

    private void addDefaultSubvariableListener() {
        addSubvariableListener((source, oldValue, newValue) -> {
            System.out.println(String.format(
                    "Subvariable '%s' in Q_ENDTIMER changed from '%s' to '%s'",
                    source.getName(), oldValue, newValue
            ));
        });
    }

    private void initializeSubvariables() {
        t_endtimer = new T_ENDTIMER();
        d_endtimerstartloc = new D_ENDTIMERSTARTLOC();

        // Apply all listeners to the newly created subvariables
        applyAllListenersToVariable(t_endtimer);
        applyAllListenersToVariable(d_endtimerstartloc);
    }

    @Override
    public Component getComponent(String com) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel ini = ((JPanel) super.getComponent(com));
        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

        Component add = panel.add(t_endtimer.getComponent());
        Component add1 = panel.add(d_endtimerstartloc.getComponent());

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

        return setTitle(panel, "Q_ENDTIMER");
    }

    @Override
    public Variables initValueSet(String[] s) {
        // Recreate subvariables and apply listeners
        t_endtimer = new T_ENDTIMER();
        d_endtimerstartloc = new D_ENDTIMERSTARTLOC();
        applyAllListenersToVariable(t_endtimer);
        applyAllListenersToVariable(d_endtimerstartloc);

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() != 0) {
            t_endtimer.initValueSet(s);
            d_endtimerstartloc.initValueSet(s);
        }

        return this;
    }

    @Override
    public Variables deepCopy() {
        Q_ENDTIMER tmp = new Q_ENDTIMER();

        // Copy listeners first
        tmp.subvariableListeners.addAll(this.subvariableListeners);

        tmp.setBinValue(getBinValue());

        // Create new subvariables and apply listeners
        tmp.t_endtimer = new T_ENDTIMER();
        tmp.d_endtimerstartloc = new D_ENDTIMERSTARTLOC();

        if (this.t_endtimer != null) {
            tmp.t_endtimer.setBinValue(this.t_endtimer.getBinValue());
        }
        if (this.d_endtimerstartloc != null) {
            tmp.d_endtimerstartloc.setBinValue(this.d_endtimerstartloc.getBinValue());
        }

        tmp.applyAllListenersToVariable(tmp.t_endtimer);
        tmp.applyAllListenersToVariable(tmp.d_endtimerstartloc);

        return tmp;
    }

    @Override
    public String getFullData() {
        if (getDecValue() == 0)
            return getBinValue();

        String tmp = "";
        tmp += getBinValue();
        tmp += t_endtimer.getFullData();
        tmp += d_endtimerstartloc.getFullData();

        return tmp;
    }

    @Override
    public String getSimpleView() {
        if (getDecValue() == 0)
            return super.getSimpleView();

        String tmp = "";
        tmp += super.getSimpleView();
        tmp += t_endtimer.getSimpleView();
        tmp += d_endtimerstartloc.getSimpleView();

        return tmp;
    }

    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> s = new ArrayList<>();
        s.add("Ne");
        s.add("Ano");
        return s;
    }

    /**
     * Gets all subvariables as a list for convenient iteration.
     */
    public List<Variables> getAllSubvariables() {
        List<Variables> subvariables = new ArrayList<>();
        if (t_endtimer != null) subvariables.add(t_endtimer);
        if (d_endtimerstartloc != null) subvariables.add(d_endtimerstartloc);
        return subvariables;
    }

    public int getSubvariableListenerCount() {
        return subvariableListeners.size();
    }

    public T_ENDTIMER getT_endtimer() {
        return t_endtimer;
    }

    public D_ENDTIMERSTARTLOC getD_endtimerstartloc() {
        return d_endtimerstartloc;
    }
}