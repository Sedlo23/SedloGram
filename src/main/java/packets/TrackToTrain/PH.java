package packets.TrackToTrain;

import tools.crypto.ArithmeticalFunctions;
import packets.Var.M.M_DUP;
import packets.Var.M.M_MCOUNT;
import packets.Var.M.M_VERSION;
import packets.Var.N.N_PIG;
import packets.Var.N.N_TOTAL;
import packets.Var.NID.NID_BG;
import packets.Var.NID.NID_C;
import packets.Var.Q.Q_LINK;
import packets.Var.Q.Q_MEDIA;
import packets.Var.Q.Q_UPDOWN;
import tools.ui.GUIHelper;
import net.miginfocom.swing.MigLayout;
import tools.string.HTMLTagGenerator;
import tools.string.StringHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PH extends Packet {

    Q_UPDOWN        q_updown;
    M_VERSION        m_version;
    Q_MEDIA       q_media;
    N_PIG       n_pig;
    N_TOTAL       n_total;
    M_DUP       m_dup;
    M_MCOUNT       m_mcount;
    NID_C       nid_c;
    NID_BG       nid_bg;
    Q_LINK       q_link;


    public JLabel getjLabel() {
        return jLabel;
    }

    public void setjLabel(JLabel jLabel) {
        this.jLabel = jLabel;
    }

    JLabel jLabel;

    public JLabel getjLabel1() {
        return jLabel1;
    }

    public void setjLabel1(JLabel jLabel1) {
        this.jLabel1 = jLabel1;
    }

    JLabel jLabel1;


    public PH() {

        this(new String[]{ArithmeticalFunctions.hex2Bin("A0007F02A0094")});


    }

    public PH(String[] d) {

        q_updown = (Q_UPDOWN) new Q_UPDOWN().initValueSet(d);
        m_version = (M_VERSION) new M_VERSION().initValueSet(d);
        q_media = (Q_MEDIA) new Q_MEDIA().initValueSet(d);
        n_pig = (N_PIG) new N_PIG().initValueSet(d);

        n_total = (N_TOTAL) new N_TOTAL().initValueSet(d);
        m_dup = (M_DUP) new M_DUP().initValueSet(d);
        m_mcount = (M_MCOUNT) new M_MCOUNT().initValueSet(d);
        nid_c = (NID_C) new NID_C().initValueSet(d);
        nid_bg = (NID_BG) new NID_BG().initValueSet(d);
        q_link = (Q_LINK) new Q_LINK().initValueSet(d);
        setIcon(GUIHelper.getImageIconFromResources("icons8-header-80"));

    }


    public Component getPacketComponent() {
        GridBagConstraints gbc = new GridBagConstraints();


        JPanel jPanel1 = new JPanel();

        jPanel1.setLayout(new MigLayout("wrap", "[]10[]", "[]10[]"));


        jPanel1.add(    q_updown    .getComponent()     );
        jPanel1.add(    m_version   .getComponent()     );
        jPanel1.add(    q_media     .getComponent()     );
        jPanel1.add(    n_pig       .getComponent()     );
        jPanel1.add(    n_total     .getComponent()     );
        jPanel1.add(    m_dup       .getComponent()     );
        jPanel1.add(    m_mcount    .getComponent()     );
        jPanel1.add(    nid_c       .getComponent()     );
        jPanel1.add(    nid_bg      .getComponent()     );
        jPanel1.add(    q_link      .getComponent()     );
        JPanel jPanel12 = new JPanel();
        jPanel12.add(jPanel1);
        GUIHelper.addActionListenerToAllComboBoxes(jPanel1, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String namea =
                        StringHelper.padLeft(String.valueOf(getNid_c().getDecValue()), 3, '0') + "_" +
                                StringHelper.padLeft(String.valueOf(getNid_bg().getDecValue()), 5, '0') + "_" +
                                StringHelper.padLeft(String.valueOf(getN_pig().getDecValue()), 1, '0') + "_" +
                                StringHelper.padLeft(String.valueOf(getM_mcount().getDecValue()), 3, '0');

                jLabel.setText(namea);
                jLabel1.setText(namea);
            }
        });


        return jPanel12;


    }

    @Override
    public String getHexData() {
        return ArithmeticalFunctions.bin2Hex(getBinData());
    }

    @Override
    public String getBinData() {
        String tmp = "";

        tmp += this.q_updown.getFullData();
        tmp += this.m_version.getFullData();
        tmp += this.q_media.getFullData();
        tmp += this.n_pig.getFullData();
        tmp += this.n_total.getFullData();
        tmp += this.m_dup.getFullData();
        tmp += this.m_mcount.getFullData();
        tmp += this.nid_c.getFullData();
        tmp += this.nid_bg.getFullData();
        tmp += this.q_link.getFullData();

        return tmp;

    }

    @Override
    public Component getGraphicalVisualization() {
        return null;
    }

    @Override
    public String getSimpleView() {

        String tmp = "";

        tmp += this.q_updown.getSimpleView();
        tmp += this.m_version.getSimpleView();
        tmp += this.q_media.getSimpleView();
        tmp += this.n_pig.getSimpleView();
        tmp += this.n_total.getSimpleView();
        tmp += this.m_dup.getSimpleView();
        tmp += this.m_mcount.getSimpleView();
        tmp += this.nid_c.getSimpleView();
        tmp += this.nid_bg.getSimpleView();
        tmp += this.q_link.getSimpleView();


        return tmp;

    }

    public Q_UPDOWN getQ_updown() {
        return q_updown;
    }

    public M_VERSION getM_version() {
        return m_version;
    }

    public Q_MEDIA getQ_media() {
        return q_media;
    }

    public N_PIG getN_pig() {
        return n_pig;
    }

    public N_TOTAL getN_total() {
        return n_total;
    }

    public M_DUP getM_dup() {
        return m_dup;
    }

    public M_MCOUNT getM_mcount() {
        return m_mcount;
    }

    public NID_C getNid_c() {
        return nid_c;
    }

    public NID_BG getNid_bg() {
        return nid_bg;
    }

    public Q_LINK getQ_link() {
        return q_link;
    }



    @Override
    public String toString() {


        return new HTMLTagGenerator()

                .startTag()
                .normal("PH:")
                .normal("")
                .normal(StringHelper.padLeft(String.valueOf((nid_c.getDecValue())),3, '0')).normal("_")
                .normal(StringHelper.padLeft(String.valueOf((nid_bg.getDecValue())),5, '0')).normal("_")
                .normal(StringHelper.padLeft(String.valueOf((n_pig.getDecValue())),1, '0')).normal("_")
                .normal(StringHelper.padLeft(String.valueOf((m_mcount.getDecValue())),3, '0')).normal(":")
                .normal(m_version.getCombo().get(m_version.getDecValue())).normal("")
                .endTag()
                .getString();

    }


}
