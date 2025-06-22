package packets.Var.Q;

import packets.Var.BinaryChangeListener;
import tools.crypto.ArithmeticalFunctions;
import tools.string.StringHelper;
import packets.Var.A.A_NVP12;
import packets.Var.A.A_NVP23;
import packets.Var.M.M_NVKVINT;
import packets.Var.N.N_ITER;
import packets.Var.V.V_NVKVINT;
import packets.Var.Variables;
import net.miginfocom.swing.MigLayout;
import tools.ui.InputJCombobox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static tools.ui.GUIHelper.addLabel;

/**
 * Q_NVKVINTSET represents a "Kv_int set" packet.
 * <p>
 * It provides a UI component that includes a combo box to select a mode (e.g. "Nákladní vlak" or "Osobní vlak")
 * and several sub-components corresponding to internal variables. The layout is built using MigLayout,
 * and the visibility of certain sub-components is toggled based on the combo box selection.
 * </p>
 */
public class Q_NVKVINTSET extends Variables {

    // Sub-variable fields for this packet
    private A_NVP12 a_nvp12;
    private A_NVP23 a_nvp23;
    private V_NVKVINT v_nvkvint;
    private M_NVKVINT m_nvkvint1;
    private M_NVKVINT m_nvkvint2;
    private N_ITER n_iter;

    /** List of listeners that will be applied to all subvariables */
    private final List<BinaryChangeListener> subvariableListeners = new CopyOnWriteArrayList<>();

    /**
     * Default constructor that initializes subfields with default conditions.
     */
    public Q_NVKVINTSET() {
        super("Q_NVKVINTSET", 2, "Type of Kv_int set");

        // Add default listener for subvariables
        addDefaultSubvariableListener();

        initializeSubvariables();
    }

    /**
     * Adds a listener that will be automatically applied to all subvariables.
     *
     * @param listener the listener to add to all subvariables
     * @return this Q_NVKVINTSET instance for chaining
     */
    public Q_NVKVINTSET addSubvariableListener(BinaryChangeListener listener) {
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
        if (a_nvp12 != null) {
            a_nvp12.addBinaryChangeListener(listener);
        }
        if (a_nvp23 != null) {
            a_nvp23.addBinaryChangeListener(listener);
        }
        if (v_nvkvint != null) {
            v_nvkvint.addBinaryChangeListener(listener);
        }
        if (m_nvkvint1 != null) {
            m_nvkvint1.addBinaryChangeListener(listener);
        }
        if (m_nvkvint2 != null) {
            m_nvkvint2.addBinaryChangeListener(listener);
        }
        if (n_iter != null) {
            n_iter.addBinaryChangeListener(listener);
            n_iter.addChildListener(listener); // For N_ITER children
        }
    }

    /**
     * Removes a single listener from all current subvariables.
     *
     * @param listener the listener to remove
     */
    private void removeListenerFromAllSubvariables(BinaryChangeListener listener) {
        if (a_nvp12 != null) {
            a_nvp12.removeBinaryChangeListener(listener);
        }
        if (a_nvp23 != null) {
            a_nvp23.removeBinaryChangeListener(listener);
        }
        if (v_nvkvint != null) {
            v_nvkvint.removeBinaryChangeListener(listener);
        }
        if (m_nvkvint1 != null) {
            m_nvkvint1.removeBinaryChangeListener(listener);
        }
        if (m_nvkvint2 != null) {
            m_nvkvint2.removeBinaryChangeListener(listener);
        }
        if (n_iter != null) {
            n_iter.removeBinaryChangeListener(listener);
            n_iter.removeChildListener(listener);
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

            // If it's an N_ITER, also add as child listener
            if (variable instanceof N_ITER) {
                ((N_ITER) variable).addChildListener(listener);
            }
        }
    }

    /**
     * Adds a default listener that logs changes in subvariables.
     */
    private void addDefaultSubvariableListener() {
        addSubvariableListener((source, oldValue, newValue) -> {
            System.out.println(String.format(
                    "Subvariable '%s' in Q_NVKVINTSET changed from '%s' to '%s'",
                    source.getName(), oldValue, newValue
            ));
        });
    }

    /**
     * Initializes all subvariables and applies listeners.
     */
    private void initializeSubvariables() {
        a_nvp12 = (A_NVP12) new A_NVP12().setCond(this, 1);
        a_nvp23 = (A_NVP23) new A_NVP23().setCond(this, 1);
        v_nvkvint = new V_NVKVINT();
        m_nvkvint1 = new M_NVKVINT();
        m_nvkvint2 = (M_NVKVINT) new M_NVKVINT().setCond(this, 1);
        n_iter = new N_ITER("Hodnoty pro výpočet křivek")
                .addNewIterVar(new V_NVKVINT())
                .addNewIterVar(new M_NVKVINT())
                .addNewIterVar(new M_NVKVINT().setCond(this, 1));

        // Apply all listeners to the newly created subvariables
        applyAllListenersToVariable(a_nvp12);
        applyAllListenersToVariable(a_nvp23);
        applyAllListenersToVariable(v_nvkvint);
        applyAllListenersToVariable(m_nvkvint1);
        applyAllListenersToVariable(m_nvkvint2);
        applyAllListenersToVariable(n_iter);
    }

