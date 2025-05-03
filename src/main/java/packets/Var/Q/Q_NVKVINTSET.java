package packets.Var.Q;

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

import static tools.ui.GUIHelper.addLabel;
import static tools.ui.GUIHelper.setTitle;

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

    /**
     * Default constructor that initializes subfields with default conditions.
     */
    public Q_NVKVINTSET() {
        super("Q_NVKVINTSET", 2, "Type of Kv_int set");
        a_nvp12 = (A_NVP12) new A_NVP12().setCond(this, 1);
        a_nvp23 = (A_NVP23) new A_NVP23().setCond(this, 1);
        v_nvkvint = new V_NVKVINT();
        m_nvkvint1 = new M_NVKVINT();
        m_nvkvint2 = (M_NVKVINT) new M_NVKVINT().setCond(this, 1);
        n_iter = new N_ITER("Hodnoty pro výpočet křivek")
                .addNewIterVar(new V_NVKVINT())
                .addNewIterVar(new M_NVKVINT())
                .addNewIterVar(new M_NVKVINT().setCond(this, 1));
    }

    /**
     * Initializes subfields using the provided binary data array.
     *
     * @param s the binary data array
     * @return this instance after initialization
     */
    @Override
    public Variables initValueSet(String[] s) {
        a_nvp12 = (A_NVP12) new A_NVP12().setCond(this, 1);
        a_nvp23 = (A_NVP23) new A_NVP23().setCond(this, 1);
        v_nvkvint = new V_NVKVINT();
        m_nvkvint1 = new M_NVKVINT();
        m_nvkvint2 = (M_NVKVINT) new M_NVKVINT().setCond(this, 1);
        n_iter = new N_ITER("Hodnoty pro výpočet křivek")
                .addNewIterVar(new V_NVKVINT())
                .addNewIterVar(new M_NVKVINT())
                .addNewIterVar(new M_NVKVINT().setCond(this, 1));

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
        copy.setBinValue(getBinValue());

        copy.a_nvp12 = (A_NVP12) new A_NVP12().initValueSet(new String[]{a_nvp12.getBinValue()}).setCond(this, 1);
        copy.a_nvp23 = (A_NVP23) new A_NVP23().initValueSet(new String[]{a_nvp23.getBinValue()}).setCond(this, 1);
        copy.v_nvkvint = (V_NVKVINT) new V_NVKVINT().initValueSet(new String[]{v_nvkvint.getBinValue()});
        copy.m_nvkvint1 = (M_NVKVINT) new M_NVKVINT().initValueSet(new String[]{m_nvkvint1.getBinValue()});
        copy.m_nvkvint2 = (M_NVKVINT) new M_NVKVINT().initValueSet(new String[]{m_nvkvint2.getBinValue()}).setCond(this, 1);
        copy.n_iter.setTemplate(n_iter.getTemplate());
        copy.n_iter.initValueSet(new String[]{n_iter.getBinValue()});
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
}
