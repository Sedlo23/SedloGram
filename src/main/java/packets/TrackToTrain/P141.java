package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tools.crypto.ArithmeticalFunctions;
import packets.Var.G.G_TSR;
import packets.Var.L.L_PACKET;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.Q_DIR;
import packets.Var.Q.Q_GDIR;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;

import javax.swing.*;
import java.awt.*;

import static tools.ui.GUIHelper.loadAndScaleIcon;

public class P141 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P141.class);

    NID_PACKET nid_packet;
    Q_DIR q_dir;
    L_PACKET l_packet;
    Q_GDIR q_gdir;
    G_TSR g_tsr;

    public P141() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("8D004132")});
    }

    public P141(String[] d) {
        
        this.nid_packet = (NID_PACKET) new NID_PACKET().initValueSet(d);
        
        this.q_dir = (Q_DIR) new Q_DIR().initValueSet(d);
        
        this.l_packet = (L_PACKET) new L_PACKET().initValueSet(d);
        
        this.q_gdir = (Q_GDIR) new Q_GDIR().initValueSet(d);
        
        this.g_tsr = (G_TSR) new G_TSR().initValueSet(d);

        setIcon(loadAndScaleIcon("flags/pac/header.png"));
    }

    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
        jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        jPanel.add(nid_packet.getComponent());
        jPanel.add(q_dir.getComponent());
        jPanel.add(l_packet.getComponent());
        jPanel.add(q_gdir.getComponent());
        jPanel.add(g_tsr.getComponent());

        JPanel jPanel1 = new JPanel();
        jPanel1.add(jPanel);
        
        return new JScrollPane(jPanel1);
    }

    @Override
    public String getSimpleView() {
        
        String tmp = "";
        tmp += nid_packet.getSimpleView();
        tmp += q_dir.getSimpleView();
        tmp += l_packet.getSimpleView();
        tmp += q_gdir.getSimpleView();
        tmp += g_tsr.getSimpleView();
        
        return tmp;
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
        tmp += q_gdir.getFullData();
        tmp += g_tsr.getFullData();
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
                .cursive("Defaultní gradient")
                .underline("[X.Y]")
                .endTag()
                .getString();
    }
}
