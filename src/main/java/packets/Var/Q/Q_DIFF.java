package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.Variables;
import packets.Var.BinaryChangeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static tools.ui.GUIHelper.addLabel;

public class Q_DIFF extends Variables {
    NC_CDDIFF nc_cddiff;

    /** List of listeners that will be applied to all subvariables */
    private final List<BinaryChangeListener> subvariableListeners = new CopyOnWriteArrayList<>();

    public Q_DIFF() {
        super("Q_DIFF",
                2,
                "Označuje typ konkrétní kategorie SSP\n" +
                        "V případě \"jiného specifického\" SSP sděluje palubnímu zařízení ERTMS/ETCS, zda nahrazuje nebo nenahrazuje SSP pro nedostatky v Cant, jak byl zvolen palubním zařízením (viz 3.11.3.2.3), pokud vlak patří do kategorie vlaků \"jiný mezinárodní\", na kterou se vztahuje \"jiný specifický\" SSP.");

        // Add default listener for subvariables
        addDefaultSubvariableListener();

        initializeSubvariables();
    }

    /**
     * Adds a listener that will be automatically applied to all subvariables.
     */
    public Q_DIFF addSubvariableListener(BinaryChangeListener listener) {
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
        if (nc_cddiff != null) nc_cddiff.addBinaryChangeListener(listener);
    }

    private void removeListenerFromAllSubvariables(BinaryChangeListener listener) {
        if (nc_cddiff != null) nc_cddiff.removeBinaryChangeListener(listener);
    }

    private void applyAllListenersToVariable(Variables variable) {
        for (BinaryChangeListener listener : subvariableListeners) {
            variable.addBinaryChangeListener(listener);
        }
    }

    private void addDefaultSubvariableListener() {
        addSubvariableListener((source, oldValue, newValue) -> {
            System.out.println(String.format(
                    "Subvariable '%s' in Q_DIFF changed from '%s' to '%s'",
                    source.getName(), oldValue, newValue
            ));
        });
    }

    private void initializeSubvariables() {
        nc_cddiff = new NC_CDDIFF();

        // Apply all listeners to the newly created subvariable
        applyAllListenersToVariable(nc_cddiff);
    }

    @Override
    public Variables deepCopy() {
        Q_DIFF tmp = new Q_DIFF();

        // Copy listeners first
        tmp.subvariableListeners.addAll(this.subvariableListeners);

        tmp.setBinValue(getBinValue());

        // Create new subvariable and apply listeners
        tmp.nc_cddiff = new NC_CDDIFF();
        if (this.nc_cddiff != null) {
            tmp.nc_cddiff.setBinValue(this.nc_cddiff.getBinValue());
        }
        tmp.applyAllListenersToVariable(tmp.nc_cddiff);

        return tmp;
    }

    Component component;

    @Override
    public Component getComponent(String com) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEADING));

        JComboBox jComboBox = (JComboBox) ((JPanel) super.getComponent(com)).getComponent(1);
        Component NID_C_comp = nc_cddiff.getComponent(com);
        JComboBox jComboBox2 = (JComboBox) ((JPanel) NID_C_comp).getComponent(1);

        panel.add(jComboBox, BorderLayout.NORTH);
        panel.add(jComboBox2, BorderLayout.SOUTH);

        jComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                DefaultComboBoxModel defaultListModel = new DefaultComboBoxModel();

                if (jComboBox.getSelectedIndex() == 0) {
                    nc_cddiff.setPrev(true);
                } else {
                    nc_cddiff.setPrev(false);
                }

                defaultListModel.addAll(nc_cddiff.getCombo());
                jComboBox2.setModel(defaultListModel);
                jComboBox2.setSelectedIndex(nc_cddiff.getDecValue());
                jComboBox2.updateUI();
            }
        });

        component = addLabel(panel, "Q_DIFF", "", new JLabel());
        return component;
    }

    @Override
    public Variables initValueSet(String[] s) {
        // Recreate subvariable and apply listeners
        nc_cddiff = new NC_CDDIFF();
        applyAllListenersToVariable(nc_cddiff);

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (this.getDecValue() == 0) {
            nc_cddiff.setPrev(true);
        } else {
            nc_cddiff.setPrev(false);
        }

        nc_cddiff.initValueSet(s);

        return this;
    }

    @Override
    public String getFullData() {
        String tmp = getBinValue();
        tmp += nc_cddiff.getFullData();
        return tmp;
    }

    @Override
    public String getSimpleView() {
        String tmp = super.getSimpleView();
        tmp += nc_cddiff.getSimpleView();
        return tmp;
    }

    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> s = new ArrayList<>();
        s.add("Kategorie");
        s.add("Nahrazuje SSP");
        s.add("Nenahrazuje SSP");
        s.add("NOT_USED");
        return s;
    }

    /**
     * Gets all subvariables as a list for convenient iteration.
     */
    public List<Variables> getAllSubvariables() {
        List<Variables> subvariables = new ArrayList<>();
        if (nc_cddiff != null) subvariables.add(nc_cddiff);
        return subvariables;
    }

    public int getSubvariableListenerCount() {
        return subvariableListeners.size();
    }

    public NC_CDDIFF getNc_cddiff() {
        return nc_cddiff;
    }
}