    /**
     * Initializes subfields using the provided binary data array.
     *
     * @param s the binary data array
     * @return this instance after initialization
     */
    @Override
    public Variables initValueSet(String[] s) {
        // Recreate subvariables
        a_nvp12 = (A_NVP12) new A_NVP12().setCond(this, 1);
        a_nvp23 = (A_NVP23) new A_NVP23().setCond(this, 1);
        v_nvkvint = new V_NVKVINT();
        m_nvkvint1 = new M_NVKVINT();
        m_nvkvint2 = (M_NVKVINT) new M_NVKVINT().setCond(this, 1);
        n_iter = new N_ITER("Hodnoty pro výpočet křivek")
                .addNewIterVar(new V_NVKVINT())
                .addNewIterVar(new M_NVKVINT())
                .addNewIterVar(new M_NVKVINT().setCond(this, 1));

        // Apply listeners to recreated subvariables
        applyAllListenersToVariable(a_nvp12);
        applyAllListenersToVariable(a_nvp23);
        applyAllListenersToVariable(v_nvkvint);
        applyAllListenersToVariable(m_nvkvint1);
        applyAllListenersToVariable(m_nvkvint2);
        applyAllListenersToVariable(n_iter);

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        a_nvp12.initValueSet(s);
        a_nvp23.initValueSet(s);
        v_nvkvint.initValueSet(s);
        m_nvkvint1.initValueSet(s);
        m_nvkvint2.initValueSet(s);
        n_iter.initValueSet(s);

        return this;
    }

