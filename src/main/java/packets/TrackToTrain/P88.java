package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Var.L.L_PACKET;
import packets.Var.NID.NID_LX;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.Q_DIR;
import packets.Var.Q.Q_SCALE;
import packets.Var.Q.Q_LXSTATUS;
import packets.Var.D.D_LX;
import packets.Var.L.L_LX;
import packets.Var.Variables;
import tools.crypto.ArithmeticalFunctions;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class P88 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P88.class);

    ArrayList<Variables> P3Variables;

    public P88() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("5800AE0D045600010F0974")});
    }

    public P88(String[] d) {
        

        P3Variables = new ArrayList<>();

        P3Variables.add((NID_PACKET) new NID_PACKET().initValueSet(d));
        
        P3Variables.add((Q_DIR) new Q_DIR().initValueSet(d));
        
        P3Variables.add((L_PACKET) new L_PACKET().initValueSet(d));
        
        P3Variables.add((Q_SCALE) new Q_SCALE().initValueSet(d));
        
        P3Variables.add((NID_LX) new NID_LX().initValueSet(d));
        
        P3Variables.add((D_LX) new D_LX().initValueSet(d));
        
        P3Variables.add((L_LX) new L_LX().initValueSet(d));
        
        P3Variables.add((Q_LXSTATUS) new Q_LXSTATUS().initValueSet(d));
        

        setIcon(GUIHelper.getImageIconFromResources("icons8-bursts-80"));
        
    }

    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new MigLayout("wrap", "[]10[]10[]", "[]10[]10[]"));

        for (Variables va : P3Variables) {
            jPanel.add(va.getComponent());
            
        }

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

    public String getHexData() {
        
        String hexData = ArithmeticalFunctions.bin2Hex(getBinData());
        
        return hexData;
    }

    public String getBinData() {
        
        StringBuilder sb = new StringBuilder();

        // First pass
        for (Variables v : P3Variables) {
            sb.append(v.getFullData());
        }
        int firstPassLength = sb.length();
        

        // Update L_PACKET (index 2)
        P3Variables.get(2).setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(firstPassLength), 13));

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
        
        return new HTMLTagGenerator().startTag()
                .bold(getClass().getSimpleName())
                .cursive("Inforamce o přejezdu")
                .underline(" [2.Y]")
                .endTag()
                .getString();
    }
}
