package packets.Var.Q;

import packets.Var.L.L_NVKRINT;
import packets.Var.M.M_NVKRINT;
import packets.Var.M.M_NVKTINT;
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

import static tools.ui.GUIHelper.setTitle;

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

    /**
     * Default constructor.
     */
    public Q_NVKINT() {
        super("Q_NVKINT", 1, "Qualifier for integrated correction factors");

        q_nvkvintset = (Q_NVKVINTSET) new Q_NVKVINTSET().setCond(this, 1);
        n_iter = (N_ITER) new N_ITER("Kategorie vlaků")
                .addNewIterVar(new Q_NVKVINTSET())
                .setCond(this, 1);
        l_nvkrint = (L_NVKRINT) new L_NVKRINT().setCond(this, 1);
        m_nvkrint = (M_NVKRINT) new M_NVKRINT().setCond(this, 1);
        n_iter2 = (N_ITER) new N_ITER().setCond(this, 1);
        n_iter.setWRAPINT(1);
        n_iter2.setWRAPINT(1);
        n_iter2.addNewIterVar(new L_NVKRINT().setCond(this, 1))
                .addNewIterVar(new M_NVKRINT().setCond(this, 1));
        m_nvktint = (M_NVKTINT) new M_NVKTINT().setCond(this, 1);
    }

    /**
     * Initializes sub-variables using the provided data array.
     *
     * @param s the binary data array
     * @return this instance after initialization
     */
    @Override
    public Variables initValueSet(String[] s) {
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
        copy.setBinValue(getBinValue());

        copy.q_nvkvintset = (Q_NVKVINTSET) new Q_NVKVINTSET().initValueSet(new String[]{q_nvkvintset.getBinValue()}).setCond(this, 1);
        copy.n_iter = (N_ITER) new N_ITER("Kategorie vlaků")
                .addNewIterVar(new Q_NVKVINTSET())
                .setCond(this, 1);
        copy.l_nvkrint = (L_NVKRINT) new L_NVKRINT().initValueSet(new String[]{l_nvkrint.getBinValue()}).setCond(this, 1);
        copy.m_nvkrint = (M_NVKRINT) new M_NVKRINT().initValueSet(new String[]{m_nvkrint.getBinValue()}).setCond(this, 1);
        copy.n_iter2 = (N_ITER) new N_ITER().setCond(this, 1);
        copy.n_iter2.addNewIterVar(new L_NVKRINT().setCond(this, 1))
                .addNewIterVar(new M_NVKRINT().setCond(this, 1));
        copy.m_nvktint = (M_NVKTINT) new M_NVKTINT().initValueSet(new String[]{m_nvktint.getBinValue()}).setCond(this, 1);
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
}
