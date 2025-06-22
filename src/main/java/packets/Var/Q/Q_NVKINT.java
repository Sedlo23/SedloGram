package packets.Var.Q;

import packets.Var.BinaryChangeListener;
import packets.Var.L.L_NVKRINT;
import packets.Var.M.M_NVKRINT;
import packets.Var.M.M_NVKTINT;
import tools.crypto.ArithmeticalFunctions;
import tools.string.StringHelper;
import packets.Var.N.N_ITER;
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

/**
 * Q_NVKINT represents a qualifier for integrated correction factors.
 * <p>
 * This packet contains several sub-variables:
 * <ul>
 *   <li>A Q_NVKVINTSET</li>
 *   <li>An N_ITER for "Kategorie vlaků"</li>
 *   <li>An L_NVKRINT and an M_NVKRINT</li>
 *   <li>A second N_ITER (for additional values) and an M_NVKTINT</li>
 * </ul>
 * The UI component is built using MigLayout and includes a combo box at the top
 * whose selection toggles the visibility of the extra components.
 * </p>
 */
public class Q_NVKINT extends Variables {

    private Q_NVKVINTSET q_nvkvintset;
    private N_ITER n_iter;
    private N_ITER n_iter2;
    private L_NVKRINT l_nvkrint;
    private M_NVKRINT m_nvkrint;
    private M_NVKTINT m_nvktint;

    /** List of listeners that will be applied to all subvariables */
    private final List<BinaryChangeListener> subvariableListeners = new CopyOnWriteArrayList<>();

    /**
     * Default constructor.
     */
    public Q_NVKINT() {
        super("Q_NVKINT", 1, "Qualifier for integrated correction factors");

        // Add default listener for subvariables
        addDefaultSubvariableListener();

        initializeSubvariables();
    }

    /**
     * Adds a listener that will be automatically applied to all subvariables.
     *
     * @param listener the listener to add to all subvariables
     * @return this Q_NVKINT instance for chaining
     */
    public Q_NVKINT addSubvariableListener(BinaryChangeListener listener) {
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
        if (q_nvkvintset != null) {
            q_nvkvintset.addBinaryChangeListener(listener);
        }
        if (n_iter != null) {
            n_iter.addBinaryChangeListener(listener);
            n_iter.addChildListener(listener); // For N_ITER children
        }
        if (n_iter2 != null) {
            n_iter2.addBinaryChangeListener(listener);
            n_iter2.addChildListener(listener); // For N_ITER children
        }
        if (l_nvkrint != null) {
            l_nvkrint.addBinaryChangeListener(listener);
        }
        if (m_nvkrint != null) {
            m_nvkrint.addBinaryChangeListener(listener);
        }
        if (m_nvktint != null) {
            m_nvktint.addBinaryChangeListener(listener);
        }
    }

