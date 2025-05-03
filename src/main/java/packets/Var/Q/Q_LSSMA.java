package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.T.T_LSSMA;
import packets.Var.Variables;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static tools.ui.GUIHelper.setTitle;


public class Q_LSSMA extends Variables {
    private T_LSSMA nid_c;

    public Q_LSSMA() {
        super("Q_LSSMA",
                1,
                "Tento kvalifikátor určuje, zda má palubní počítač zapnout/vypnout zobrazení nejnižší kontrolované rychlosti v rámci MA.");

        nid_c = new T_LSSMA();

    }


    @Override
    public Variables deepCopy() {

        Q_LSSMA tmp = new Q_LSSMA();

        tmp.setBinValue(getBinValue());

        return tmp;
    }

    @Override
    public Component getComponent(String com) {

        JPanel panel = new JPanel();

        JPanel ini = ((JPanel) super.getComponent(com));

        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

        //panel.add(ini);


        Component NID_C_comp =  ((JPanel)nid_c.getComponent(com)).getComponent(1);

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox.getSelectedIndex() == 1)
                    NID_C_comp.setVisible(true);
                else
                    NID_C_comp.setVisible(false);
            }
        });

        if (jComboBox.getSelectedIndex() == 1)
            NID_C_comp.setVisible(true);
        else
            NID_C_comp.setVisible(false);

        panel.add(jComboBox);

        panel.add(NID_C_comp);


        return setTitle(panel, getName());

    }

    @Override
    public Variables initValueSet(String[] s) {


        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() == 1)
            nid_c.initValueSet(s);

        return this;
    }


    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();
        s.add("Vypnout");
        s.add("Zapnout");


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
}
