package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.Variables;
import packets.Var.BinaryChangeListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static tools.ui.GUIHelper.setTitle;

public class Q_TEXTCONFIRM extends Variables {
    private Q_CONFTEXTDISPLAY nid_c;
    private Q_TEXTREPORT nid_rbc;

    /** List of listeners that will be applied to all subvariables */
    private final List<BinaryChangeListener> subvariableListeners = new CopyOnWriteArrayList<>();

    public Q_TEXTCONFIRM() {
        super("Q_TEXTCONFIRM",
                2,
                "Kvalifikacer ");

        // Add default listener for subvariables
        addDefaultSubvariableListener();

        initializeSubvariables();
    }

    /**
     * Adds a listener that will be automatically applied to all subvariables.
     */
    public Q_TEXTCONFIRM addSubvariableListener(BinaryChangeListener listener) {
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
        if (nid_rbc != null) nid_rbc.addBinaryChangeListener(listener);
    }

    private void removeListenerFromAllSubvariables(BinaryChangeListener listener) {
        if (nid_c != null) nid_c.removeBinaryChangeListener(listener);
        if (nid_rbc != null) nid_rbc.removeBinaryChangeListener(listener);
    }

    private void applyAllListenersToVariable(Variables variable) {
        for (BinaryChangeListener listener : subvariableListeners) {
            variable.addBinaryChangeListener(listener);
        }
    }

    private void addDefaultSubvariableListener() {
        addSubvariableListener((source, oldValue, newValue) -> {
            System.out.println(String.format(
                    "Subvariable '%s' in Q_TEXTCONFIRM changed from '%s' to '%s'",
                    source.getName(), oldValue, newValue
            ));
        });
    }

    private void initializeSubvariables() {
        nid_c = new Q_CONFTEXTDISPLAY();
        nid_rbc = new Q_TEXTREPORT();

        // Apply all listeners to the newly created subvariables
        applyAllListenersToVariable(nid_c);
        applyAllListenersToVariable(nid_rbc);
    }

    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> s = new ArrayList<>();
        s.add("Není vyžadováno");
        s.add("Je vyžadováno");
        s.add("Je a provozní brzda");
        s.add("Je a Nouzová przda");
        return s;
    }

    @Override
    public Variables deepCopy() {
        Q_TEXTCONFIRM tmp = new Q_TEXTCONFIRM();

        // Copy listeners first
        tmp.subvariableListeners.addAll(this.subvariableListeners);

        tmp.setBinValue(getBinValue());

        // Create new subvariables and apply listeners
        tmp.nid_c = new Q_CONFTEXTDISPLAY();
        tmp.nid_rbc = new Q_TEXTREPORT();

        if (this.nid_c != null) {
            tmp.nid_c.setBinValue(this.nid_c.getBinValue());
        }
        if (this.nid_rbc != null) {
            tmp.nid_rbc.setBinValue(this.nid_rbc.getBinValue());
        }

        tmp.applyAllListenersToVariable(tmp.nid_c);
        tmp.applyAllListenersToVariable(tmp.nid_rbc);

        return tmp;
    }

    @Override
    public Component getComponent(String com) {
        JPanel panel = new JPanel(new MigLayout("wrap 1"));

        JPanel ini = ((JPanel) super.getComponent(com));
        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

        Component NID_C_comp1 = nid_c.getComponent(com);
        Component NID_C_comp2 = nid_rbc.getComponent(com);

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox.getSelectedIndex() != 0) {
                    NID_C_comp1.setVisible(true);
                    NID_C_comp2.setVisible(true);
                } else {
                    NID_C_comp1.setVisible(false);
                    NID_C_comp2.setVisible(false);
                }
            }
        });

        if (jComboBox.getSelectedIndex() != 0) {
            NID_C_comp1.setVisible(true);
            NID_C_comp2.setVisible(true);
        } else {
            NID_C_comp1.setVisible(false);
            NID_C_comp2.setVisible(false);
        }

        panel.add(jComboBox);
        panel.add(NID_C_comp1);
        panel.add(NID_C_comp2);

        return setTitle(panel, "Q_TEXTCONFIRM");
    }

    @Override
    public Variables initValueSet(String[] s) {
        // Recreate subvariables and apply listeners
        nid_c = new Q_CONFTEXTDISPLAY();
        nid_rbc = new Q_TEXTREPORT();
        applyAllListenersToVariable(nid_c);
        applyAllListenersToVariable(nid_rbc);

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() != 0) {
            nid_c.initValueSet(s);
            nid_rbc.initValueSet(s);
        }
        return this;
    }

    @Override
    public String getFullData() {
        String tmp = getBinValue();

        if (getDecValue() != 0) {
            tmp += nid_c.getFullData();
            tmp += nid_rbc.getFullData();
        }
        return tmp;
    }

    @Override
    public String getSimpleView() {
        String tmp = super.getSimpleView();

        if (getDecValue() != 0) {
            tmp += nid_c.getSimpleView();
            tmp += nid_rbc.getSimpleView();
        }
        return tmp;
    }

    /**
     * Gets all subvariables as a list for convenient iteration.
     */
    public List<Variables> getAllSubvariables() {
        List<Variables> subvariables = new ArrayList<>();
        if (nid_c != null) subvariables.add(nid_c);
        if (nid_rbc != null) subvariables.add(nid_rbc);
        return subvariables;
    }

    public int getSubvariableListenerCount() {
        return subvariableListeners.size();
    }

    public Q_CONFTEXTDISPLAY getNid_c() {
        return nid_c;
    }

    public Q_TEXTREPORT getNid_rbc() {
        return nid_rbc;
    }
}