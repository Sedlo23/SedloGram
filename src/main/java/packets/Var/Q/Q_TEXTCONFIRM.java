package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.Variables;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static tools.ui.GUIHelper.setTitle;


public class Q_TEXTCONFIRM extends Variables {
    private Q_CONFTEXTDISPLAY nid_c;
    private Q_TEXTREPORT nid_rbc;


    public Q_TEXTCONFIRM() {
        super("Q_TEXTCONFIRM",
                2,
                "Kvalifikacer ");

        nid_c = new Q_CONFTEXTDISPLAY();
        nid_rbc = new Q_TEXTREPORT();


    }


    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();
        s.add("Není vyžadováno");
        s.add("Je vyžadováno");
        s.add("Je a provozní brzda");
        s.add("Je a Nouzová przda");

        return s;

    }

    @Override
    public Variables deepCopy() {

        Q_TEXTCONFIRM tmp = new Q_TEXTCONFIRM();

        tmp.setBinValue(getBinValue());

        return tmp;
    }

    @Override
    public Component getComponent(String com) {

        JPanel panel = new JPanel(new MigLayout("wrap 1"));



        JPanel ini = ((JPanel) super.getComponent(com));

        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

        //panel.add(ini);

        Component NID_C_comp1 = nid_c.getComponent(com);
        Component NID_C_comp2 = nid_rbc.getComponent(com);

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox.getSelectedIndex() != 0) {
                    NID_C_comp1.setVisible(true);
                    NID_C_comp2.setVisible(true);

                } else {
                    NID_C_comp1.setVisible(false);
                    NID_C_comp2.setVisible(false);
                }
            }
        });

        if (jComboBox.getSelectedIndex() != 0) {
            NID_C_comp1.setVisible(true);
            NID_C_comp2.setVisible(true);

        } else {
            NID_C_comp1.setVisible(false);
            NID_C_comp2.setVisible(false);
        }

        panel.add(jComboBox);

        panel.add(NID_C_comp1);
        panel.add(NID_C_comp2);


        return setTitle(panel, "Q_TEXTCONFIRM");

    }

    @Override
    public Variables initValueSet(String[] s) {


        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() != 0) {
            nid_c.initValueSet(s);
            nid_rbc.initValueSet(s);
        }
        return this;
    }


    @Override
    public String getFullData() {

        String tmp = getBinValue();

        if (getDecValue() != 0) {
            tmp += nid_c.getFullData();
            tmp += nid_rbc.getFullData();
        }
        return tmp;
    }


    @Override
    public String getSimpleView() {


        String tmp = super.getSimpleView();

        if (getDecValue() != 0) {
            tmp += nid_c.getSimpleView();
            tmp += nid_rbc.getSimpleView();
        }
        return tmp;
    }
}
