package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tools.crypto.ArithmeticalFunctions;
import packets.Var.NID.NID_PACKET;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;

import javax.swing.*;
import java.awt.*;

import static tools.ui.GUIHelper.loadAndScaleIcon;

public class P255 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P255.class);

    NID_PACKET nid_packet;

    public P255() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("FF")});
    }

    public P255(String[] d) {
        
        this.nid_packet = (NID_PACKET) new NID_PACKET().initValueSet(d);

        setIcon(loadAndScaleIcon("flags/pac/header.png"));
    }

    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
        jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        jPanel.add(nid_packet.getComponent());
        JPanel jPanel1 = new JPanel();
        jPanel1.add(jPanel);
        
        return jPanel1;
    }

    public String getHexData() {
        
        String hexData = ArithmeticalFunctions.bin2Hex(getBinData());
        
        return hexData;
    }

    public String getBinData() {
        
        String tmp = getBinDataPrivately("");
        
        return tmp;
    }

    private String getBinDataPrivately(String tmp) {
        
        tmp += nid_packet.getFullData();
        return tmp;
    }

    @Override
    public String getSimpleView() {
        
        String tmp = "";
        tmp += nid_packet.getSimpleView();
        
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
                .cursive("Konec telegramu")
                .underline("[X.Y]")
                .endTag()
                .getString();
    }
}
