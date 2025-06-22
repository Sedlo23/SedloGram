package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Var.L.L_PACKET;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.Q_DIR;
import packets.Var.Q.Q_SCALE;
import packets.Var.Q.Q_TRACKINIT;
import packets.Var.Variables;
import tools.crypto.ArithmeticalFunctions;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static tools.ui.GUIHelper.loadAndScaleIcon;

public class P68 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P68.class);

    ArrayList<Variables> P3Variables;

    public P68() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("4400C6045608AC408D051A0B0")});
    }

    public P68(String[] d) {


        setIcon(loadAndScaleIcon("flags/pac/header.png"));

        P3Variables = new ArrayList<>();

        P3Variables.add((NID_PACKET) new NID_PACKET().initValueSet(d));
        
        P3Variables.add((Q_DIR) new Q_DIR().initValueSet(d));
        
        P3Variables.add((L_PACKET) new L_PACKET().initValueSet(d));
        
        P3Variables.add((Q_SCALE) new Q_SCALE().initValueSet(d));
        
        P3Variables.add((Q_TRACKINIT) new Q_TRACKINIT().initValueSet(d));
        
    }

    @Override
    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel(new MigLayout("wrap", "[]10[]", "[]10[]"));

        // Add variable components. Special layout for Q_TRACKINIT
        for (Variables va : P3Variables) {
            if (va instanceof Q_TRACKINIT) {
                jPanel.add(va.getComponent(), "newline,growx,push,span");
            } else {
                jPanel.add(va.getComponent());
            }
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
        

        // Update L_PACKET (index=2) with bit length
        P3Variables.get(2).setBinValue(
                ArithmeticalFunctions.dec2XBin(String.valueOf(firstPassLength), 13)
        );

        // Second pass
        sb.setLength(0);
        for (Variables v : P3Variables) {
            sb.append(v.getFullData());
        }
        int secondPassLength = sb.length();

        LOG.debug("Hex Data: " + ArithmeticalFunctions.bin2Hex(sb.toString()));
        return sb.toString();
    }

    @Override
    public Component getGraphicalVisualization() {
        
        return null;
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
    public String toString() {
        
        return new HTMLTagGenerator()
                .startTag()
                .bold(getClass().getSimpleName())
                .cursive("Traťové podmínky")
                .underline("[X.Y]")
                .endTag()
                .getString();
    }
}
