package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tools.crypto.ArithmeticalFunctions;
import packets.Var.L.L_PACKET;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.Q_DIR;
import packets.Var.Q.Q_SRSTOP;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;

import javax.swing.*;
import java.awt.*;

import static tools.ui.GUIHelper.loadAndScaleIcon;

public class P137 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P137.class);

    NID_PACKET nid_packet;
    Q_DIR q_dir;
    L_PACKET l_packet;
    Q_SRSTOP q_srstop;

    public P137() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("87002E")});
    }

    public P137(String[] d) {
        
        this.nid_packet = (NID_PACKET) new NID_PACKET().initValueSet(d);
        
        this.q_dir = (Q_DIR) new Q_DIR().initValueSet(d);
        
        this.l_packet = (L_PACKET) new L_PACKET().initValueSet(d);
        
        this.q_srstop = (Q_SRSTOP) new Q_SRSTOP().initValueSet(d);

        setIcon(loadAndScaleIcon("flags/pac/header.png"));
    }

    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
        jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        jPanel.add(nid_packet.getComponent());
        jPanel.add(q_dir.getComponent());
        jPanel.add(l_packet.getComponent());
        jPanel.add(q_srstop.getComponent());
        JPanel jPanel1 = new JPanel();
        jPanel1.add(jPanel);
        
        return new JScrollPane(jPanel1);
    }

    public String getHexData() {
        
        String hexData = ArithmeticalFunctions.bin2Hex(getBinData());
        
        return hexData;
    }

    public String getBinData() {
        
        String tmp = getBinDataPrivately("");
        int firstPassLength = tmp.length();
        
        l_packet.setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(firstPassLength), 13));
        tmp = "";
        tmp = getBinDataPrivately(tmp);
        LOG.debug("Hex Data: " + ArithmeticalFunctions.bin2Hex(tmp.toString()));

        return tmp;
    }

    private String getBinDataPrivately(String tmp) {
        
        tmp += nid_packet.getFullData();
        tmp += q_dir.getFullData();
        tmp += l_packet.getFullData();
        tmp += q_srstop.getFullData();
        return tmp;
    }

    @Override
    public String getSimpleView() {
        
        String tmp = "";
        tmp += nid_packet.getSimpleView();
        tmp += q_dir.getSimpleView();
        tmp += l_packet.getSimpleView();
        tmp += q_srstop.getSimpleView();
        
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
                .cursive("Zastav SR")
                .underline("[X.Y]")
                .endTag()
                .getString();
    }
}
