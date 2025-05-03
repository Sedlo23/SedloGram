package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Var.L.L_PACKET;
import packets.Var.M.M_LEVELTR;
import packets.Var.N.N_ITER;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.Q_DIR;
import tools.crypto.ArithmeticalFunctions;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class P46 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P46.class);

    NID_PACKET nid_packet;
    Q_DIR q_dir;
    L_PACKET l_packet;
    M_LEVELTR m_leveltr;
    N_ITER n_iter;

    public P46() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("2E006041C69C")});
    }

    public P46(String[] d) {
        

        setIcon(GUIHelper.getImageIconFromResources("icons8-transition-both-directions-80"));
        

        // Initialize fields
        this.nid_packet = (NID_PACKET) new NID_PACKET().initValueSet(d);
        

        this.q_dir = (Q_DIR) new Q_DIR().initValueSet(d);
        

        this.l_packet = (L_PACKET) new L_PACKET().initValueSet(d);
        

        this.m_leveltr = (M_LEVELTR) new M_LEVELTR().initValueSet(d);
        

        // N_ITER initialization
        this.n_iter = new N_ITER("Přechod");
        this.n_iter.addNewIterVar(new M_LEVELTR());
        this.n_iter = (N_ITER) this.n_iter.initValueSet(d);
        
    }

    @Override
    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel(new MigLayout("wrap", "[]10[]10[]", "[]10[]10[]"));

        jPanel.add(nid_packet.getComponent());
        jPanel.add(q_dir.getComponent());
        jPanel.add(l_packet.getComponent());
        jPanel.add(m_leveltr.getComponent(), "span,push,newline");
        jPanel.add(n_iter.getComponent(), "span,push,newline");

        JPanel jPanel1 = new JPanel();
        jPanel1.add(jPanel);

        // Refresh action on combo box changes
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
        sb.append(m_leveltr.getSimpleView());
        sb.append(n_iter.getSimpleView());

        String simpleView = sb.toString();
        
        return simpleView;
    }

    public String getHexData() {
        
        String hexData = ArithmeticalFunctions.bin2Hex(getBinData());
        
        return hexData;
    }

    public String getBinData() {
        
        String tmp = "";

        // First pass
        tmp += nid_packet.getFullData();
        tmp += q_dir.getFullData();
        tmp += l_packet.getFullData();
        tmp += m_leveltr.getFullData();
        tmp += n_iter.getFullData();

        int firstPassLength = tmp.length();
        

        // Update l_packet
        l_packet.setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(firstPassLength), 13));

        // Second pass
        tmp = "";
        tmp += nid_packet.getFullData();
        tmp += q_dir.getFullData();
        tmp += l_packet.getFullData();
        tmp += m_leveltr.getFullData();
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
                .cursive(" Tabulka priorit")
                .underline("[X.Y]")
                .endTag()
                .getString();
    }
}
