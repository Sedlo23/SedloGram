package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.NID.NID_C;
import packets.Var.Variables;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static tools.ui.GUIHelper.addLabel;


public class Q_NEWCOUNTRY extends Variables {
    private NID_C nid_c;

    public Q_NEWCOUNTRY() {
        super("Q_NEWCOUNTRY",
                1,
                "Kvalifikátor označující, zda je další balízová skupina ve stejné zemi / železniční správě jako ta předchozí uvnitř balíčku nebo ne.\n" +
                        "Pro první skupinu balise v paketu, pokud Q_NEWCOUNTRY = 0, je to stejná země / železniční správa jako LRBG v rádiové zprávě, jedna balízová  skupina v rámci balízického telegramu předávající paket  nebo jedna ze smyček uvnitř zprávy smyčky předávající paket.\n");
        nid_c = new NID_C();

    }


    @Override
    public Variables deepCopy() {

        Q_NEWCOUNTRY tmp = new Q_NEWCOUNTRY();

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


        JPanel ini = ((JPanel) super.getComponent(com));

        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);


      //  panel.add(ini);

        Component NID_C_comp = nid_c.getComponent(com);
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



        return addLabel(panel, "Q_NEWCOUNTRY","",new JLabel());

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
        s.add("Stejná země");
        s.add("Nová země");


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
