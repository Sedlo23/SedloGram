package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Var.D.D_MAMODE;
import packets.Var.L.L_ACKMAMODE;
import packets.Var.L.L_MAMODE;
import packets.Var.L.L_PACKET;
import packets.Var.M.M_MAMODE;
import packets.Var.N.N_ITER;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.Q_DIR;
import packets.Var.Q.Q_MAMODE;
import packets.Var.Q.Q_SCALE;
import packets.Var.V.V_MAMODE;
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

public class P80 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P80.class);

    ArrayList<Variables> P3Variables;

    public P80() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("5040AA8000907FFF000100")});
    }

    public P80(String[] d) {
        
        P3Variables = new ArrayList<>();

        P3Variables.add((NID_PACKET) new NID_PACKET().initValueSet(d));
        

        P3Variables.add((Q_DIR) new Q_DIR().initValueSet(d));
        

        P3Variables.add((L_PACKET) new L_PACKET().initValueSet(d));
        

        P3Variables.add((Q_SCALE) new Q_SCALE().initValueSet(d));
        

        P3Variables.add((D_MAMODE) new D_MAMODE().initValueSet(d));
        

        P3Variables.add((M_MAMODE) new M_MAMODE().initValueSet(d));
        

        P3Variables.add((V_MAMODE) new V_MAMODE().initValueSet(d));
        

        P3Variables.add((L_MAMODE) new L_MAMODE().initValueSet(d));
        

        P3Variables.add((L_ACKMAMODE) new L_ACKMAMODE().initValueSet(d));
        

        P3Variables.add((Q_MAMODE) new Q_MAMODE().initValueSet(d));
        

        // Initialize N_ITER for the profile
        P3Variables.add(((N_ITER) new N_ITER("Profil"))
                .addNewIterVar(new D_MAMODE())
                .addNewIterVar(new M_MAMODE())
                .addNewIterVar(new V_MAMODE())
                .addNewIterVar(new L_MAMODE())
                .addNewIterVar(new L_ACKMAMODE())
                .addNewIterVar(new Q_MAMODE())
                .initValueSet(d));
        

        setIcon(GUIHelper.getImageIconFromResources("icons8-bursts-80"));
        
    }

    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel(new MigLayout("wrap", "[]10[]10[]", "[]10[]10[]"));

        for (Variables va : P3Variables) {
            if (va instanceof N_ITER) {
                jPanel.add(va.getComponent(), "span,push,newline ");
                
            } else {
                jPanel.add(va.getComponent(), "top,split 1");
                
            }
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

    @Override
    public String getSimpleView() {
        
        StringBuilder sb = new StringBuilder();
        for (Variables va : P3Variables) {
            sb.append(va.getSimpleView());
        }
        String simpleView = sb.toString();
        
        return simpleView;
    }

    public String getHexData() {
        
        String hexData = ArithmeticalFunctions.bin2Hex(getBinData());
        
        return hexData;
    }

    public String getBinData() {
        
        StringBuilder sb = new StringBuilder();

        // First pass: build combined binary string
        for (Variables v : P3Variables) {
            sb.append(v.getFullData());
        }
        int firstPassLength = sb.length();
        

        // Update L_PACKET (index 2) with the current length
        P3Variables.get(2).setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(firstPassLength), 13));

        // Second pass: rebuild combined binary string with updated L_PACKET
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
    public String toString() {
        
        return new HTMLTagGenerator().startTag()
                .bold(getClass().getSimpleName())
                .cursive("Profil přidružený k MA")
                .underline("[X.Y]")
                .endTag()
                .getString();
    }
}
