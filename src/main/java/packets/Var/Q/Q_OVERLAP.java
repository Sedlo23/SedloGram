package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.D.D_OL;
import packets.Var.D.D_STARTOL;
import packets.Var.T.T_OL;
import packets.Var.V.V_RELEASEOL;
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

public class Q_OVERLAP extends Variables {
    D_STARTOL d_startol;
    T_OL t_ol;
    D_OL d_ol;
    V_RELEASEOL v_releaseol;

    /** List of listeners that will be applied to all subvariables */
    private final List<BinaryChangeListener> subvariableListeners = new CopyOnWriteArrayList<>();

    public Q_OVERLAP() {
        super("Q_OVERLAP",
                1,
                "This variable is set to 1 if either an overlap exists or a release speed has to be specified");

        // Add default listener for subvariables
        addDefaultSubvariableListener();

        initializeSubvariables();
    }

    /**
     * Adds a listener that will be automatically applied to all subvariables.
     */
    public Q_OVERLAP addSubvariableListener(BinaryChangeListener listener) {
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
        if (d_startol != null) d_startol.addBinaryChangeListener(listener);
        if (t_ol != null) t_ol.addBinaryChangeListener(listener);
        if (d_ol != null) d_ol.addBinaryChangeListener(listener);
        if (v_releaseol != null) v_releaseol.addBinaryChangeListener(listener);
    }

    private void removeListenerFromAllSubvariables(BinaryChangeListener listener) {
        if (d_startol != null) d_startol.removeBinaryChangeListener(listener);
        if (t_ol != null) t_ol.removeBinaryChangeListener(listener);
        if (d_ol != null) d_ol.removeBinaryChangeListener(listener);
        if (v_releaseol != null) v_releaseol.removeBinaryChangeListener(listener);
    }

    private void applyAllListenersToVariable(Variables variable) {
        for (BinaryChangeListener listener : subvariableListeners) {
            variable.addBinaryChangeListener(listener);
        }
    }

    private void addDefaultSubvariableListener() {
        addSubvariableListener((source, oldValue, newValue) -> {
            System.out.println(String.format(
                    "Subvariable '%s' in Q_OVERLAP changed from '%s' to '%s'",
                    source.getName(), oldValue, newValue
            ));
        });
    }

    private void initializeSubvariables() {
        d_startol = new D_STARTOL();
        t_ol = new T_OL();
        d_ol = new D_OL();
        v_releaseol = new V_RELEASEOL();

        // Apply all listeners to the newly created subvariables
        applyAllListenersToVariable(d_startol);
        applyAllListenersToVariable(t_ol);
        applyAllListenersToVariable(d_ol);
        applyAllListenersToVariable(v_releaseol);
    }

    @Override
    public Component getComponent(String com) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel ini = ((JPanel) super.getComponent(com));
        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

        Component add = panel.add(d_startol.getComponent());
        Component add1 = panel.add(t_ol.getComponent());
        Component add2 = panel.add(d_ol.getComponent());
        Component add3 = panel.add(v_releaseol.getComponent());

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox.getSelectedIndex() == 1) {
                    add.setVisible(true);
                    add1.setVisible(true);
                    add2.setVisible(true);
                    add3.setVisible(true);
                } else {
                    add.setVisible(false);
                    add1.setVisible(false);
                    add2.setVisible(false);
                    add3.setVisible(false);
                }
                panel.updateUI();
            }
        });

        if (getDecValue() == 1) {
            add.setVisible(true);
            add1.setVisible(true);
            add2.setVisible(true);
            add3.setVisible(true);
        } else {
            add.setVisible(false);
            add1.setVisible(false);
            add2.setVisible(false);
            add3.setVisible(false);
        }

        panel.add(jComboBox);
        panel.add(add);
        panel.add(add1);
        panel.add(add2);
        panel.add(add3);

        return setTitle(panel, "Q_OVERLAP");
    }

    @Override
    public Variables initValueSet(String[] s) {
        // Recreate subvariables and apply listeners
        d_startol = new D_STARTOL();
        t_ol = new T_OL();
        d_ol = new D_OL();
        v_releaseol = new V_RELEASEOL();
        applyAllListenersToVariable(d_startol);
        applyAllListenersToVariable(t_ol);
        applyAllListenersToVariable(d_ol);
        applyAllListenersToVariable(v_releaseol);

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() != 0) {
            d_startol.initValueSet(s);
            t_ol.initValueSet(s);
            d_ol.initValueSet(s);
            v_releaseol.initValueSet(s);
        }

        return this;
    }

    @Override
    public Variables deepCopy() {
        Q_OVERLAP tmp = new Q_OVERLAP();

        // Copy listeners first
        tmp.subvariableListeners.addAll(this.subvariableListeners);

        tmp.setBinValue(getBinValue());

        // Create new subvariables and apply listeners
        tmp.d_startol = new D_STARTOL();
        tmp.t_ol = new T_OL();
        tmp.d_ol = new D_OL();
        tmp.v_releaseol = new V_RELEASEOL();

        if (this.d_startol != null) {
            tmp.d_startol.setBinValue(this.d_startol.getBinValue());
        }
        if (this.t_ol != null) {
            tmp.t_ol.setBinValue(this.t_ol.getBinValue());
        }
        if (this.d_ol != null) {
            tmp.d_ol.setBinValue(this.d_ol.getBinValue());
        }
        if (this.v_releaseol != null) {
            tmp.v_releaseol.setBinValue(this.v_releaseol.getBinValue());
        }

        tmp.applyAllListenersToVariable(tmp.d_startol);
        tmp.applyAllListenersToVariable(tmp.t_ol);
        tmp.applyAllListenersToVariable(tmp.d_ol);
        tmp.applyAllListenersToVariable(tmp.v_releaseol);

        return tmp;
    }

    @Override
    public String getFullData() {
        if (getDecValue() == 0)
            return getBinValue();

        String tmp = "";
        tmp += getBinValue();
        tmp += d_startol.getFullData();
        tmp += t_ol.getFullData();
        tmp += d_ol.getFullData();
        tmp += v_releaseol.getFullData();

        return tmp;
    }

    @Override
    public String getSimpleView() {
        if (getDecValue() == 0)
            return super.getSimpleView();

        String tmp = "";
        tmp += super.getSimpleView();
        tmp += d_startol.getSimpleView();
        tmp += t_ol.getSimpleView();
        tmp += d_ol.getSimpleView();
        tmp += v_releaseol.getSimpleView();

        return tmp;
    }

    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> s = new ArrayList<>();
        s.add("Není");
        s.add("Je");
        return s;
    }

    /**
     * Gets all subvariables as a list for convenient iteration.
     */
    public List<Variables> getAllSubvariables() {
        List<Variables> subvariables = new ArrayList<>();
        if (d_startol != null) subvariables.add(d_startol);
        if (t_ol != null) subvariables.add(t_ol);
        if (d_ol != null) subvariables.add(d_ol);
        if (v_releaseol != null) subvariables.add(v_releaseol);
        return subvariables;
    }

    public int getSubvariableListenerCount() {
        return subvariableListeners.size();
    }

    public D_STARTOL getD_startol() {
        return d_startol;
    }

    public T_OL getT_ol() {
        return t_ol;
    }

    public D_OL getD_ol() {
        return d_ol;
    }

    public V_RELEASEOL getV_releaseol() {
        return v_releaseol;
    }
}