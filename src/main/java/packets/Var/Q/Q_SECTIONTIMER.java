package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.D.D_SECTIONTIMERSTOPLOC;
import packets.Var.T.T_SECTIONTIMER;
import packets.Var.Variables;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static tools.ui.GUIHelper.setTitle;

public class Q_SECTIONTIMER extends Variables {

    T_SECTIONTIMER t_sectiontimer;
    D_SECTIONTIMERSTOPLOC d_sectiontimerstoploc;

    public Q_SECTIONTIMER() {
        super("Q_SECTIONTIMER",
                1,
                "Kvalifikátor označující, zda existuje časový limit oddílu související s oddílem");

        t_sectiontimer = (T_SECTIONTIMER) new T_SECTIONTIMER();
        d_sectiontimerstoploc = (D_SECTIONTIMERSTOPLOC) new D_SECTIONTIMERSTOPLOC();
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Není");
        s.add("Je");

        return s;

    }

    @Override
    public Component getComponent(String com) {


        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));


        JPanel ini = ((JPanel) super.getComponent(com));

        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

      //  panel.add(ini);

        Component add = panel.add(t_sectiontimer.getComponent());
        Component add1 = panel.add(d_sectiontimerstoploc.getComponent());

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

        return setTitle(panel, "Q_SECTIONTIMER");
    }

    @Override
    public Variables initValueSet(String[] s) {

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() == 0)
            return this;

        t_sectiontimer.initValueSet(s);
        d_sectiontimerstoploc.initValueSet(s);

        return this;
    }

    @Override
    public Variables deepCopy() {

        Q_SECTIONTIMER tmp = new Q_SECTIONTIMER();

        tmp.setBinValue(getBinValue());

        tmp.t_sectiontimer = (T_SECTIONTIMER) new T_SECTIONTIMER().initValueSet(new String[]{t_sectiontimer.getBinValue()});

        tmp.d_sectiontimerstoploc = (D_SECTIONTIMERSTOPLOC) new D_SECTIONTIMERSTOPLOC().initValueSet(new String[]{d_sectiontimerstoploc.getBinValue()});


        return tmp;
    }

    @Override
    public String getFullData() {

        if (getDecValue() == 0)
            return getBinValue();

        String tmp = "";



        tmp += getBinValue();
        tmp += t_sectiontimer.getFullData();
        tmp += d_sectiontimerstoploc.getFullData();


        return tmp;
    }

    @Override
    public String getSimpleView() {

        if (getDecValue() == 0)
            return super.getSimpleView();

        String tmp = "";

        tmp += super.getSimpleView();
        tmp += t_sectiontimer.getSimpleView();
        tmp += d_sectiontimerstoploc.getSimpleView();


        return tmp;

    }

    public T_SECTIONTIMER getT_sectiontimer() {
        return t_sectiontimer;
    }

    public D_SECTIONTIMERSTOPLOC getD_sectiontimerstoploc() {
        return d_sectiontimerstoploc;
    }
}
