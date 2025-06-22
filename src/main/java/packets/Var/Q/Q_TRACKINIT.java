package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.D.D_TRACKCOND;
import packets.Var.D.D_TRACKINIT;
import packets.Var.L.L_TRACKCOND;
import packets.Var.M.M_TRACKCOND;
import packets.Var.N.N_ITER;
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

public class Q_TRACKINIT extends Variables {
    D_TRACKINIT d_trackinit;
    D_TRACKCOND d_trackcond;
    L_TRACKCOND l_trackcond;
    M_TRACKCOND m_trackcond;
    N_ITER n_iter;

    /** List of listeners that will be applied to all subvariables */
    private final List<BinaryChangeListener> subvariableListeners = new CopyOnWriteArrayList<>();

    public Q_TRACKINIT() {
        super("Q_TRACKINIT",
                1,
                "Kvalifikátor pro obnovení počátečních stavů souvisejícího popisu stopy paketu.");

        // Add default listener for subvariables
        addDefaultSubvariableListener();

        initializeSubvariables();
    }

    /**
     * Adds a listener that will be automatically applied to all subvariables.
     */
    public Q_TRACKINIT addSubvariableListener(BinaryChangeListener listener) {
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
        if (d_trackinit != null) d_trackinit.addBinaryChangeListener(listener);
        if (d_trackcond != null) d_trackcond.addBinaryChangeListener(listener);
        if (l_trackcond != null) l_trackcond.addBinaryChangeListener(listener);
        if (m_trackcond != null) m_trackcond.addBinaryChangeListener(listener);
        if (n_iter != null) n_iter.addBinaryChangeListener(listener);
    }

    private void removeListenerFromAllSubvariables(BinaryChangeListener listener) {
        if (d_trackinit != null) d_trackinit.removeBinaryChangeListener(listener);
        if (d_trackcond != null) d_trackcond.removeBinaryChangeListener(listener);
        if (l_trackcond != null) l_trackcond.removeBinaryChangeListener(listener);
        if (m_trackcond != null) m_trackcond.removeBinaryChangeListener(listener);
        if (n_iter != null) n_iter.removeBinaryChangeListener(listener);
    }

    private void applyAllListenersToVariable(Variables variable) {
        for (BinaryChangeListener listener : subvariableListeners) {
            variable.addBinaryChangeListener(listener);
        }
    }

    private void addDefaultSubvariableListener() {
        addSubvariableListener((source, oldValue, newValue) -> {
            System.out.println(String.format(
                    "Subvariable '%s' in Q_TRACKINIT changed from '%s' to '%s'",
                    source.getName(), oldValue, newValue
            ));
        });
    }

