package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.Variables;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import static tools.ui.GUIHelper.addLabel;


public class Q_DIFF extends Variables {

    NC_CDDIFF nc_cddiff;

    public Q_DIFF() {
        super("Q_DIFF",
                2,
                "Označuje typ konkrétní kategorie SSP\n" +
                        "V případě \"jiného specifického\" SSP sděluje palubnímu zařízení ERTMS/ETCS, zda nahrazuje nebo nenahrazuje SSP pro nedostatky v Cant, jak byl zvolen palubním zařízením (viz 3.11.3.2.3), pokud vlak patří do kategorie vlaků \"jiný mezinárodní\", na kterou se vztahuje \"jiný specifický\" SSP.");

        nc_cddiff = new NC_CDDIFF();
    }

    @Override
    public Variables deepCopy() {

        Q_DIFF tmp = new Q_DIFF();

        tmp.setBinValue(getBinValue());

        return tmp;
    }

   Component component;

    @Override
    public Component getComponent(String com) {

        JPanel panel = new JPanel();

        panel.setLayout(new FlowLayout(FlowLayout.LEADING));

        JComboBox   jComboBox = (JComboBox) ((JPanel) super.getComponent(com)).getComponent(1);

        Component NID_C_comp = nc_cddiff.getComponent(com);

        JComboBox jComboBox2 = (JComboBox) ((JPanel) NID_C_comp).getComponent(1);

        panel.add(jComboBox,BorderLayout.NORTH);

        panel.add(jComboBox2,BorderLayout.SOUTH);
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

        component =  addLabel(panel, "Q_DIFF","",new JLabel());

        return component;

    }

    @Override
    public Variables initValueSet(String[] s) {

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

    public NC_CDDIFF getNc_cddiff() {
        return nc_cddiff;
    }
}