package packets.Var.Q;

import tools.string.StringHelper;
import packets.Var.D.D_TRACKCOND;
import packets.Var.D.D_TRACKINIT;
import packets.Var.L.L_TRACKCOND;
import packets.Var.M.M_TRACKCOND;
import packets.Var.N.N_ITER;
import packets.Var.Variables;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static tools.ui.GUIHelper.setTitle;


public class Q_TRACKINIT extends Variables {

    D_TRACKINIT d_trackinit;

    D_TRACKCOND d_trackcond;

    L_TRACKCOND l_trackcond;

    M_TRACKCOND m_trackcond;

    N_ITER n_iter;

    public Q_TRACKINIT() {
        super("Q_TRACKINIT",
                1,
                "Kvalifikátor pro obnovení počátečních stavů souvisejícího popisu stopy paketu.");

        d_trackinit = new D_TRACKINIT();

        d_trackcond = new D_TRACKCOND();

        l_trackcond = new L_TRACKCOND();

        m_trackcond = new M_TRACKCOND();

        n_iter = new N_ITER()
                .addNewIterVar(new D_TRACKCOND())
                .addNewIterVar(new L_TRACKCOND())
                .addNewIterVar(new M_TRACKCOND());

        n_iter.setWRAPINT(1);

    }


    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Žádné počáteční stavy");
        s.add("Prázdný profil");


        return s;

    }


    @Override
    public Variables initValueSet(String[] s) {


        setBinValue(StringHelper.TrimAR(s, getMaxSize()));

        if (getDecValue() == 0) {
            d_trackcond.initValueSet(s);
            l_trackcond.initValueSet(s);
            m_trackcond.initValueSet(s);
            n_iter.initValueSet(s);
        } else {
            d_trackinit.initValueSet(s);
        }

        return this;
    }

    @Override
    public String getFullData() {

        String tmp = getBinValue();

        if (getDecValue() == 0) {
            tmp += d_trackcond.getFullData();
            tmp += l_trackcond.getFullData();
            tmp += m_trackcond.getFullData();
            tmp += n_iter.getFullData();
        } else {
            tmp += d_trackinit.getFullData();
        }

        return tmp;
    }

    @Override
    public Component getComponent(String com) {

        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel ini = ((JPanel) super.getComponent(com));

        JComboBox jComboBox = (JComboBox) (ini).getComponent(1);

      //  panel.add(ini);


        jComboBox.setPreferredSize(new Dimension(600, 24));
        jComboBox.setMaximumSize(new Dimension(600, 24));
        jComboBox.setMinimumSize(new Dimension(600, 24));

        Component component1 = d_trackinit.getComponent(com);
        Component component2 = d_trackcond.getComponent(com);
        Component component3 = l_trackcond.getComponent(com);
        Component component4 = m_trackcond.getComponent(com);
        Component component5 = n_iter.getComponent();


        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jComboBox.getSelectedIndex() == 1) {
                    component1.setVisible(true);

                    component2.setVisible(false);
                    component3.setVisible(false);
                    component4.setVisible(false);
                    component5.setVisible(false);


                } else {
                    component1.setVisible(false);

                    component2.setVisible(true);
                    component3.setVisible(true);
                    component4.setVisible(true);
                    component5.setVisible(true);
                }

            }
        });

        if (jComboBox.getSelectedIndex() == 1) {
            component1.setVisible(true);

            component2.setVisible(false);
            component3.setVisible(false);
            component4.setVisible(false);
            component5.setVisible(false);


        } else {
            component1.setVisible(false);

            component2.setVisible(true);
            component3.setVisible(true);
            component4.setVisible(true);
            component5.setVisible(true);
        }

        panel.add(jComboBox);

        panel.add(component1);
        panel.add(component2);
        panel.add(component3);
        panel.add(component4);
        panel.add(component5);


        return setTitle(panel, "Q_TRACKINIT");

    }

    @Override
    public String getSimpleView() {

        String tmp = super.getSimpleView();

        if (getDecValue() == 0) {
            tmp += d_trackcond.getSimpleView();
            tmp += l_trackcond.getSimpleView();
            tmp += m_trackcond.getSimpleView();
            tmp += n_iter.getSimpleView();
        } else {
            tmp += d_trackinit.getSimpleView();
        }

        return tmp;

    }
}