    private void initializeSubvariables() {
        d_trackinit = new D_TRACKINIT();
        d_trackcond = new D_TRACKCOND();
        l_trackcond = new L_TRACKCOND();
        m_trackcond = new M_TRACKCOND();
        n_iter = new N_ITER()
                .addNewIterVar(new D_TRACKCOND())
                .addNewIterVar(new L_TRACKCOND())
                .addNewIterVar(new M_TRACKCOND());
        n_iter.setWRAPINT(1);

        // Apply all listeners to the newly created subvariables
        applyAllListenersToVariable(d_trackinit);
        applyAllListenersToVariable(d_trackcond);
        applyAllListenersToVariable(l_trackcond);
        applyAllListenersToVariable(m_trackcond);
        applyAllListenersToVariable(n_iter);
    }

    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> s = new ArrayList<>();
        s.add("Žádné počáteční stavy");
        s.add("Prázdný profil");
        return s;
    }

    @Override
    public Variables initValueSet(String[] s) {
        // Recreate subvariables and apply listeners
        d_trackinit = new D_TRACKINIT();
        d_trackcond = new D_TRACKCOND();
        l_trackcond = new L_TRACKCOND();
        m_trackcond = new M_TRACKCOND();
        n_iter = new N_ITER()
                .addNewIterVar(new D_TRACKCOND())
                .addNewIterVar(new L_TRACKCOND())
                .addNewIterVar(new M_TRACKCOND());
        n_iter.setWRAPINT(1);

        applyAllListenersToVariable(d_trackinit);
        applyAllListenersToVariable(d_trackcond);
        applyAllListenersToVariable(l_trackcond);
        applyAllListenersToVariable(m_trackcond);
        applyAllListenersToVariable(n_iter);

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() == 0) {
            d_trackcond.initValueSet(s);
            l_trackcond.initValueSet(s);
            m_trackcond.initValueSet(s);
            n_iter.initValueSet(s);
        } else {
            d_trackinit.initValueSet(s);
        }

        return this;
    }

    @Override
    public String getFullData() {
        String tmp = getBinValue();

        if (getDecValue() == 0) {
            tmp += d_trackcond.getFullData();
            tmp += l_trackcond.getFullData();
            tmp += m_trackcond.getFullData();
            tmp += n_iter.getFullData();
        } else {
            tmp += d_trackinit.getFullData();
        }

        return tmp;
    }

    @Override
    public Component getComponent(String com) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel ini = ((JPanel) super.getComponent(com));
        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

        jComboBox.setPreferredSize(new Dimension(600, 24));
        jComboBox.setMaximumSize(new Dimension(600, 24));
        jComboBox.setMinimumSize(new Dimension(600, 24));

        Component component1 = d_trackinit.getComponent(com);
        Component component2 = d_trackcond.getComponent(com);
        Component component3 = l_trackcond.getComponent(com);
        Component component4 = m_trackcond.getComponent(com);
        Component component5 = n_iter.getComponent();

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox.getSelectedIndex() == 1) {
                    component1.setVisible(true);
                    component2.setVisible(false);
                    component3.setVisible(false);
                    component4.setVisible(false);
                    component5.setVisible(false);
                } else {
                    component1.setVisible(false);
                    component2.setVisible(true);
                    component3.setVisible(true);
                    component4.setVisible(true);
                    component5.setVisible(true);
                }
            }
        });

        if (jComboBox.getSelectedIndex() == 1) {
            component1.setVisible(true);
            component2.setVisible(false);
            component3.setVisible(false);
            component4.setVisible(false);
            component5.setVisible(false);
        } else {
            component1.setVisible(false);
            component2.setVisible(true);
            component3.setVisible(true);
            component4.setVisible(true);
            component5.setVisible(true);
        }

        panel.add(jComboBox);
        panel.add(component1);
        panel.add(component2);
        panel.add(component3);
        panel.add(component4);
        panel.add(component5);

        return setTitle(panel, "Q_TRACKINIT");
    }

    @Override
    public String getSimpleView() {
        String tmp = super.getSimpleView();

        if (getDecValue() == 0) {
            tmp += d_trackcond.getSimpleView();
            tmp += l_trackcond.getSimpleView();
            tmp += m_trackcond.getSimpleView();
            tmp += n_iter.getSimpleView();
        } else {
            tmp += d_trackinit.getSimpleView();
        }

        return tmp;
    }

    @Override
    public Variables deepCopy() {
        Q_TRACKINIT tmp = new Q_TRACKINIT();

        // Copy listeners first
        tmp.subvariableListeners.addAll(this.subvariableListeners);

        tmp.setBinValue(getBinValue());

        // Create new subvariables and apply listeners
        tmp.d_trackinit = new D_TRACKINIT();
        tmp.d_trackcond = new D_TRACKCOND();
        tmp.l_trackcond = new L_TRACKCOND();
        tmp.m_trackcond = new M_TRACKCOND();
        tmp.n_iter = new N_ITER()
                .addNewIterVar(new D_TRACKCOND())
                .addNewIterVar(new L_TRACKCOND())
                .addNewIterVar(new M_TRACKCOND());
        tmp.n_iter.setWRAPINT(1);

        if (this.d_trackinit != null) {
            tmp.d_trackinit.setBinValue(this.d_trackinit.getBinValue());
        }
        if (this.d_trackcond != null) {
            tmp.d_trackcond.setBinValue(this.d_trackcond.getBinValue());
        }
        if (this.l_trackcond != null) {
            tmp.l_trackcond.setBinValue(this.l_trackcond.getBinValue());
        }
        if (this.m_trackcond != null) {
            tmp.m_trackcond.setBinValue(this.m_trackcond.getBinValue());
        }
        if (this.n_iter != null) {
            tmp.n_iter.setBinValue(this.n_iter.getBinValue());
        }

        tmp.applyAllListenersToVariable(tmp.d_trackinit);
        tmp.applyAllListenersToVariable(tmp.d_trackcond);
        tmp.applyAllListenersToVariable(tmp.l_trackcond);
        tmp.applyAllListenersToVariable(tmp.m_trackcond);
        tmp.applyAllListenersToVariable(tmp.n_iter);

        return tmp;
    }

    /**
     * Gets all subvariables as a list for convenient iteration.
     */
    public List<Variables> getAllSubvariables() {
        List<Variables> subvariables = new ArrayList<>();
        if (d_trackinit != null) subvariables.add(d_trackinit);
        if (d_trackcond != null) subvariables.add(d_trackcond);
        if (l_trackcond != null) subvariables.add(l_trackcond);
        if (m_trackcond != null) subvariables.add(m_trackcond);
        if (n_iter != null) subvariables.add(n_iter);
        return subvariables;
    }

    public int getSubvariableListenerCount() {
        return subvariableListeners.size();
    }

    public D_TRACKINIT getD_trackinit() {
        return d_trackinit;
    }

    public D_TRACKCOND getD_trackcond() {
        return d_trackcond;
    }

    public L_TRACKCOND getL_trackcond() {
        return l_trackcond;
    }

    public M_TRACKCOND getM_trackcond() {
        return m_trackcond;
    }

    public N_ITER getN_iter() {
        return n_iter;
    }
}