package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tools.crypto.ArithmeticalFunctions;
import packets.Var.L.L_PACKET;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.Q_DIR;
import packets.Var.Q.Q_LSSMA;
import packets.Var.Variables;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class P180 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P180.class);

    ArrayList<Variables> P3Variables;

    public P180() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("B4004112")});
    }

    public P180(String[] d) {
        

        P3Variables = new ArrayList<>();

        P3Variables.add((NID_PACKET) new NID_PACKET().initValueSet(d));
        

        P3Variables.add((Q_DIR) new Q_DIR().initValueSet(d));
        

        P3Variables.add((L_PACKET) new L_PACKET().initValueSet(d));
        

        P3Variables.add((Q_LSSMA) new Q_LSSMA().initValueSet(d));
        

        setIcon(GUIHelper.getImageIconFromResources("icons8-cruise-control-on-80"));
        
    }

    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new MigLayout("wrap", "[]10[]10[]", "[]10[]10[]"));

        for (Variables va : P3Variables) {
            jPanel.add(va.getComponent());
            
        }

        JPanel jPanel1 = new JPanel();
        jPanel1.add(jPanel);
        
        return new JScrollPane(jPanel1);
    }

    public String getHexData() {
        
        String hexData = ArithmeticalFunctions.bin2Hex(getBinData());
        
        return hexData;
    }

    public String getBinData() {
        
        StringBuilder sb = new StringBuilder();

        // First pass: Build the combined binary string.
        for (Variables v : P3Variables) {
            sb.append(v.getFullData());
        }
        int firstPassLength = sb.length();
        

        // Update L_PACKET (index 2) with the new bit length.
        P3Variables.get(2).setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(firstPassLength), 13));

        // Second pass: Rebuild the binary string with updated L_PACKET.
        sb.setLength(0);
        for (Variables v : P3Variables) {
            sb.append(v.getFullData());
        }
        int secondPassLength = sb.length();

        LOG.debug("Hex Data: " + ArithmeticalFunctions.bin2Hex(sb.toString()));

        return sb.toString();
    }

    @Override
    public String getSimpleView() {
        
        StringBuilder sb = new StringBuilder();
        for (Variables va : P3Variables) {
            sb.append(va.getSimpleView());
        }
        String simpleView = sb.toString();
        
        return simpleView;
    }

    @Override
    public Component getGraphicalVisualization() {
        return null;
    }

    @Override
    public String toString() {
        
        return new HTMLTagGenerator().startTag()
                .bold(getClass().getSimpleName())
                .cursive("LSSMA příkaz")
                .underline("[X.Y]")
                .endTag()
                .getString();
    }
}