    /**
     * Removes a single listener from all current subvariables.
     *
     * @param listener the listener to remove
     */
    private void removeListenerFromAllSubvariables(BinaryChangeListener listener) {
        if (q_nvkvintset != null) {
            q_nvkvintset.removeBinaryChangeListener(listener);
        }
        if (n_iter != null) {
            n_iter.removeBinaryChangeListener(listener);
            n_iter.removeChildListener(listener);
        }
        if (n_iter2 != null) {
            n_iter2.removeBinaryChangeListener(listener);
            n_iter2.removeChildListener(listener);
        }
        if (l_nvkrint != null) {
            l_nvkrint.removeBinaryChangeListener(listener);
        }
        if (m_nvkrint != null) {
            m_nvkrint.removeBinaryChangeListener(listener);
        }
        if (m_nvktint != null) {
            m_nvktint.removeBinaryChangeListener(listener);
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
                    "Subvariable '%s' in Q_NVKINT changed from '%s' to '%s'",
                    source.getName(), oldValue, newValue
            ));
        });
    }

    /**
     * Initializes all subvariables and applies listeners.
     */
    private void initializeSubvariables() {
        q_nvkvintset = (Q_NVKVINTSET) new Q_NVKVINTSET().setCond(this, 1);
        n_iter = (N_ITER) new N_ITER("Kategorie vlaků")
                .addNewIterVar(new Q_NVKVINTSET())
                .setCond(this, 1);
        l_nvkrint = (L_NVKRINT) new L_NVKRINT().setCond(this, 1);
        m_nvkrint = (M_NVKRINT) new M_NVKRINT().setCond(this, 1);
        n_iter2 = (N_ITER) new N_ITER().setCond(this, 1);
        n_iter2.addNewIterVar(new L_NVKRINT().setCond(this, 1))
                .addNewIterVar(new M_NVKRINT().setCond(this, 1));
        m_nvktint = (M_NVKTINT) new M_NVKTINT().setCond(this, 1);

        n_iter.setWRAPINT(1);
        n_iter2.setWRAPINT(1);

        // Apply all listeners to the newly created subvariables
        applyAllListenersToVariable(q_nvkvintset);
        applyAllListenersToVariable(n_iter);
        applyAllListenersToVariable(l_nvkrint);
        applyAllListenersToVariable(m_nvkrint);
        applyAllListenersToVariable(n_iter2);
        applyAllListenersToVariable(m_nvktint);
    }

    /**
     * Initializes sub-variables using the provided data array.
     *
     * @param s the binary data array
     * @return this instance after initialization
     */
    @Override
    public Variables initValueSet(String[] s) {
        // Recreate subvariables
        q_nvkvintset = (Q_NVKVINTSET) new Q_NVKVINTSET().setCond(this, 1);
        n_iter = (N_ITER) new N_ITER("Kategorie vlaků")
                .addNewIterVar(new Q_NVKVINTSET())
                .setCond(this, 1);
        l_nvkrint = (L_NVKRINT) new L_NVKRINT().setCond(this, 1);
        m_nvkrint = (M_NVKRINT) new M_NVKRINT().setCond(this, 1);
        n_iter2 = (N_ITER) new N_ITER().setCond(this, 1);
        n_iter2.addNewIterVar(new L_NVKRINT().setCond(this, 1))
                .addNewIterVar(new M_NVKRINT().setCond(this, 1));
        m_nvktint = (M_NVKTINT) new M_NVKTINT().setCond(this, 1);

        // Apply listeners to recreated subvariables
        applyAllListenersToVariable(q_nvkvintset);
        applyAllListenersToVariable(n_iter);
        applyAllListenersToVariable(l_nvkrint);
        applyAllListenersToVariable(m_nvkrint);
        applyAllListenersToVariable(n_iter2);
        applyAllListenersToVariable(m_nvktint);

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        // If the qualifier is not defined (0), do not further initialize sub-fields.
        if (getDecValue() == 0)
            return this;

        q_nvkvintset.initValueSet(s);
        n_iter.initValueSet(s);
        l_nvkrint.initValueSet(s);
        m_nvkrint.initValueSet(s);
        n_iter2.initValueSet(s);
        m_nvktint.initValueSet(s);

        n_iter.setWRAPINT(1);
        n_iter2.setWRAPINT(1);
        return this;
    }

    /**
     * Builds and returns the UI component for this packet using MigLayout.
     * <p>
     * The layout consists of:
     * <ul>
     *   <li>A base component from the superclass (expected to contain a combo box)</li>
     *   <li>A sub-panel for additional variable components arranged in two columns</li>
     * </ul>
     * The combo box toggles the visibility of extra sub-components based on its selection.
     * </p>
     *
     * @param comment an optional comment appended to the title
     * @return the constructed UI component
     */
    @Override
    public Component getComponent(String comment) {
        // Main panel with vertical layout and insets.
        JPanel mainPanel = new JPanel(new MigLayout("wrap 1, insets 10", "[grow]", "[]10[]"));

        // Get the base component from super, which should include the combo box.
        Component baseComponent = super.getComponent(comment);
        mainPanel.add(baseComponent, "growx, wrap");

        // Create a sub-panel for the variable sub-components.
        JPanel variablePanel = new JPanel(new MigLayout("wrap 2, insets 0", "[grow][grow]", "[]10[]"));

        // Retrieve individual component views.
        Component compQ_NVKVINTSET = q_nvkvintset.getComponent();
        Component compN_ITER = n_iter.getComponent();
        Component compL_NVKRINT = l_nvkrint.getComponent();
        Component compM_NVKRINT = m_nvkrint.getComponent();
        Component compN_ITER2 = n_iter2.getComponent();
        Component compM_NVKTINT = m_nvktint.getComponent();

        // Add components to the variable panel.
        variablePanel.add(compQ_NVKVINTSET, "growx, span 2, wrap");
        variablePanel.add(compN_ITER, "growx, span 2, wrap");
        variablePanel.add(compL_NVKRINT, "growx");
        variablePanel.add(compM_NVKRINT, "growx, wrap");
        variablePanel.add(compN_ITER2, "growx, span 2, wrap");
        variablePanel.add(compM_NVKTINT, "growx, span 2, wrap");

        mainPanel.add(variablePanel, "growx, wrap");

        // Extract the combo box from the base component for toggling extra fields.
        if (baseComponent instanceof JPanel) {
            JPanel basePanel = (JPanel) baseComponent;
            if (basePanel.getComponentCount() > 1 && basePanel.getComponent(1) instanceof JComboBox) {
                @SuppressWarnings("unchecked")
                JComboBox<String> comboBox = (JComboBox<String>) basePanel.getComponent(1);

                new InputJCombobox(comboBox);

                comboBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int selectedIndex = comboBox.getSelectedIndex();
                        setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(selectedIndex), getMaxSize()));
                        // Toggle visibility: show extra fields only if "Osobní vlak" (index 1) is selected.
                        boolean showExtras = (selectedIndex == 1);
                        compQ_NVKVINTSET.setVisible(showExtras);
                        compN_ITER.setVisible(showExtras);
                        compL_NVKRINT.setVisible(showExtras);
                        compM_NVKRINT.setVisible(showExtras);
                        compN_ITER2.setVisible(showExtras);
                        compM_NVKTINT.setVisible(showExtras);
                    }
                });
                // Set initial visibility.
                boolean showExtras = (comboBox.getSelectedIndex() == 1);
                compQ_NVKVINTSET.setVisible(showExtras);
                compN_ITER.setVisible(showExtras);
                compL_NVKRINT.setVisible(showExtras);
                compM_NVKRINT.setVisible(showExtras);
                compN_ITER2.setVisible(showExtras);
                compM_NVKTINT.setVisible(showExtras);
            }
        }

        return mainPanel;
    }

    @Override
    public Component getComponent() {
        return getComponent("");
    }

    @Override
    public Variables deepCopy() {
        Q_NVKINT copy = new Q_NVKINT();

        // Copy listeners first
        copy.subvariableListeners.addAll(this.subvariableListeners);

        copy.setBinValue(getBinValue());

        // Create new subvariables
        copy.q_nvkvintset = (Q_NVKVINTSET) new Q_NVKVINTSET().initValueSet(new String[]{q_nvkvintset.getBinValue()}).setCond(copy, 1);
        copy.n_iter = (N_ITER) new N_ITER("Kategorie vlaků")
                .addNewIterVar(new Q_NVKVINTSET())
                .setCond(copy, 1);
        copy.l_nvkrint = (L_NVKRINT) new L_NVKRINT().initValueSet(new String[]{l_nvkrint.getBinValue()}).setCond(copy, 1);
        copy.m_nvkrint = (M_NVKRINT) new M_NVKRINT().initValueSet(new String[]{m_nvkrint.getBinValue()}).setCond(copy, 1);
        copy.n_iter2 = (N_ITER) new N_ITER().setCond(copy, 1);
        copy.n_iter2.addNewIterVar(new L_NVKRINT().setCond(copy, 1))
                .addNewIterVar(new M_NVKRINT().setCond(copy, 1));
        copy.m_nvktint = (M_NVKTINT) new M_NVKTINT().initValueSet(new String[]{m_nvktint.getBinValue()}).setCond(copy, 1);

        copy.n_iter.setWRAPINT(1);
        copy.n_iter2.setWRAPINT(1);

        // Apply all listeners to the copied subvariables
        copy.applyAllListenersToVariable(copy.q_nvkvintset);
        copy.applyAllListenersToVariable(copy.n_iter);
        copy.applyAllListenersToVariable(copy.l_nvkrint);
        copy.applyAllListenersToVariable(copy.m_nvkrint);
        copy.applyAllListenersToVariable(copy.n_iter2);
        copy.applyAllListenersToVariable(copy.m_nvktint);

        return copy;
    }

    @Override
    public String getFullData() {
        StringBuilder sb = new StringBuilder();
        sb.append(getBinValue());
        sb.append(q_nvkvintset.getFullData());
        sb.append(n_iter.getFullData());
        sb.append(l_nvkrint.getFullData());
        sb.append(m_nvkrint.getFullData());
        sb.append(n_iter2.getFullData());
        sb.append(m_nvktint.getFullData());
        return sb.toString();
    }

    @Override
    public String getSimpleView() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getSimpleView());
        sb.append(q_nvkvintset.getSimpleView());
        sb.append(n_iter.getSimpleView());
        sb.append(l_nvkrint.getSimpleView());
        sb.append(m_nvkrint.getSimpleView());
        sb.append(n_iter2.getSimpleView());
        sb.append(m_nvktint.getSimpleView());
        return sb.toString();
    }

    /**
     * Gets all subvariables as a list for convenient iteration.
     *
     * @return a list containing all subvariables
     */
    public List<Variables> getAllSubvariables() {
        List<Variables> subvariables = new ArrayList<>();
        if (q_nvkvintset != null) subvariables.add(q_nvkvintset);
        if (n_iter != null) subvariables.add(n_iter);
        if (l_nvkrint != null) subvariables.add(l_nvkrint);
        if (m_nvkrint != null) subvariables.add(m_nvkrint);
        if (n_iter2 != null) subvariables.add(n_iter2);
        if (m_nvktint != null) subvariables.add(m_nvktint);
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
    public Q_NVKVINTSET getQ_nvkvintset() { return q_nvkvintset; }
    public N_ITER getN_iter() { return n_iter; }
    public N_ITER getN_iter2() { return n_iter2; }
    public L_NVKRINT getL_nvkrint() { return l_nvkrint; }
    public M_NVKRINT getM_nvkrint() { return m_nvkrint; }
    public M_NVKTINT getM_nvktint() { return m_nvktint; }
}