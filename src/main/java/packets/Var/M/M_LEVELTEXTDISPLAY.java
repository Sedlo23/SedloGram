package packets.Var.M;

import tools.string.StringHelper;
import packets.Var.NID.NID_NTC;
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

public class M_LEVELTEXTDISPLAY extends Variables {
    private NID_NTC nid_c;

    /** List of listeners that will be applied to all subvariables */
    private final List<BinaryChangeListener> subvariableListeners = new CopyOnWriteArrayList<>();

    public M_LEVELTEXTDISPLAY() {
        super("Text se při zadávání / zobrazuje tak dlouho, dokud je v definované úrovni.",
                3,
                "The text is displayed when entering / as long as in the defined level");

        // Add default listener for subvariables
        addDefaultSubvariableListener();

        initializeSubvariables();
    }

    /**
     * Adds a listener that will be automatically applied to all subvariables.
     *
     * @param listener the listener to add to all subvariables
     * @return this M_LEVELTEXTDISPLAY instance for chaining
     */
    public M_LEVELTEXTDISPLAY addSubvariableListener(BinaryChangeListener listener) {
        if (listener != null) {
            subvariableListeners.add(listener);
            applyListenerToAllSubvariables(listener);
        }
        return this;
    }

    /**
     * Removes a listener from all subvariables and from the subvariable listeners list.
     *
     * @param listener the listener to remove
     * @return true if the listener was found and removed
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

    /**
     * Applies a single listener to all current subvariables.
     *
     * @param listener the listener to apply
     */
    private void applyListenerToAllSubvariables(BinaryChangeListener listener) {
        if (nid_c != null) {
            nid_c.addBinaryChangeListener(listener);
        }
    }

    /**
     * Removes a single listener from all current subvariables.
     *
     * @param listener the listener to remove
     */
    private void removeListenerFromAllSubvariables(BinaryChangeListener listener) {
        if (nid_c != null) {
            nid_c.removeBinaryChangeListener(listener);
        }
    }

    /**
     * Applies all registered subvariable listeners to a specific variable.
     *
     * @param variable the variable to apply listeners to
     */
    private void applyAllListenersToVariable(Variables variable) {
        for (BinaryChangeListener listener : subvariableListeners) {
            variable.addBinaryChangeListener(listener);
        }
    }

    /**
     * Adds a default listener that logs changes in subvariables.
     */
    private void addDefaultSubvariableListener() {
        addSubvariableListener((source, oldValue, newValue) -> {
            System.out.println(String.format(
                    "Subvariable '%s' in M_LEVELTEXTDISPLAY changed from '%s' to '%s'",
                    source.getName(), oldValue, newValue
            ));
        });
    }

    /**
     * Initializes all subvariables and applies listeners.
     */
    private void initializeSubvariables() {
        nid_c = new NID_NTC();

        // Apply all listeners to the newly created subvariable
        applyAllListenersToVariable(nid_c);
    }

    @Override
    public Variables deepCopy() {
        M_LEVELTEXTDISPLAY tmp = new M_LEVELTEXTDISPLAY();

        // Copy listeners first
        tmp.subvariableListeners.addAll(this.subvariableListeners);

        tmp.setBinValue(getBinValue());

        // Create new subvariable and apply listeners
        tmp.nid_c = new NID_NTC();
        if (this.nid_c != null) {
            tmp.nid_c.setBinValue(this.nid_c.getBinValue());
        }
        tmp.applyAllListenersToVariable(tmp.nid_c);

        return tmp;
    }

    @Override
    public Component getComponent(String com) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JComboBox jComboBox = (JComboBox) ((JPanel) super.getComponent(com)).getComponent(1);
        Component NID_C_comp = nid_c.getComponent(com);

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox.getSelectedIndex() == 1)
                    NID_C_comp.setVisible(true);
                else
                    NID_C_comp.setVisible(false);
            }
        });

        // Set initial visibility based on current selection
        boolean shouldShowNidC = (jComboBox.getSelectedIndex() == 1);
        NID_C_comp.setVisible(shouldShowNidC);

        panel.add(jComboBox);
        panel.add(NID_C_comp);

        return setTitle(panel, "M_LEVELTEXTDISPLAY");
    }

    @Override
    public Variables initValueSet(String[] s) {
        // Recreate subvariable and apply listeners
        nid_c = new NID_NTC();
        applyAllListenersToVariable(nid_c);

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() == 1)
            nid_c.initValueSet(s);

        return this;
    }

    @Override
    public String getFullData() {
        String tmp = getBinValue();

        if (getDecValue() == 1)
            tmp += nid_c.getFullData();

        return tmp;
    }

    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> s = super.getCombo();

        s.set(0, "Level 0");
        s.set(1, "Level NTC");
        s.set(2, "Level 1");
        s.set(3, "Level 2");
        s.set(4, "Level 3");
        s.set(5, "Není omezeno");
        s.set(6, "NOT_USED");
        s.set(7, "NOT_USED");

        return s;
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
     *
     * @return a list containing all subvariables
     */
    public List<Variables> getAllSubvariables() {
        List<Variables> subvariables = new ArrayList<>();
        if (nid_c != null) subvariables.add(nid_c);
        return subvariables;
    }

    /**
     * Gets the number of registered subvariable listeners.
     *
     * @return the number of subvariable listeners
     */
    public int getSubvariableListenerCount() {
        return subvariableListeners.size();
    }

    // Getter for the subvariable (useful for testing and specific access)
    public NID_NTC getNid_c() {
        return nid_c;
    }
}