package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Var.L.L_PACKET;
import packets.Var.NID.NID_PACKET;
import packets.Var.NID.NID_TSR;
import packets.Var.Q.Q_DIR;
import packets.Var.Variables;
import tools.crypto.ArithmeticalFunctions;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static tools.ui.GUIHelper.loadAndScaleIcon;

public class P66 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P66.class);

    ArrayList<Variables> P3Variables;

    public P66() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("42403EF8")});
    }

    public P66(String[] d) {
        

        P3Variables = new ArrayList<>();

        // Initialize variables
        P3Variables.add((NID_PACKET) new NID_PACKET().initValueSet(d));
        P3Variables.add((Q_DIR) new Q_DIR().initValueSet(d));
        P3Variables.add((L_PACKET) new L_PACKET().initValueSet(d));
        P3Variables.add((NID_TSR) new NID_TSR().initValueSet(d));

        setIcon(loadAndScaleIcon("flags/pac/header.png"));
    }

    @Override
    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel(new MigLayout("wrap", "[]10[]10[]", "[]10[]10[]"));
        for (Variables va : P3Variables) {
            jPanel.add(va.getComponent());
        }

        JPanel jPanel1 = new JPanel();
        jPanel1.add(jPanel);

        
        return new JScrollPane(jPanel1);
    }

    @Override
    public String getHexData() {
        
        String hexData = ArithmeticalFunctions.bin2Hex(getBinData());
        
        return hexData;
    }

    @Override
    public String getBinData() {
        
        StringBuilder sb = new StringBuilder();

        // First pass
        for (Variables v : P3Variables) {
            sb.append(v.getFullData());
        }
        int firstPassLength = sb.length();
        

        // Update L_PACKET (index=2)
        P3Variables.get(2).setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(firstPassLength), 13));

        // Second pass
        sb.setLength(0);
        for (Variables v : P3Variables) {
            sb.append(v.getFullData());
        }
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
        
        return new HTMLTagGenerator()
                .startTag()
                .bold(getClass().getSimpleName())
                .cursive("TSR - odvolání")
                .underline("[X.Y]")
                .endTag()
                .getString();
    }
}
