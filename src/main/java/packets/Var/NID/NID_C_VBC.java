package packets.Var.NID;

import tools.string.StringHelper;
import packets.Var.T.T_VBC;
import packets.Var.Variables;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static tools.ui.GUIHelper.setTitle;

public class NID_C_VBC extends Variables {
    private T_VBC t_vbc;

    public NID_C_VBC() {
        super("NID_C_VBC",
                1,
                "Kód používaný k identifikaci země nebo regionu, ve kterém se nachází skupina balíků, RBC nebo RIU. Nemusí se nutně řídit administrativními nebo politickými hranicemi.");

        t_vbc = new T_VBC();

    }


    @Override
    public Variables deepCopy() {

        NID_C_VBC tmp = new NID_C_VBC();

        tmp.setBinValue(getBinValue());

        return tmp;
    }

    @Override
    public Component getComponent(String com) {

        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JComboBox jComboBox = (JComboBox) ((JPanel) super.getComponent(com)).getComponent(1);

        Component NID_C_comp = t_vbc.getComponent(com);

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


        return setTitle(panel, "Q_NEWCOUNTRY");

    }

    @Override
    public Variables initValueSet(String[] s) {


        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() == 1)
            t_vbc.initValueSet(s);

        return this;
    }

    @Override
    public String getFullData() {

        String tmp = getBinValue();

        if (getDecValue() == 1)
            tmp += t_vbc.getFullData();

        return tmp;
    }

    @Override
    public String getSimpleView() {

        String tmp = super.getSimpleView();

        if (getDecValue() == 1)
            tmp += t_vbc.getSimpleView();

        return tmp;
    }
}
