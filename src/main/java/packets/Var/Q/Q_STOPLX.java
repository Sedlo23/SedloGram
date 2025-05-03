package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.L.L_STOPLX;
import packets.Var.Variables;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static tools.ui.GUIHelper.setTitle;


public class Q_STOPLX extends Variables {
    private L_STOPLX nid_c;

    public Q_STOPLX() {
        super("Q_STOPLX",
                1,
                "Označuje, zda je nutné zastavit vlak za nechráněným LX");

        nid_c = new L_STOPLX();

    }


    @Override
    public Variables deepCopy() {

        Q_STOPLX tmp = new Q_STOPLX();

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

        Component NID_C_comp = nid_c.getComponent(com);

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


        // new InputJCombobox(jComboBox);
        // panel.add(jComboBox);
        panel.add(jComboBox);
        panel.add(NID_C_comp);


        return setTitle(panel, "Q_STOPLX");

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
        s.add("Nevyžadováno");
        s.add("Vyžadováno");


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
