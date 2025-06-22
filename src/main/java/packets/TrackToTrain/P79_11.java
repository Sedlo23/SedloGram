package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Var.D.D_POSOFF;
import packets.Var.L.L_PACKET;
import packets.Var.M.M_POSTION11;
import packets.Var.N.N_ITER;
import packets.Var.NID.NID_BG;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.Q_DIR;
import packets.Var.Q.Q_MPOSITION;
import packets.Var.Q.Q_NEWCOUNTRY;
import packets.Var.Q.Q_SCALE;
import tools.crypto.ArithmeticalFunctions;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static tools.ui.GUIHelper.loadAndScaleIcon;

public class P79_11 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P79_11.class);

    NID_PACKET nid_packet;
    Q_DIR q_dir;
    L_PACKET l_packet;
    Q_SCALE q_scale;
    Q_NEWCOUNTRY q_newcountry;
    NID_BG nid_bg;
    D_POSOFF d_posoff;
    Q_MPOSITION q_mposition;
    M_POSTION11 m_postion;
    N_ITER n_iter;

    public P79_11() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("4F011C40F9C3D387FD09042115C22B86C81C")});
    }

    public P79_11(String[] d) {
        

        // Initialize variables from data
        this.nid_packet = (NID_PACKET) new NID_PACKET().initValueSet(d);
        

        this.q_dir = (Q_DIR) new Q_DIR().initValueSet(d);
        

        this.l_packet = (L_PACKET) new L_PACKET().initValueSet(d);
        

        this.q_scale = (Q_SCALE) new Q_SCALE().initValueSet(d);
        

        this.q_newcountry = (Q_NEWCOUNTRY) new Q_NEWCOUNTRY().initValueSet(d);
        

        this.nid_bg = (NID_BG) new NID_BG().initValueSet(d);
        

        this.d_posoff = (D_POSOFF) new D_POSOFF().initValueSet(d);
        

        this.q_mposition = (Q_MPOSITION) new Q_MPOSITION().initValueSet(d);
        

        this.m_postion = (M_POSTION11) new M_POSTION11().initValueSet(d);
        

        // N_ITER for kilometer jumps
        this.n_iter = new N_ITER("Kilometrický skok")
                .addNewIterVar(new Q_NEWCOUNTRY())
                .addNewIterVar(new NID_BG())
                .addNewIterVar(new D_POSOFF())
                .addNewIterVar(new Q_MPOSITION())
                .addNewIterVar(new M_POSTION11());
        this.n_iter = (N_ITER) this.n_iter.initValueSet(d);


        setIcon(loadAndScaleIcon("flags/pac/header.png"));
    }

    @Override
    public String getSimpleView() {
        
        StringBuilder sb = new StringBuilder();
        sb.append(nid_packet.getSimpleView());
        sb.append(q_dir.getSimpleView());
        sb.append(l_packet.getSimpleView());
        sb.append(q_scale.getSimpleView());
        sb.append(q_newcountry.getSimpleView());
        sb.append(nid_bg.getSimpleView());
        sb.append(d_posoff.getSimpleView());
        sb.append(q_mposition.getSimpleView());
        sb.append(m_postion.getSimpleView());
        sb.append(n_iter.getSimpleView());

        String result = sb.toString();
        
        return result;
    }

    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel(new MigLayout("wrap", "[]10[]10[]", "[]10[]10[]"));
        jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Populate panel with each variable's component
        jPanel.add(nid_packet.getComponent());
        jPanel.add(q_dir.getComponent());
        jPanel.add(l_packet.getComponent());

        jPanel.add(q_scale.getComponent());
        jPanel.add(q_newcountry.getComponent(), "span,push,newline");
        jPanel.add(nid_bg.getComponent());
        jPanel.add(d_posoff.getComponent());
        jPanel.add(q_mposition.getComponent());
        jPanel.add(m_postion.getComponent());
        jPanel.add(n_iter.getComponent(), "span,push,newline");

        // Listen for user changes in combo boxes
        GUIHelper.addActionListenerToAllComboBoxes(jPanel, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                getjProgressBar().doClick();
            }
        });

        
        
        JPanel jPanel1 = new JPanel();
        jPanel1.add(jPanel);

        return new JScrollPane(jPanel1);
    }

    public String getHexData() {
        
        String hexData = ArithmeticalFunctions.bin2Hex(getBinData());
        
        return hexData;
    }

    public String getBinData() {
        

        // First pass
        String tmp = getBinDataPrivately("");
        int firstPassLength = tmp.length();
        

        // Update L_PACKET
        l_packet.setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(firstPassLength), 13));

        // Second pass
        tmp = getBinDataPrivately("");
        LOG.debug("Hex Data: " + ArithmeticalFunctions.bin2Hex(tmp.toString()));

        return tmp;
    }

    private String getBinDataPrivately(String tmp) {
        
        tmp += nid_packet.getFullData();
        tmp += q_dir.getFullData();
        tmp += l_packet.getFullData();
        tmp += q_scale.getFullData();
        tmp += q_newcountry.getFullData();
        tmp += nid_bg.getFullData();
        tmp += d_posoff.getFullData();
        tmp += q_mposition.getFullData();
        tmp += m_postion.getFullData();
        tmp += n_iter.getFullData();
        return tmp;
    }

    @Override
    public Component getGraphicalVisualization() {
        
        return null;
    }

    @Override
    public String toString() {
        
        return new HTMLTagGenerator().startTag()
                .bold(getClass().getSimpleName())
                .cursive(" Geo informace")
                .underline(" [1.Y] ")
                .endTag()
                .getString();
    }
}
