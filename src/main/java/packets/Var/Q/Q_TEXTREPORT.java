package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.NID.NID_C;
import packets.Var.NID.NID_RBC;
import packets.Var.NID.NID_TEXTMESSAGE;
import packets.Var.Variables;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static tools.ui.GUIHelper.setTitle;


public class Q_TEXTREPORT extends Variables {
    private NID_C nid_c;
    private NID_RBC nid_rbc;
    private NID_TEXTMESSAGE nid_textmessage;


    public Q_TEXTREPORT() {
        super("Q_TEXTREPORT",
                1,
                "Kvalifikátor pro hlášení potvrzení textu řidičem");

        nid_c = new NID_C();
        nid_rbc = new NID_RBC();
        nid_textmessage = new NID_TEXTMESSAGE();

    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();
        s.add("Není vyžadováno");
        s.add("Vyžaduje");

        return s;

    }

    @Override
    public Variables deepCopy() {

        Q_TEXTREPORT tmp = new Q_TEXTREPORT();

        tmp.setBinValue(getBinValue());

        return tmp;
    }

    @Override
    public Component getComponent(String com) {

        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel ini = ((JPanel) super.getComponent(com));

        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

      //  panel.add(ini);

        Component NID_C_comp = nid_textmessage.getComponent(com);
        Component NID_C_comp1 = nid_c.getComponent(com);
        Component NID_C_comp2 = nid_rbc.getComponent(com);

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox.getSelectedIndex() == 1) {
                    NID_C_comp.setVisible(true);
                    NID_C_comp1.setVisible(true);
                    NID_C_comp2.setVisible(true);

                } else {
                    NID_C_comp.setVisible(false);
                    NID_C_comp1.setVisible(false);
                    NID_C_comp2.setVisible(false);
                }
            }
        });

        if (jComboBox.getSelectedIndex() == 1) {
            NID_C_comp.setVisible(true);
            NID_C_comp1.setVisible(true);
            NID_C_comp2.setVisible(true);

        } else {
            NID_C_comp.setVisible(false);
            NID_C_comp1.setVisible(false);
            NID_C_comp2.setVisible(false);
        }

        panel.add(jComboBox);

        panel.add(NID_C_comp);
        panel.add(NID_C_comp1);
        panel.add(NID_C_comp2);


        return setTitle(panel, "Q_TEXTREPORT");

    }

    @Override
    public Variables initValueSet(String[] s) {


        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() == 1) {
            nid_textmessage.initValueSet(s);
            nid_c.initValueSet(s);
            nid_rbc.initValueSet(s);
        }
        return this;
    }


    @Override
    public String getFullData() {

        String tmp = getBinValue();

        if (getDecValue() == 1) {
            tmp += nid_textmessage.getFullData();
            tmp += nid_c.getFullData();
            tmp += nid_rbc.getFullData();
        }
        return tmp;
    }

    @Override
    public String getSimpleView() {


        String tmp = super.getSimpleView();

        if (getDecValue() == 1) {
            tmp += nid_textmessage.getSimpleView();
            tmp += nid_c.getSimpleView();
            tmp += nid_rbc.getSimpleView();
        }
        return tmp;
    }
}
