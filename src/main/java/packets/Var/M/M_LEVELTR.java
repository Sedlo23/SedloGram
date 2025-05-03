package packets.Var.M;

import tools.string.StringHelper;
import packets.Var.NID.NID_NTC;
import packets.Var.Variables;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static tools.ui.GUIHelper.addLabel;


public class M_LEVELTR extends Variables {
    private NID_NTC nid_ntc;
    public M_LEVELTR() {
        super("M_LEVELTR",
                3,
                "Požadovaná úroveň");

        nid_ntc = new NID_NTC();

    }

    @Override
    public Variables deepCopy() {


        M_LEVELTR tmp = new M_LEVELTR();

        tmp.setBinValue(getBinValue());

        return tmp;
    }

    @Override
    public Component getComponent(String com) {

        JPanel panel = new JPanel(new GridBagLayout());

        // Configure GridBag constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 0.5;
        gbc.weighty = 0.5;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;  // Make components fill horizontal space
        gbc.insets = new Insets(5, 5, 5, 5);      // Add padding around components;


        JComboBox jComboBox = (JComboBox) ((JPanel) super.getComponent(com)).getComponent(1);

        Component NID_C_comp = nid_ntc.getComponent(com);

        JComboBox jComboBox1 = (JComboBox) (((JPanel) NID_C_comp).getComponent(1));

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox.getSelectedIndex() == 1)
                    jComboBox1.setVisible(true);
                else
                    jComboBox1.setVisible(false);
            }
        });

        if (jComboBox.getSelectedIndex() == 1)
            jComboBox1.setVisible(true);
        else
            jComboBox1.setVisible(false);

        panel.add(jComboBox);

        panel.add(jComboBox1);


        return addLabel(panel, "M_LEVELTR","",new JLabel());

    }

    @Override
    public Variables initValueSet(String[] s) {


        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() == 1)
            nid_ntc.initValueSet(s);

        return this;
    }

    @Override
    public String getFullData() {

        String tmp = getBinValue();

        if (getDecValue() == 1)
            tmp += nid_ntc.getFullData();

        return tmp;
    }

    @Override
    public String getSimpleView() {
        String tmp = super.getSimpleView();


        if (getDecValue() == 1)
            tmp += nid_ntc.getSimpleView();

        return tmp;

    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add(0, "Level 0");
        s.add(1, "Level NTC");
        s.add(2, "Level 1");
        s.add(3, "Level 2");
        s.add(4, "Level 3");
        s.add(5, "NOT_USED");
        s.add(6, "NOT_USED");
        s.add(7, "NOT_USED");


        return s;

    }


}