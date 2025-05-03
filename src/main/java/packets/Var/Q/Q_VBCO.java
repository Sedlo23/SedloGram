package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.NID.NID_C;
import packets.Var.NID.NID_VBCMK;
import packets.Var.T.T_VBC;
import packets.Var.Variables;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static tools.ui.GUIHelper.setTitle;


public class Q_VBCO extends Variables {
    private NID_C nid_c;

    private NID_VBCMK nid_vbcmk;

    private T_VBC t_vbc;


    public Q_VBCO() {
        super("Q_VBCO",
                1,
                "Kvalifikátor pro nastavení nebo odebrání VBC");

        nid_c = new NID_C();

        nid_vbcmk = new NID_VBCMK();

        t_vbc = new T_VBC();


    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Zrušit");
        s.add("Nastavit");


        return s;

    }

    @Override
    public Variables deepCopy() {

        Q_VBCO tmp = new Q_VBCO();

        tmp.setBinValue(getBinValue());

        return tmp;
    }

    @Override
    public Component getComponent(String com) {

        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel ini = ((JPanel) super.getComponent(com));

        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

       // panel.add(ini);

        Component NID_C_comp = nid_vbcmk.getComponent(com);
        Component NID_C_comp1 = nid_c.getComponent(com);
        Component NID_C_comp2 = t_vbc.getComponent(com);

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox.getSelectedIndex() == 1) {

                    NID_C_comp2.setVisible(true);

                } else {

                    NID_C_comp2.setVisible(false);
                }
            }
        });

        if (jComboBox.getSelectedIndex() == 1) {

            NID_C_comp2.setVisible(true);

        } else {

            NID_C_comp2.setVisible(false);
        }

        panel.add(jComboBox);

        panel.add(NID_C_comp);
        panel.add(NID_C_comp1);
        panel.add(NID_C_comp2);


        return setTitle(panel, getName());

    }

    @Override
    public Variables initValueSet(String[] s) {


        setBinValue(StringHelper.TrimAR(s, getMaxSize()));
        nid_vbcmk.initValueSet(s);
        nid_c.initValueSet(s);

        if (getDecValue() == 1) {

            t_vbc.initValueSet(s);
        }
        return this;
    }


    @Override
    public String getFullData() {

        String tmp = getBinValue();
        tmp += nid_vbcmk.getFullData();
        tmp += nid_c.getFullData();
        if (getDecValue() == 1) {

            tmp += t_vbc.getFullData();
        }
        return tmp;
    }

    @Override
    public String getSimpleView() {


        String tmp = super.getSimpleView();
        tmp += nid_vbcmk.getSimpleView();
        tmp += nid_c.getSimpleView();
        if (getDecValue() == 1) {

            tmp += t_vbc.getSimpleView();
        }
        return tmp;
    }
}
