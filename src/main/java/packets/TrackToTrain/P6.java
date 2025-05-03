package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Var.L.L_PACKET;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.Q_DIR;
import packets.Var.Q.Q_VBCO;
import tools.crypto.ArithmeticalFunctions;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class P6 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P6.class);

    NID_PACKET nid_packet;
    Q_DIR q_dir;
    L_PACKET l_packet;
    Q_VBCO q_vbco;

    public P6() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("0600612201F7")});
    }

    public P6(String[] d) {
        

        this.nid_packet = (NID_PACKET) new NID_PACKET().initValueSet(d);
        

        this.q_dir = (Q_DIR) new Q_DIR().initValueSet(d);
        

        this.l_packet = (L_PACKET) new L_PACKET().initValueSet(d);
        

        this.q_vbco = (Q_VBCO) new Q_VBCO().initValueSet(d);
        

        setIcon(GUIHelper.getImageIconFromResources("icons8-mark-view-as-hidden-80"));
        
    }

    @Override
    public String getSimpleView() {
        
        StringBuilder sb = new StringBuilder();
        sb.append(nid_packet.getSimpleView());
        sb.append(q_dir.getSimpleView());
        sb.append(l_packet.getSimpleView());
        sb.append(q_vbco.getSimpleView());
        String simpleView = sb.toString();
        
        return simpleView;
    }

    @Override
    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
        jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        jPanel.add(nid_packet.getComponent());
        jPanel.add(q_dir.getComponent());
        jPanel.add(l_packet.getComponent());
        jPanel.add(q_vbco.getComponent());

        JPanel jPanel1 = new JPanel();
        jPanel1.add(jPanel);
        GUIHelper.addActionListenerToAllComboBoxes(jPanel1, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                getjProgressBar().doClick();
            }
        });
        return new JScrollPane(jPanel1);
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
        tmp = getBinDataPrivately(tmp);
        int lengthBefore = tmp.length();
        

        // Update l_packet
        l_packet.setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(lengthBefore), 13));

        // Second pass
        tmp = "";
        tmp = getBinDataPrivately(tmp);

        LOG.debug("Hex Data: " + ArithmeticalFunctions.bin2Hex(tmp.toString()));

        return tmp;
    }

    private String getBinDataPrivately(String tmp) {
        
        tmp += nid_packet.getFullData();
        tmp += q_dir.getFullData();
        tmp += l_packet.getFullData();
        tmp += q_vbco.getFullData();
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
                .cursive("VBC příkaz")
                .underline("[X.Y]")
                .endTag()
                .getString();
    }
}
