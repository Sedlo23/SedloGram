package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.D.D_ENDTIMERSTARTLOC;
import packets.Var.T.T_ENDTIMER;
import packets.Var.Variables;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static tools.ui.GUIHelper.setTitle;


public class Q_ENDTIMER extends Variables {

    T_ENDTIMER t_endtimer;
    D_ENDTIMERSTARTLOC d_endtimerstartloc;

    public Q_ENDTIMER() {
        super("Q_ENDTIMER",
                1,
                "Kvalifikátor označující, zda pro sekci End v MA existují informace o časovači koncové sekce.");

        t_endtimer = (T_ENDTIMER) new T_ENDTIMER();
        d_endtimerstartloc = (D_ENDTIMERSTARTLOC) new D_ENDTIMERSTARTLOC();

    }

    @Override
    public Component getComponent(String com) {

        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel ini = ((JPanel) super.getComponent(com));

        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

        //panel.add(ini);

        Component add = panel.add(t_endtimer.getComponent());
        Component add1 = panel.add(d_endtimerstartloc.getComponent());

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (jComboBox.getSelectedIndex() == 1) {
                    add.setVisible(true);
                    add1.setVisible(true);

                } else {
                    add.setVisible(false);
                    add1.setVisible(false);

                }

                panel.updateUI();


            }
        });

        if (getDecValue() == 1) {
            add.setVisible(true);
            add1.setVisible(true);

        } else {
            add.setVisible(false);
            add1.setVisible(false);
        }
        panel.add(jComboBox);
        panel.add(add);
        panel.add(add1);

        return setTitle(panel, "Q_ENDTIMER");

    }

    @Override
    public Variables initValueSet(String[] s) {

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() == 0)
            return this;

        t_endtimer.initValueSet(s);
        d_endtimerstartloc.initValueSet(s);

        return this;
    }

    @Override
    public Variables deepCopy() {

        Q_ENDTIMER tmp = new Q_ENDTIMER();

        tmp.setBinValue(getBinValue());

        tmp.t_endtimer = (T_ENDTIMER) new T_ENDTIMER().initValueSet(new String[]{t_endtimer.getBinValue()});

        tmp.d_endtimerstartloc = (D_ENDTIMERSTARTLOC) new D_ENDTIMERSTARTLOC().initValueSet(new String[]{d_endtimerstartloc.getBinValue()});

        return tmp;
    }

    @Override
    public String getFullData() {

        if (getDecValue() == 0)
            return getBinValue();

        String tmp = "";


        tmp += getBinValue();
        tmp += t_endtimer.getFullData();
        tmp += d_endtimerstartloc.getFullData();


        return tmp;
    }

    @Override
    public String getSimpleView() {
        if (getDecValue() == 0)
            return super.getSimpleView();

        String tmp = "";


        tmp += super.getSimpleView();
        tmp += t_endtimer.getSimpleView();
        tmp += d_endtimerstartloc.getSimpleView();


        return tmp;
    }

    public T_ENDTIMER getT_endtimer() {
        return t_endtimer;
    }

    public D_ENDTIMERSTARTLOC getD_endtimerstartloc() {
        return d_endtimerstartloc;
    }


    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Ne");
        s.add("Ano");


        return s;

    }
}
