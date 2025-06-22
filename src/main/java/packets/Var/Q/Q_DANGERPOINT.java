package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.D.D_DP;
import packets.Var.V.V_RELEASEDP;
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

public class Q_DANGERPOINT extends Variables {
    D_DP d_dp;
    V_RELEASEDP v_releasedp;

    /** List of listeners that will be applied to all subvariables */
    private final List<BinaryChangeListener> subvariableListeners = new CopyOnWriteArrayList<>();

    public Q_DANGERPOINT() {
        super("Q_DANGERPOINT",
                1,
                "This variable is set to 1 if either a danger point exists or a release speed has to be specified");

        // Add default listener for subvariables
        addDefaultSubvariableListener();

        initializeSubvariables();
    }

    /**
     * Adds a listener that will be automatically applied to all subvariables.
     */
    public Q_DANGERPOINT addSubvariableListener(BinaryChangeListener listener) {
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
        if (d_dp != null) d_dp.addBinaryChangeListener(listener);
        if (v_releasedp != null) v_releasedp.addBinaryChangeListener(listener);
    }

    private void removeListenerFromAllSubvariables(BinaryChangeListener listener) {
        if (d_dp != null) d_dp.removeBinaryChangeListener(listener);
        if (v_releasedp != null) v_releasedp.removeBinaryChangeListener(listener);
    }

    private void applyAllListenersToVariable(Variables variable) {
        for (BinaryChangeListener listener : subvariableListeners) {
            variable.addBinaryChangeListener(listener);
        }
    }

    private void addDefaultSubvariableListener() {
        addSubvariableListener((source, oldValue, newValue) -> {
            System.out.println(String.format(
                    "Subvariable '%s' in Q_DANGERPOINT changed from '%s' to '%s'",
                    source.getName(), oldValue, newValue
            ));
        });
    }

    private void initializeSubvariables() {
        d_dp = new D_DP();
        v_releasedp = new V_RELEASEDP();

        // Apply all listeners to the newly created subvariables
        applyAllListenersToVariable(d_dp);
        applyAllListenersToVariable(v_releasedp);
    }

    @Override
    public Component getComponent(String com) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel ini = ((JPanel) super.getComponent(com));
        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

        Component add = panel.add(d_dp.getComponent());
        Component add1 = panel.add(v_releasedp.getComponent());

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

        return setTitle(panel, "Q_DANGERPOINT");
    }

    @Override
    public Variables initValueSet(String[] s) {
        // Recreate subvariables and apply listeners
        d_dp = new D_DP();
        v_releasedp = new V_RELEASEDP();
        applyAllListenersToVariable(d_dp);
        applyAllListenersToVariable(v_releasedp);

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() != 0) {
            d_dp.initValueSet(s);
            v_releasedp.initValueSet(s);
        }

        return this;
    }

    @Override
    public Variables deepCopy() {
        Q_DANGERPOINT tmp = new Q_DANGERPOINT();

        // Copy listeners first
        tmp.subvariableListeners.addAll(this.subvariableListeners);

        tmp.setBinValue(getBinValue());

        // Create new subvariables and apply listeners
        tmp.d_dp = new D_DP();
        tmp.v_releasedp = new V_RELEASEDP();

        if (this.d_dp != null) {
            tmp.d_dp.setBinValue(this.d_dp.getBinValue());
        }
        if (this.v_releasedp != null) {
            tmp.v_releasedp.setBinValue(this.v_releasedp.getBinValue());
        }

        tmp.applyAllListenersToVariable(tmp.d_dp);
        tmp.applyAllListenersToVariable(tmp.v_releasedp);

        return tmp;
    }

    @Override
    public String getFullData() {
        if (getDecValue() == 0)
            return getBinValue();

        String tmp = "";
        tmp += getBinValue();
        tmp += d_dp.getFullData();
        tmp += v_releasedp.getFullData();

        return tmp;
    }

    @Override
    public String getSimpleView() {
        if (getDecValue() == 0)
            return super.getSimpleView();

        String tmp = "";
        tmp += super.getSimpleView();
        tmp += d_dp.getSimpleView();
        tmp += v_releasedp.getSimpleView();

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
        if (d_dp != null) subvariables.add(d_dp);
        if (v_releasedp != null) subvariables.add(v_releasedp);
        return subvariables;
    }

    public int getSubvariableListenerCount() {
        return subvariableListeners.size();
    }

    public D_DP getD_dp() {
        return d_dp;
    }

    public V_RELEASEDP getV_releasedp() {
        return v_releasedp;
    }
}