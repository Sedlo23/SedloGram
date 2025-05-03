package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.D.D_OL;
import packets.Var.D.D_STARTOL;
import packets.Var.T.T_OL;
import packets.Var.V.V_RELEASEOL;
import packets.Var.Variables;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static tools.ui.GUIHelper.setTitle;


public class Q_OVERLAP extends Variables {

    D_STARTOL d_startol;
    T_OL t_ol;
    D_OL d_ol;
    V_RELEASEOL v_releaseol;

    public Q_OVERLAP() {
        super("Q_OVERLAP",
                1,
                "This variable is set to 1 if either an overlap exists or a release speed has to be specified");

        d_startol = (D_STARTOL) new D_STARTOL();
        t_ol = (T_OL) new T_OL();
        d_ol = (D_OL) new D_OL();
        v_releaseol = (V_RELEASEOL) new V_RELEASEOL();

    }

    @Override
    public Component getComponent(String com) {


        JPanel panel = new JPanel();


        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));


        JPanel ini = ((JPanel) super.getComponent(com));

        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

       // panel.add(ini);

        Component add = panel.add(d_startol.getComponent());
        Component add1 = panel.add(t_ol.getComponent());
        Component add2 = panel.add(d_ol.getComponent());
        Component add3 = panel.add(v_releaseol.getComponent());

        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (jComboBox.getSelectedIndex() == 1) {
                    add.setVisible(true);
                    add1.setVisible(true);
                    add2.setVisible(true);
                    add3.setVisible(true);

                } else {
                    add.setVisible(false);
                    add1.setVisible(false);
                    add2.setVisible(false);
                    add3.setVisible(false);

                }

                panel.updateUI();


            }
        });

        if (getDecValue() == 1) {
            add.setVisible(true);
            add1.setVisible(true);
            add2.setVisible(true);
            add3.setVisible(true);

        } else {
            add.setVisible(false);
            add1.setVisible(false);
            add2.setVisible(false);
            add3.setVisible(false);
        }
        panel.add(jComboBox);
        panel.add(add);
        panel.add(add1);
        panel.add(add2);
        panel.add(add3);


        return setTitle(panel, "Q_OVERLAP");
    }

    @Override
    public Variables initValueSet(String[] s) {

        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() == 0)
            return this;

        d_startol.initValueSet(s);
        t_ol.initValueSet(s);
        d_ol.initValueSet(s);
        v_releaseol.initValueSet(s);

        return this;
    }

    @Override
    public Variables deepCopy() {

        Q_OVERLAP tmp = new Q_OVERLAP();

        tmp.setBinValue(getBinValue());

        tmp.d_startol = (D_STARTOL) new D_STARTOL().initValueSet(new String[]{d_startol.getBinValue()});
        tmp.t_ol = (T_OL) new T_OL().initValueSet(new String[]{t_ol.getBinValue()});
        tmp.d_ol = (D_OL) new D_OL().initValueSet(new String[]{d_ol.getBinValue()});
        tmp.v_releaseol = (V_RELEASEOL) new V_RELEASEOL().initValueSet(new String[]{v_releaseol.getBinValue()});


        return tmp;
    }


    @Override
    public String getFullData() {

        if (getDecValue() == 0)
            return getBinValue();

        String tmp = "";

        tmp += getBinValue();
        tmp += d_startol.getFullData();
        tmp += t_ol.getFullData();
        tmp += d_ol.getFullData();
        tmp += v_releaseol.getFullData();


        return tmp;
    }

    @Override
    public String getSimpleView() {


        if (getDecValue() == 0)
            return super.getSimpleView();

        String tmp = "";

        tmp += super.getSimpleView();
        tmp += d_startol.getSimpleView();
        tmp += t_ol.getSimpleView();
        tmp += d_ol.getSimpleView();
        tmp += v_releaseol.getSimpleView();


        return tmp;
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Není");
        s.add("Je");


        return s;

    }

    public D_STARTOL getD_startol() {
        return d_startol;
    }

    public T_OL getT_ol() {
        return t_ol;
    }

    public D_OL getD_ol() {
        return d_ol;
    }

    public V_RELEASEOL getV_releaseol() {
        return v_releaseol;
    }
}


