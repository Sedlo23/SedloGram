package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tools.crypto.ArithmeticalFunctions;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.Q_DIR;
import packets.Var.Q.Q_NVGUIPERM;
import packets.Var.Q.Q_NVSBFBPERM;
import packets.Var.Q.Q_NVINHSMICPERM;
import packets.Var.A.A_NVMAXREDADH1;
import packets.Var.A.A_NVMAXREDADH2;
import packets.Var.A.A_NVMAXREDADH3;
import packets.Var.L.L_PACKET;
import packets.Var.M.M_NVAVADH;
import packets.Var.M.M_NVEBCL;
import packets.Var.Q.Q_NVKINT;
import packets.Var.Variables;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class P203 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P203.class);

    ArrayList<Variables> P3Variables;

    public P203() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("CB010201042095044000C200068846428")});
    }

    public P203(String[] d) {
        
        P3Variables = new ArrayList<>();

        P3Variables.add((NID_PACKET) new NID_PACKET().initValueSet(d));
        

        P3Variables.add((Q_DIR) new Q_DIR().initValueSet(d));
        

        P3Variables.add((L_PACKET) new L_PACKET().initValueSet(d));
        

        P3Variables.add((Q_NVGUIPERM) new Q_NVGUIPERM().initValueSet(d));
        

        P3Variables.add((Q_NVSBFBPERM) new Q_NVSBFBPERM().initValueSet(d));
        

        P3Variables.add((Q_NVINHSMICPERM) new Q_NVINHSMICPERM().initValueSet(d));
        

        P3Variables.add((A_NVMAXREDADH1) new A_NVMAXREDADH1().initValueSet(d));
        

        P3Variables.add((A_NVMAXREDADH2) new A_NVMAXREDADH2().initValueSet(d));
        

        P3Variables.add((A_NVMAXREDADH3) new A_NVMAXREDADH3().initValueSet(d));
        

        P3Variables.add((M_NVAVADH) new M_NVAVADH().initValueSet(d));
        

        P3Variables.add((M_NVEBCL) new M_NVEBCL().initValueSet(d));
        

        P3Variables.add((Q_NVKINT) new Q_NVKINT().initValueSet(d));
        

        setIcon(GUIHelper.getImageIconFromResources("icons8-registry-editor-80"));
        
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

    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel(new MigLayout("wrap 3"));
        for (Variables va : P3Variables) {
            if (va instanceof Q_NVKINT) {
                jPanel.add(va.getComponent(), "span,push,newline");
                
            } else {
                jPanel.add(va.getComponent(), "top,split 1");
                
            }
        }
        JPanel jPanel1 = new JPanel();
        jPanel1.add(jPanel);
        

        GUIHelper.addActionListenerToAllComboBoxes(jPanel, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                getjProgressBar().doClick();
              
            }
        });

        return  new JScrollPane(jPanel1);
    }

    public String getHexData() {
        
        String hexData = ArithmeticalFunctions.bin2Hex(getBinData());
        
        return hexData;
    }

    public String getBinData() {
        
        StringBuilder sb = new StringBuilder();
        // First pass: Concatenate full data for each variable.
        for (Variables v : P3Variables) {
            sb.append(v.getFullData());
        }
        int firstPassLength = sb.length();
        

        // Update L_PACKET (index 2) with the current length.
        P3Variables.get(2).setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(firstPassLength), 13));

        // Second pass: Reconstruct the binary data after updating L_PACKET.
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
                .cursive("Brzdné křivky")
                .underline(" [1.Y] ")
                .endTag()
                .getString();
    }
}
