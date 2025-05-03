package packets.Var.M;

import tools.string.StringHelper;
import packets.Var.NID.NID_NTC;
import packets.Var.Variables;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static tools.ui.GUIHelper.setTitle;


public class M_LEVELTEXTDISPLAY extends Variables {
    private NID_NTC nid_c;

    public M_LEVELTEXTDISPLAY() {
        super("Text se při zadávání / zobrazuje tak dlouho, dokud je v definované úrovni.",
                3,
                "The text is displayed when entering / as long as in the defined level");

        nid_c = new NID_NTC();

    }


    @Override
    public Variables deepCopy() {

        M_LEVELTEXTDISPLAY tmp = new M_LEVELTEXTDISPLAY();

        tmp.setBinValue(getBinValue());

        return tmp;
    }

    @Override
    public Component getComponent(String com) {

        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JComboBox jComboBox = (JComboBox) ((JPanel) super.getComponent(com)).getComponent(1);

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

      //  if (jComboBox.getSelectedIndex() == 1)
      //      NID_C_comp.setVisible(true);
      //  else
      //      NID_C_comp.setVisible(false);

        panel.add(jComboBox);

        panel.add(NID_C_comp);


        return setTitle(panel, "M_LEVELTEXTDISPLAY");

    }

    @Override
    public Variables initValueSet(String[] s) {


        setBinValue(StringHelper.TrimAR(s, getMaxSize()));


        if (getDecValue() == 1)
            nid_c.initValueSet(s);

        return this;
    }


    @Override
    public String getFullData() {

        String tmp = getBinValue();

        if (getDecValue() == 1)
            tmp += nid_c.getFullData();

        return tmp;
    }


    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(0, "Level 0");
        s.set(1, "Level NTC");
        s.set(2, "Level 1");
        s.set(3, "Level 2");
        s.set(4, "Level 3");
        s.set(5, "Není omezeno");
        s.set(6, "NOT_USED");
        s.set(7, "NOT_USED");


        return s;

    }

    @Override
    public String getSimpleView() {
        String tmp = super.getSimpleView();


        if (getDecValue() == 1)
            tmp += nid_c.getSimpleView();

        return tmp;

    }
}
