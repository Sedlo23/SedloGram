package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.D.D_DP;
import packets.Var.V.V_RELEASEDP;
import packets.Var.Variables;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static tools.ui.GUIHelper.setTitle;


public class Q_DANGERPOINT extends Variables {

    D_DP d_dp;
    V_RELEASEDP v_releasedp;

    public Q_DANGERPOINT() {
        super("Q_DANGERPOINT",
                1,
                "This variable is set to 1 if either a danger point exists or a release speed has to be specified");

        d_dp = (D_DP) new D_DP();
        v_releasedp = (V_RELEASEDP) new V_RELEASEDP();
    }

    @Override
    public Component getComponent(String com) {


        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));


        JPanel ini = ((JPanel) super.getComponent(com));

        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);


      // panel.add(ini);

        Component add = panel.add(d_dp.getComponent());
        Component add1 = panel.add(v_releasedp.getComponent());

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


        return setTitle(panel, "Q_DANGERPOINT");
    }

    @Override
    public Variables initValueSet(String[] s) {

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() == 0)
            return this;

        d_dp.initValueSet(s);
        v_releasedp.initValueSet(s);

        return this;
    }

    @Override
    public Variables deepCopy() {

        Q_DANGERPOINT tmp = new Q_DANGERPOINT();

        tmp.setBinValue(getBinValue());

        tmp.d_dp = (D_DP) new D_DP().initValueSet(new String[]{d_dp.getBinValue()});

        tmp.v_releasedp = (V_RELEASEDP) new V_RELEASEDP().initValueSet(new String[]{v_releasedp.getBinValue()});


        return tmp;
    }

    @Override
    public String getFullData() {

        if (getDecValue() == 0)
            return getBinValue();

        String tmp = "";



        tmp += getBinValue();
        tmp += d_dp.getFullData();
        tmp += v_releasedp.getFullData();


        return tmp;
    }

    @Override
    public String getSimpleView() {


        if (getDecValue() == 0)
            return super.getSimpleView();

        String tmp = "";


        tmp += super.getSimpleView();
        tmp += d_dp.getSimpleView();
        tmp += v_releasedp.getSimpleView();


        return tmp;
    }

    public D_DP getD_dp() {
        return d_dp;
    }

    public V_RELEASEDP getV_releasedp() {
        return v_releasedp;
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Ne");
        s.add("Ano");


        return s;

    }
}
