package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Var.L.L_PACKET;
import packets.Var.NID.NID_C;
import packets.Var.NID.NID_PACKET;
import packets.Var.NID.NID_RADIO;
import packets.Var.NID.NID_RBC;
import packets.Var.Q.Q_DIR;
import packets.Var.Q.Q_RBC;
import packets.Var.Q.Q_SLEEPSESSION;
import tools.crypto.ArithmeticalFunctions;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class P42 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P42.class);

    NID_PACKET nid_packet;
    Q_DIR q_dir;
    L_PACKET l_packet;
    Q_RBC q_rbc;
    NID_C nid_c;
    NID_RBC nid_rbc;
    NID_RADIO nid_radio;
    Q_SLEEPSESSION q_sleepsession;

    public P42() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("2A80E270001E0041512800502FFF8")});
    }

    public P42(String[] d) {
        

        // Initialize each variable using incoming data
        this.nid_packet = (NID_PACKET) new NID_PACKET().initValueSet(d);
        

        this.q_dir = (Q_DIR) new Q_DIR().initValueSet(d);
        

        this.l_packet = (L_PACKET) new L_PACKET().initValueSet(d);
        

        this.q_rbc = (Q_RBC) new Q_RBC().initValueSet(d);
        

        this.nid_c = (NID_C) new NID_C().initValueSet(d);
        

        this.nid_rbc = (NID_RBC) new NID_RBC().initValueSet(d);
        

        this.nid_radio = (NID_RADIO) new NID_RADIO().initValueSet(d);
        

        this.q_sleepsession = (Q_SLEEPSESSION) new Q_SLEEPSESSION().initValueSet(d);
        

        setIcon(GUIHelper.getImageIconFromResources("icons8-radio-station-80"));
        
    }

    @Override
    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel(new MigLayout("wrap 4", "[grow]10[grow]10[grow]10[grow]", "[]10[]10[]10[]"));
        jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add components to the layout
        jPanel.add(nid_packet.getComponent());
        jPanel.add(q_dir.getComponent());
        jPanel.add(l_packet.getComponent());
        jPanel.add(q_rbc.getComponent());
        jPanel.add(nid_c.getComponent());
        jPanel.add(nid_rbc.getComponent());
        jPanel.add(nid_radio.getComponent());
        jPanel.add(q_sleepsession.getComponent());

        JPanel jPanel1 = new JPanel();
        jPanel1.add(jPanel);

        // Add ActionListener for any combo box changes
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
        

        // First pass
        String tmp = getBinDataPrivately("");
        int firstPassLength = tmp.length();
        

        // Update the length in l_packet
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
        tmp += q_rbc.getFullData();
        tmp += nid_c.getFullData();
        tmp += nid_rbc.getFullData();
        tmp += nid_radio.getFullData();
        tmp += q_sleepsession.getFullData();


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
                .cursive("Rel. ovládaní")
                .underline("[X.Y]")
                .endTag()
                .getString();
    }

    @Override
    public String getSimpleView() {
        
        StringBuilder sb = new StringBuilder();
        sb.append(nid_packet.getSimpleView());
        sb.append(q_dir.getSimpleView());
        sb.append(l_packet.getSimpleView());
        sb.append(q_rbc.getSimpleView());
        sb.append(nid_c.getSimpleView());
        sb.append(nid_rbc.getSimpleView());
        sb.append(nid_radio.getSimpleView());
        sb.append(q_sleepsession.getSimpleView());

        String simpleView = sb.toString();
        
        return simpleView;
    }
}
