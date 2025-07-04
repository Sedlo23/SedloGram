package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Var.L.L_PACKET;
import packets.Var.L.L_SECTION;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.Q_DIR;
import packets.Var.Q.Q_SCALE;
import packets.Var.Variables;
import tools.crypto.ArithmeticalFunctions;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static tools.ui.GUIHelper.loadAndScaleIcon;

public class P16 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P16.class);

    ArrayList<Variables> P3Variables;

    public P16() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("1080510D5C")});
    }

    public P16(String[] d) {
        

        P3Variables = new ArrayList<>();

        P3Variables.add((NID_PACKET) new NID_PACKET().initValueSet(d));
        P3Variables.add((Q_DIR) new Q_DIR().initValueSet(d));
        P3Variables.add((L_PACKET) new L_PACKET().initValueSet(d));
        P3Variables.add((Q_SCALE) new Q_SCALE().initValueSet(d));
        P3Variables.add((L_SECTION) new L_SECTION().initValueSet(d));

        setIcon(loadAndScaleIcon("flags/pac/icons8-skip-50.png"));
    }

    @Override
    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        for (Variables va : P3Variables) {
            gbc.weightx++;
            jPanel.add(va.getComponent(), gbc);
        }

        JPanel jPanel1 = new JPanel();
        jPanel1.add(jPanel);
        
        return new JScrollPane(jPanel1);
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
    public String getHexData() {
        String hexData = ArithmeticalFunctions.bin2Hex(getBinData());
        
        return hexData;
    }

    @Override
    public String getBinData() {
        
        String tmp = "";
        // First pass
        for (Variables v : P3Variables) {
            tmp += v.getFullData();
        }
        int firstPassLength = tmp.length();
        

        // Update L_PACKET
        P3Variables.get(2).setBinValue(
                ArithmeticalFunctions.dec2XBin(String.valueOf(firstPassLength), 13));

        // Second pass
        tmp = "";
        for (Variables v : P3Variables) {
            tmp += v.getFullData();
        }
        LOG.debug("Hex Data: " + ArithmeticalFunctions.bin2Hex(tmp.toString()));


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
                .cursive("Repoziční informace")
                .underline("[X.Y]")
                .endTag()
                .getString();
    }
}
