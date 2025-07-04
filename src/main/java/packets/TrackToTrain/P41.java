package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Var.D.D_LEVELTR;
import packets.Var.L.L_ACKLEVELTR;
import packets.Var.L.L_PACKET;
import packets.Var.M.M_LEVELTR;
import packets.Var.N.N_ITER;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.Q_DIR;
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

public class P41 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P41.class);

    NID_PACKET nid_packet;
    Q_DIR q_dir;
    L_PACKET l_packet;
    Q_SCALE q_scale;
    D_LEVELTR d_leveltr;
    M_LEVELTR m_leveltr;
    L_ACKLEVELTR l_ackleveltr;
    N_ITER n_iter;

    public P41() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("29807EFFFF1700C0")});
    }

    public P41(String[] d) {
        

        // Initialize variables from data
        this.nid_packet = (NID_PACKET) new NID_PACKET().initValueSet(d);
        

        this.q_dir = (Q_DIR) new Q_DIR().initValueSet(d);
        

        this.l_packet = (L_PACKET) new L_PACKET().initValueSet(d);
        

        this.q_scale = (Q_SCALE) new Q_SCALE().initValueSet(d);
        

        this.d_leveltr = (D_LEVELTR) new D_LEVELTR().initValueSet(d);
        

        this.m_leveltr = (M_LEVELTR) new M_LEVELTR().initValueSet(d);
        

        this.l_ackleveltr = (L_ACKLEVELTR) new L_ACKLEVELTR().initValueSet(d);
        

        this.n_iter = new N_ITER("Přechod");
        this.n_iter
                .addNewIterVar(new M_LEVELTR())
                .addNewIterVar(new L_ACKLEVELTR());
        this.n_iter = (N_ITER) this.n_iter.initValueSet(d);
        this.n_iter.setWRAPINT(2);

        setIcon(loadAndScaleIcon("flags/pac/icons8-change-50.png"));
    }

    @Override
    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel(new MigLayout("nogrid", "[]10[]10[]", "[]10[]10[]"));

        jPanel.add(nid_packet.getComponent());
        jPanel.add(q_dir.getComponent());
        jPanel.add(l_packet.getComponent());
        jPanel.add(q_scale.getComponent(), "newline");
        jPanel.add(d_leveltr.getComponent());
        jPanel.add(m_leveltr.getComponent());
        jPanel.add(l_ackleveltr.getComponent());
        jPanel.add(n_iter.getComponent(), "span,push,newline");

        JPanel jPanel1 = new JPanel();
        jPanel1.add(jPanel);

        // Refresh logic
        GUIHelper.addActionListenerToAllComboBoxes(jPanel1, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                getjProgressBar().doClick();
            }
        });

        
        return new JScrollPane(jPanel1);
    }

    @Override
    public String getSimpleView() {
        
        StringBuilder sb = new StringBuilder();
        sb.append(nid_packet.getSimpleView());
        sb.append(q_dir.getSimpleView());
        sb.append(l_packet.getSimpleView());
        sb.append(q_scale.getSimpleView());
        sb.append(d_leveltr.getSimpleView());
        sb.append(m_leveltr.getSimpleView());
        sb.append(l_ackleveltr.getSimpleView());
        sb.append(n_iter.getSimpleView());

        String simpleView = sb.toString();
        
        return simpleView;
    }

    @Override
    public String getHexData() {
        String hexData = ArithmeticalFunctions.bin2Hex(getBinData());
        
        return hexData;
    }

    @Override
    public String getBinData() {
        
        String tmp = "";

        // First pass
        tmp += nid_packet.getFullData();
        tmp += q_dir.getFullData();
        tmp += l_packet.getFullData();
        tmp += q_scale.getFullData();
        tmp += d_leveltr.getFullData();
        tmp += m_leveltr.getFullData();
        tmp += l_ackleveltr.getFullData();
        tmp += n_iter.getFullData();

        int firstPassLength = tmp.length();
        

        l_packet.setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(firstPassLength), 13));

        // Second pass
        tmp = "";
        tmp += nid_packet.getFullData();
        tmp += q_dir.getFullData();
        tmp += l_packet.getFullData();
        tmp += q_scale.getFullData();
        tmp += d_leveltr.getFullData();
        tmp += m_leveltr.getFullData();
        tmp += l_ackleveltr.getFullData();
        tmp += n_iter.getFullData();

        LOG.debug("Hex Data: " + ArithmeticalFunctions.bin2Hex(tmp.toString()));

        return tmp;
    }

    @Override
    public Component getGraphicalVisualization() {
        return null;
    }

    @Override
    public String toString() {
        
        return new HTMLTagGenerator()
                .startTag()
                .bold(getClass().getSimpleName())
                .cursive("Přechod")
                .underline("[X.Y]")
                .endTag()
                .getString();
    }
}