    /**
     * Constructs and returns the UI component for Q_NVKVINTSET using MigLayout.
     * <p>
     * The component contains:
     * <ul>
     *   <li>A combo box at the top for mode selection.</li>
     *   <li>A panel with sub-components for A_NVP12, A_NVP23, V_NVKVINT, M_NVKVINT1, M_NVKVINT2.</li>
     *   <li>The N_ITER component is added on a new row spanning the full width.</li>
     * </ul>
     * The action listener on the combo box toggles the visibility of extra components.
     * </p>
     *
     * @param comment an optional comment to append to the title
     * @return the constructed UI component with title
     */
    @Override
    public Component getComponent(String comment) {
        // Main panel with MigLayout. One column per row.
        JPanel mainPanel = new JPanel(new MigLayout("wrap 1, insets 10", "[grow]", "[]10[]"));

        // Combo box for mode selection.
        JComboBox<String> modeComboBox = new JComboBox<>();
        modeComboBox.addItem("Nákladní vlak");
        modeComboBox.addItem("Osobní vlak");
        modeComboBox.addItem("NOT_USED");
        modeComboBox.addItem("NOT_USED");

        new InputJCombobox(modeComboBox);

        modeComboBox.setSelectedIndex(getDecValue());

        // Add combo box to panel with a label using GUIHelper.
        Component comboComponent = addLabel(modeComboBox, getName(), getDescription(), new JLabel());
        mainPanel.add(comboComponent, "growx");

        // Create a sub-panel to hold variable components.
        JPanel variablePanel = new JPanel(new MigLayout("wrap 2, insets 0", "[grow, fill][grow, fill]", "[]10[]"));

        // Retrieve components for each sub-variable.
        Component compA_NVP12 = a_nvp12.getComponent();
        Component compA_NVP23 = a_nvp23.getComponent();
        Component compV_NVKVINT = v_nvkvint.getComponent();
        Component compM_NVKVINT1 = m_nvkvint1.getComponent();
        Component compM_NVKVINT2 = m_nvkvint2.getComponent();
        Component compN_ITER = n_iter.getComponent();

        // Add sub-components to the variable panel.
        variablePanel.add(compA_NVP12, "growx");
        variablePanel.add(compA_NVP23, "growx");
        variablePanel.add(compV_NVKVINT, "growx");
        variablePanel.add(compM_NVKVINT1, "growx");
        variablePanel.add(compM_NVKVINT2, "growx");
        variablePanel.add(compN_ITER, "span, growx");

        mainPanel.add(variablePanel, "growx");

        // Add an action listener to toggle visibility based on combo box selection.
        modeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = modeComboBox.getSelectedIndex();
                // Show extra components only if "Osobní vlak" (index 1) is selected.
                boolean showExtra = (selectedIndex == 1);
                compA_NVP12.setVisible(showExtra);
                compA_NVP23.setVisible(showExtra);
                compM_NVKVINT2.setVisible(showExtra);
                setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(selectedIndex), getMaxSize()));
            }
        });

        // Set initial visibility for extra components.
        boolean showExtra = (modeComboBox.getSelectedIndex() == 1);
        compA_NVP12.setVisible(showExtra);
        compA_NVP23.setVisible(showExtra);
        compM_NVKVINT2.setVisible(showExtra);

        // Return the panel with a title.
        return mainPanel;
    }

    @Override
    public Component getComponent() {
        return getComponent("");
    }

    @Override
    public Variables deepCopy() {
        Q_NVKVINTSET copy = new Q_NVKVINTSET();

        // Copy listeners first
        copy.subvariableListeners.addAll(this.subvariableListeners);

        copy.setBinValue(getBinValue());

        // Create new subvariables
        copy.a_nvp12 = (A_NVP12) new A_NVP12().initValueSet(new String[]{a_nvp12.getBinValue()}).setCond(copy, 1);
        copy.a_nvp23 = (A_NVP23) new A_NVP23().initValueSet(new String[]{a_nvp23.getBinValue()}).setCond(copy, 1);
        copy.v_nvkvint = (V_NVKVINT) new V_NVKVINT().initValueSet(new String[]{v_nvkvint.getBinValue()});
        copy.m_nvkvint1 = (M_NVKVINT) new M_NVKVINT().initValueSet(new String[]{m_nvkvint1.getBinValue()});
        copy.m_nvkvint2 = (M_NVKVINT) new M_NVKVINT().initValueSet(new String[]{m_nvkvint2.getBinValue()}).setCond(copy, 1);

        // For N_ITER, we need to properly copy the template and data
        copy.n_iter = new N_ITER("Hodnoty pro výpočet křivek")
                .addNewIterVar(new V_NVKVINT())
                .addNewIterVar(new M_NVKVINT())
                .addNewIterVar(new M_NVKVINT().setCond(copy, 1));
        copy.n_iter.setTemplate(n_iter.getTemplate());
        copy.n_iter.initValueSet(new String[]{n_iter.getBinValue()});

        // Apply all listeners to the copied subvariables
        copy.applyAllListenersToVariable(copy.a_nvp12);
        copy.applyAllListenersToVariable(copy.a_nvp23);
        copy.applyAllListenersToVariable(copy.v_nvkvint);
        copy.applyAllListenersToVariable(copy.m_nvkvint1);
        copy.applyAllListenersToVariable(copy.m_nvkvint2);
        copy.applyAllListenersToVariable(copy.n_iter);

        return copy;
    }

    @Override
    public String getFullData() {
        StringBuilder sb = new StringBuilder();
        sb.append(getBinValue());
        sb.append(a_nvp12.getFullData());
        sb.append(a_nvp23.getFullData());
        sb.append(v_nvkvint.getFullData());
        sb.append(m_nvkvint1.getFullData());
        sb.append(m_nvkvint2.getFullData());
        sb.append(n_iter.getFullData());
        return sb.toString();
    }

    @Override
    public String getSimpleView() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getSimpleView());
        sb.append(a_nvp12.getSimpleView());
        sb.append(a_nvp23.getSimpleView());
        sb.append(v_nvkvint.getSimpleView());
        sb.append(m_nvkvint1.getSimpleView());
        sb.append(m_nvkvint2.getSimpleView());
        sb.append(n_iter.getSimpleView());
        return sb.toString();
    }

    /**
     * Gets all subvariables as a list for convenient iteration.
     *
     * @return a list containing all subvariables
     */
    public List<Variables> getAllSubvariables() {
        List<Variables> subvariables = new ArrayList<>();
        if (a_nvp12 != null) subvariables.add(a_nvp12);
        if (a_nvp23 != null) subvariables.add(a_nvp23);
        if (v_nvkvint != null) subvariables.add(v_nvkvint);
        if (m_nvkvint1 != null) subvariables.add(m_nvkvint1);
        if (m_nvkvint2 != null) subvariables.add(m_nvkvint2);
        if (n_iter != null) subvariables.add(n_iter);
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

    // Getters for individual subvariables (useful for testing and specific access)
    public A_NVP12 getA_nvp12() { return a_nvp12; }
    public A_NVP23 getA_nvp23() { return a_nvp23; }
    public V_NVKVINT getV_nvkvint() { return v_nvkvint; }
    public M_NVKVINT getM_nvkvint1() { return m_nvkvint1; }
    public M_NVKVINT getM_nvkvint2() { return m_nvkvint2; }
    public N_ITER getN_iter() { return n_iter; }
}