package packets.Var.N;

import packets.Var.BinaryChangeListener;
import tools.ui.GUIHelper;
import tools.string.StringHelper;
import packets.Interfaces.IterationData;
import packets.Var.Variables;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class N_ITER extends Variables {

    private ArrayList<Variables> template = new ArrayList<>();
    private ArrayList<IterationData> data = new ArrayList<>();

    private String nameOfIteration;
    private int WRAPINT = 3;

    /** Listeners that will be propagated to child variables */
    private final ArrayList<BinaryChangeListener> childListeners = new ArrayList<>();

    public N_ITER() {
        this("Iterace");
    }

    public N_ITER(String nameOfIteration) {
        super("N_ITER", 5, "Number of iterations of a data set following this variable in a packet");
        this.nameOfIteration = nameOfIteration;

        // Add default listener to track changes in child variables
        addDefaultChildListener();
    }

    /**
     * Adds a listener that will be automatically applied to all child variables
     * (both template and iteration data variables).
     *
     * @param listener the listener to add to all children
     * @return this N_ITER instance for chaining
     */
    public N_ITER addChildListener(BinaryChangeListener listener) {
        if (listener != null) {
            childListeners.add(listener);

            // Apply to existing template variables
            template.forEach(var -> var.addBinaryChangeListener(listener));

            // Apply to existing iteration data variables
            data.forEach(iterData ->
                    iterData.forEach(var -> applyListenerRecursively(var, listener))
            );
        }
        return this;
    }

    /**
     * Removes a listener from all child variables and from the child listeners list.
     *
     * @param listener the listener to remove
     * @return true if the listener was found and removed
     */
    public boolean removeChildListener(BinaryChangeListener listener) {
        boolean removed = childListeners.remove(listener);

        if (removed) {
            // Remove from existing template variables
            template.forEach(var -> var.removeBinaryChangeListener(listener));

            // Remove from existing iteration data variables
            data.forEach(iterData ->
                    iterData.forEach(var -> removeListenerRecursively(var, listener))
            );
        }

        return removed;
    }

    /**
     * Applies a listener recursively to a variable and its children if it's an N_ITER.
     */
    private void applyListenerRecursively(Variables variable, BinaryChangeListener listener) {
        variable.addBinaryChangeListener(listener);
        if (variable instanceof N_ITER) {
            ((N_ITER) variable).addChildListener(listener);
        }
    }

    /**
     * Removes a listener recursively from a variable and its children if it's an N_ITER.
     */
    private void removeListenerRecursively(Variables variable, BinaryChangeListener listener) {
        variable.removeBinaryChangeListener(listener);
        if (variable instanceof N_ITER) {
            ((N_ITER) variable).removeChildListener(listener);
        }
    }

    /**
     * Applies all registered child listeners to a variable.
     *
     * @param variable the variable to apply listeners to
     */
    private void applyChildListeners(Variables variable) {
        childListeners.forEach(listener -> applyListenerRecursively(variable, listener));
    }

    /**
     * Adds a default listener that logs child variable changes.
     */
    private void addDefaultChildListener() {
        addChildListener((source, oldValue, newValue) -> {
            System.out.println(String.format(
                    "Child variable '%s' in iteration '%s' changed from '%s' to '%s'",
                    source.getName(), nameOfIteration, oldValue, newValue
            ));
        });
    }

    public N_ITER addNewIterVar(Variables newVar) {
        Variables varToAdd = newVar instanceof N_ITER ? (N_ITER) newVar : newVar;

        // Apply all child listeners to the new variable
        applyChildListeners(varToAdd);

        template.add(varToAdd);

        // Update existing iteration data with the new variable
        updateExistingIterationsWithNewVariable(varToAdd);

        return this;
    }

    /**
     * Updates existing iterations by adding a copy of the new variable to each.
     *
     * @param newVar the new variable to add to existing iterations
     */
    private void updateExistingIterationsWithNewVariable(Variables newVar) {
        for (IterationData iterData : data) {
            Variables copy = createDeepCopyWithListeners(newVar);
            iterData.add(copy);
        }
    }

    /**
     * Creates a deep copy of a variable and ensures all listeners are applied.
     */
    private Variables createDeepCopyWithListeners(Variables var) {
        Variables copy = var instanceof N_ITER ?
                ((N_ITER) var).deepCopy() : var.deepCopy();

        applyChildListeners(copy);


        return copy;
    }

    @Override
    public void setBinValue(String binValue) {
        String oldBinValue = getBinValue();
        super.setBinValue(binValue);
        adjustDataSize();
        notifyBinaryChangeListeners(oldBinValue, binValue);
    }

    @Override
    public Variables deepCopy() {
        N_ITER copy = new N_ITER(nameOfIteration);

        // Copy child listeners FIRST before creating any variables
        copy.childListeners.addAll(this.childListeners);

        // Copy template variables and apply listeners
        for (Variables var : template) {
            Variables varCopy = createDeepCopyWithListeners(var);
            copy.template.add(varCopy);
        }

        copy.setWRAPINT(getWRAPINT());

        // Set the value AFTER copying listeners and template
        copy.setBinValue(this.getBinValue());

        // Deep copy existing data and apply listeners
        for (IterationData iterData : this.data) {
            IterationData dataCopy = new IterationData(copy.data, nameOfIteration);
            for (Variables var : iterData) {
                Variables varCopy = createDeepCopyWithListeners(var);
                dataCopy.add(varCopy);
            }
            copy.data.add(dataCopy);
        }

        return copy;
    }

    JList<IterationData> jList = new JList<>();

    @Override
    public Component getComponent() {
        JPanel mainPanel = new JPanel(new MigLayout("fill, insets 0, hidemode 3", "[grow,fill][grow,fill]", "[][grow,fill]"));

        // Control component
        Component control = super.getComponent();
        mainPanel.add(control, "span 2, wrap, growx");

        // JList to show iteration data
        JList<IterationData> jList = new JList<>();
        JScrollPane scrollPane = new JScrollPane(jList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Iteration Data"));

        mainPanel.add(scrollPane, "grow");

        // Panel to show details of selected iteration
        JPanel iterationDetailPanel = new JPanel(new MigLayout("wrap 1, fillx", "[grow]"));
        iterationDetailPanel.setBorder(BorderFactory.createTitledBorder("Iteration Details"));

        mainPanel.add(iterationDetailPanel, "grow");

        JComboBox j = ((JComboBox<?>) ((JPanel) control).getComponent(1));

        ((JComboBox<?>) ((JPanel) control).getComponent(1)).addActionListener(e -> {
            updateListModel(scrollPane, jList, iterationDetailPanel);
            updateIterationPanelVisibility(iterationDetailPanel);
        });

        // Listener for JList selections to update details
        jList.addListSelectionListener(e -> updateIterationPanel(iterationDetailPanel, jList));

        // Initial updates
        updateListModel(scrollPane, jList, iterationDetailPanel);
        updateIterationPanelVisibility(iterationDetailPanel);

        return mainPanel;
    }

    private void adjustDataSize() {
        int targetSize = getDecValue();

        while (data.size() < targetSize) {
            data.add(copyList());
        }

        while (data.size() > targetSize) {
            data.remove(data.size() - 1);
        }
    }

    private IterationData copyList() {
        IterationData copy = new IterationData(data, nameOfIteration);
        for (Variables var : template) {
            Variables varCopy = createDeepCopyWithListeners(var);
            copy.add(varCopy);
        }
        return copy;
    }

    private void updateListModel(JScrollPane scrollPane, JList<IterationData> jList, JPanel iterationDetailPanel) {
        DefaultListModel<IterationData> model = new DefaultListModel<>();
        model.addAll(data);
        jList.setModel(model);

        boolean hasData = !model.isEmpty();
        scrollPane.setVisible(hasData);
        iterationDetailPanel.setVisible(hasData);
    }

    private void updateIterationPanelVisibility(JPanel iterationPanel) {
        iterationPanel.setVisible(getDecValue() != 0);
    }

    private void updateIterationPanel(JPanel iterationPanel, JList<IterationData> jList) {
        JPanel contentPanel = new JPanel(new MigLayout("wrap 1", "[]0[]"));

        // Use bulk update to minimize revalidations and repaints
        SwingUtilities.invokeLater(() -> {
            for (IterationData iterData : jList.getSelectedValuesList()) {
                JPanel innerPanel = createInnerPanel(iterData);
                contentPanel.add(GUIHelper.setTitle2(innerPanel, iterData.toString()));
                contentPanel.add(new JSeparator());
            }

            iterationPanel.removeAll();
            iterationPanel.add(contentPanel);
            iterationPanel.revalidate();
            iterationPanel.repaint();
        });
    }

    private JPanel createInnerPanel(IterationData iterData) {
        JPanel innerPanel = new JPanel(new MigLayout("wrap " + WRAPINT, "10[]10[]"));

        for (Variables var : iterData) {
            Component component;

            if (var instanceof N_ITER) {
                component = var.getComponent();
                innerPanel.add(component, "span, growx");
            } else {
                // Regular variable component creation
                component = var.getComponent();
                innerPanel.add(component);
            }
        }

        return innerPanel;
    }

    private JPanel createIterationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Iterace"));
        return panel;
    }

    public String getNameOfIteration() {
        return nameOfIteration;
    }

    public N_ITER setNameOfIteration(String nameOfIteration) {
        this.nameOfIteration = nameOfIteration;
        return this;
    }

    @Override
    public Variables initValueSet(String[] values) {
        setBinValue(StringHelper.TrimAR(values, getMaxSize()));

        // Apply listeners to all existing iteration data
        data.forEach(iterData ->
                iterData.forEach(var -> {
                    var.initValueSet(values);
                    // Ensure listeners are applied after initialization
                    applyChildListeners(var);
                })
        );

        return this;
    }

    @Override
    public String getFullData() {
        StringBuilder result = new StringBuilder(getBinValue());
        data.forEach(iterData -> iterData.forEach(var -> result.append(var.getFullData())));
        return result.toString();
    }

    public ArrayList<Variables> getTemplate() {
        return template;
    }

    public void setTemplate(ArrayList<Variables> template) {
        this.template = template;
        // Apply listeners to all template variables
        template.forEach(this::applyChildListeners);
    }

    public ArrayList<IterationData> getData() {
        return data;
    }

    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> combo = new ArrayList<>();
        for (int i = 0; i < Math.pow(2, getMaxSize()); i++) {
            combo.add(i + "x iterací");
        }
        return combo;
    }

    @Override
    public String getSimpleView() {
        StringBuilder result = new StringBuilder(super.getSimpleView());
        data.forEach(iterData -> iterData.forEach(var -> result.append(var.getSimpleView())));
        return result.toString();
    }

    public int getWRAPINT() {
        return WRAPINT;
    }

    public N_ITER setWRAPINT(int WRAPINT) {
        this.WRAPINT = WRAPINT;
        return this;
    }

    /**
     * Gets the number of registered child listeners.
     *
     * @return the number of child listeners
     */
    public int getChildListenerCount() {
        return childListeners.size();
    }

    /**
     * Clears all child listeners from this N_ITER and all its child variables.
     */
    public void clearAllChildListeners() {
        // Remove from template variables
        template.forEach(var -> childListeners.forEach(listener ->
                removeListenerRecursively(var, listener)));

        // Remove from iteration data variables
        data.forEach(iterData ->
                iterData.forEach(var -> childListeners.forEach(listener ->
                        removeListenerRecursively(var, listener)))
        );

        childListeners.clear();
    }
}