package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.V.V_LX;
import packets.Var.Variables;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static tools.ui.GUIHelper.setTitle;

public class Q_LXSTATUS extends Variables {
    private V_LX nid_c;
    private Q_STOPLX nid_c2;

    public Q_LXSTATUS() {
        super("Q_LXSTATUS",
                1, "Indicates whether the LX is protected or not"
        );

        nid_c = new V_LX();
        nid_c2 = new Q_STOPLX();

    }


    @Override
    public Variables deepCopy() {

        Q_LXSTATUS tmp = new Q_LXSTATUS();

        tmp.setBinValue(getBinValue());

        return tmp;
    }

    @Override
    public Component getComponent(String com) {

        JPanel panel = new JPanel();

        JPanel ini = ((JPanel) super.getComponent(com));

        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

      //  panel.add(ini);


        Component NID_C_comp = nid_c.getComponent(com);
        Component NID_C_comp2 = nid_c2.getComponent(com);

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox.getSelectedIndex() == 1) {
                    NID_C_comp.setVisible(true);
                    NID_C_comp2.setVisible(true);
                } else {
                    NID_C_comp.setVisible(false);
                    NID_C_comp2.setVisible(false);
                }
            }
        });

        if (jComboBox.getSelectedIndex() == 1) {
            NID_C_comp.setVisible(true);
            NID_C_comp2.setVisible(true);
        } else {
            NID_C_comp.setVisible(false);
            NID_C_comp2.setVisible(false);
        }

        panel.add(jComboBox);

        panel.add(NID_C_comp);
        panel.add(NID_C_comp2);

        return setTitle(panel, "Q_LXSTATUS");

    }

    @Override
    public Variables initValueSet(String[] s) {


        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() == 1) {

            nid_c.initValueSet(s);
            nid_c2.initValueSet(s);
        }

        return this;
    }


    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();
        s.add("Zabeznečený");
        s.add("Nezabeznečený");


        return s;

    }

    @Override
    public String getFullData() {

        String tmp = getBinValue();

        if (getDecValue() == 1) {
            tmp += nid_c.getFullData();
            tmp += nid_c2.getFullData();
        }


        return tmp;
    }

    @Override
    public String getSimpleView() {


        String tmp = super.getSimpleView();

        if (getDecValue() == 1) {
            tmp += nid_c.getSimpleView();
            tmp += nid_c2.getSimpleView();
        }


        return tmp;
    }
}
