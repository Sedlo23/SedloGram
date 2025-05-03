package packets.Var.N;

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

    public N_ITER() {
        this("Iterace");
    }

    public N_ITER(String nameOfIteration) {
        super("N_ITER", 5, "Number of iterations of a data set following this variable in a packet");
        this.nameOfIteration = nameOfIteration;


    }

    public N_ITER addNewIterVar(Variables newVar) {
        template.add(newVar instanceof N_ITER ? (N_ITER) newVar : newVar);
        return this;
    }

    @Override
    public void setBinValue(String binValue) {
        super.setBinValue(binValue);
        adjustDataSize();
    }

    @Override
    public Variables deepCopy() {
        N_ITER copy = new N_ITER();
        copy.template = template;
        copy.initValueSet(new String[]{getBinValue()});
        copy.setWRAPINT(getWRAPINT());
        copy.data.addAll(this.data);
        copy.setBinValue(this.getBinValue());
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
       // scrollPane.setPreferredSize(new Dimension(200, 300));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Iteration Data"));

        mainPanel.add(scrollPane, "grow");

        // Panel to show details of selected iteration
        JPanel iterationDetailPanel = new JPanel(new MigLayout("wrap 1, fillx", "[grow]"));
        iterationDetailPanel.setBorder(BorderFactory.createTitledBorder("Iteration Details"));

        mainPanel.add(iterationDetailPanel, "grow");

        JComboBox j =((JComboBox<?>) ((JPanel) control).getComponent(1));


        ((JComboBox<?>) ((JPanel) control).getComponent(1)).addActionListener(e -> {
            updateListModel(scrollPane, jList,iterationDetailPanel);
            updateIterationPanelVisibility(iterationDetailPanel);
        });



        // Listener for JList selections to update details
        jList.addListSelectionListener(e -> updateIterationPanel(iterationDetailPanel, jList));

        // Initial updates
        updateListModel(scrollPane, jList,iterationDetailPanel);
        updateIterationPanelVisibility(iterationDetailPanel);

        return mainPanel;
    }


    private void adjustDataSize() {
        int targetSize = getDecValue();

        while (data.size() < targetSize)
        {
            data.add(copyList());
        }

        while (data.size() > targetSize) {
            data.remove(data.size() - 1);
        }


    }

    private IterationData copyList() {
        IterationData copy = new IterationData(data, nameOfIteration);
        for (Variables var : template) {
            copy.add(var instanceof N_ITER ? ((N_ITER) var).deepCopy() : var.deepCopy());
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

    private void updateIterationPanel(JPanel iterationPanel,JList<IterationData> jList) {
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
                //  data.indexOf(iterData);
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
        data.forEach(iterData -> iterData.forEach(var -> var.initValueSet(values)));
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
}
