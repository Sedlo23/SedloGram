package packets.Var.Q;

import net.miginfocom.swing.MigLayout;
import tools.string.StringHelper;
import packets.Var.NID.NID_C;
import packets.Var.Variables;
import packets.Var.BinaryChangeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static tools.ui.GUIHelper.addLabel;

public class Q_NEWCOUNTRY extends Variables {
    private NID_C nid_c;

    /** List of listeners that will be applied to all subvariables */
    private final List<BinaryChangeListener> subvariableListeners = new CopyOnWriteArrayList<>();

    public Q_NEWCOUNTRY() {
        super("Q_NEWCOUNTRY",
                1,
                "Kvalifikátor označující, zda je další balízová skupina ve stejné zemi / železniční správě jako ta předchozí uvnitř balíčku nebo ne.\n" +
                        "Pro první skupinu balise v paketu, pokud Q_NEWCOUNTRY = 0, je to stejná země / železniční správa jako LRBG v rádiové zprávě, jedna balízová  skupina v rámci balízického telegramu předávající paket  nebo jedna ze smyček uvnitř zprávy smyčky předávající paket.\n");

        // Add default listener for subvariables
        addDefaultSubvariableListener();

        initializeSubvariables();
    }

    /**
     * Adds a listener that will be automatically applied to all subvariables.
     */
    public Q_NEWCOUNTRY addSubvariableListener(BinaryChangeListener listener) {
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
                    "Subvariable '%s' in Q_NEWCOUNTRY changed from '%s' to '%s'",
                    source.getName(), oldValue, newValue
            ));
        });
    }

    private void initializeSubvariables() {
        nid_c = new NID_C();

        // Apply all listeners to the newly created subvariable
        applyAllListenersToVariable(nid_c);
    }

    @Override
    public Variables deepCopy() {
        Q_NEWCOUNTRY tmp = new Q_NEWCOUNTRY();

        // Copy listeners first
        tmp.subvariableListeners.addAll(this.subvariableListeners);

        tmp.setBinValue(getBinValue());

        // Create new subvariable and apply listeners
        tmp.nid_c = new NID_C();
        if (this.nid_c != null) {
            tmp.nid_c.setBinValue(this.nid_c.getBinValue());
        }

        tmp.applyAllListenersToVariable(tmp.nid_c);

        return tmp;
    }

    @Override
    public Component getComponent(String com) {
        JPanel panel = new JPanel(new MigLayout("", "[]16[]",""));

        JPanel ini = ((JPanel) super.getComponent(com));
        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

        Component NID_C_comp = nid_c.getComponent(com);
        JComboBox jComboBox1 = (JComboBox) (((JPanel) NID_C_comp).getComponent(1));

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox.getSelectedIndex() == 1)
                    jComboBox1.setVisible(true);
                else
                    jComboBox1.setVisible(false);
            }
        });

        if (jComboBox.getSelectedIndex() == 1)
            jComboBox1.setVisible(true);
        else
            jComboBox1.setVisible(false);

        panel.add(jComboBox);
        panel.add(jComboBox1);

        return addLabel(panel, "Q_NEWCOUNTRY","",new JLabel());
    }

    @Override
    public Variables initValueSet(String[] s) {
        // Recreate subvariable and apply listeners
        nid_c = new NID_C();
        applyAllListenersToVariable(nid_c);

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() == 1)
            nid_c.initValueSet(s);

        return this;
    }

    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> s = new ArrayList<>();
        s.add("Stejná země");
        s.add("Nová země");
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

    public NID_C getNid_c() {
        return nid_c;
    }
}