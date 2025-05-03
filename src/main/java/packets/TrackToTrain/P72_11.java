package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Var.*;
import packets.Var.D.D_TEXTDISPLAY;
import packets.Var.L.L_PACKET;
import packets.Var.L.L_TEXTDISPLAY;
import packets.Var.M.M_LEVELTEXTDISPLAY;
import packets.Var.M.M_MODETEXTDISPLAY;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.*;
import packets.Var.T.T_TEXTDISPLAY;
import packets.Var.X.X_TEXT;
import tools.crypto.ArithmeticalFunctions;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class P72_11 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P72_11.class);

    private final ArrayList<Variables> P3Variables;

    public P72_11() {
        this(new String[]{
                ArithmeticalFunctions.hex2Bin("4801A8808AE004560F0047C0E5468697320697320612074657374")
        });
    }

    public P72_11(String[] data) {
        
        P3Variables = initializeVariables(data);
        setIcon(GUIHelper.getImageIconFromResources("icons8-txt-80"));
        
    }

    private ArrayList<Variables> initializeVariables(String[] data) {
        
        ArrayList<Variables> variables = new ArrayList<>();

        variables.add(new NID_PACKET().initValueSet(data));
        

        variables.add(new Q_DIR().initValueSet(data));
        

        variables.add(new L_PACKET().initValueSet(data));
        

        variables.add(new Q_SCALE().initValueSet(data));
        

        variables.add(new Q_TEXTCLASS().initValueSet(data));
        

        variables.add(new Q_TEXTDISPLAY()
                .setName("Q_TEXTDISPLAY začátek")
                .initValueSet(data));
        

        variables.add(new D_TEXTDISPLAY()
                .setName("D_TEXTDISPLAY začátek")
                .initValueSet(data));
        

        variables.add(new M_MODETEXTDISPLAY()
                .setName("M_MODETEXTDISPLAY začátek")
                .initValueSet(data));
        

        variables.add(new M_LEVELTEXTDISPLAY()
                .setName("M_LEVELTEXTDISPLAY začátek")
                .initValueSet(data));
        

        variables.add(new L_TEXTDISPLAY()
                .setName("L_TEXTDISPLAY konec")
                .initValueSet(data));
        

        variables.add(new T_TEXTDISPLAY()
                .setName("T_TEXTDISPLAY konec")
                .initValueSet(data));
        

        variables.add(new M_MODETEXTDISPLAY()
                .setName("M_MODETEXTDISPLAY konec")
                .initValueSet(data));
        

        variables.add(new M_LEVELTEXTDISPLAY()
                .setName("M_LEVELTEXTDISPLAY konec")
                .initValueSet(data));
        

        variables.add(new Q_TEXTCONFIRM_11().initValueSet(data));
        

        variables.add(new X_TEXT().initValueSet(data));
        

        return variables;
    }

    public Component getPacketComponent() {
        
        JPanel panel = new JPanel(new MigLayout("wrap", "[]10[]10[]", "[]10[]10[]"));

        for (Variables variable : P3Variables) {
            String layoutConstraints = getLayoutConstraints(variable);
            panel.add(variable.getComponent(), layoutConstraints);
            
        }

        JPanel jPanel1 = new JPanel();
        jPanel1.add(panel);

        GUIHelper.addActionListenerToAllComboBoxes(jPanel1, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                getjProgressBar().doClick();
            }
        });
        
        return new JScrollPane(jPanel1);
    }

    private String getLayoutConstraints(Variables variable) {
        if (variable instanceof Q_TEXTCONFIRM_11 || variable instanceof X_TEXT) {
            return "newline,span,push,growx,top";
        }
        return "flowy, align left";
    }

    public String getHexData() {
        
        String hexData = ArithmeticalFunctions.bin2Hex(getBinData());
        
        return hexData;
    }

    public String getBinData() {
        
        StringBuilder binaryData = new StringBuilder();

        // First pass
        for (Variables variable : P3Variables) {
            binaryData.append(variable.getFullData());
        }
        int firstPassLength = binaryData.length();
        

        // Update L_PACKET => index 2
        P3Variables.get(2).setBinValue(
                ArithmeticalFunctions.dec2XBin(String.valueOf(firstPassLength), 13)
        );

        // Second pass
        binaryData.setLength(0);  // Reset the StringBuilder
        for (Variables variable : P3Variables) {
            binaryData.append(variable.getFullData());
        }
        int secondPassLength = binaryData.length();

        LOG.debug("Hex Data: " + ArithmeticalFunctions.bin2Hex(binaryData.toString()));

        return binaryData.toString();
    }

    @Override
    public String getSimpleView() {
        
        StringBuilder simpleView = new StringBuilder();
        for (Variables variable : P3Variables) {
            simpleView.append(variable.getSimpleView());
        }
        String result = simpleView.toString();
        
        return result;
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
                .cursive(" Textová zpráva")
                .underline(" [1.Y] ")
                .endTag()
                .getString();
    }

    public ArrayList<Variables> getP3Variables() {
        
        return P3Variables;
    }
}
