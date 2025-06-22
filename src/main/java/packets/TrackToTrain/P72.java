package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Var.D.D_TEXTDISPLAY;
import packets.Var.L.L_PACKET;
import packets.Var.L.L_TEXTDISPLAY;
import packets.Var.M.M_LEVELTEXTDISPLAY;
import packets.Var.M.M_MODETEXTDISPLAY;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.*;
import packets.Var.T.T_TEXTDISPLAY;
import packets.Var.Variables;
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

import static tools.ui.GUIHelper.loadAndScaleIcon;

public class P72 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P72.class);

    ArrayList<Variables> P3Variables;

    public P72() {
        this(new String[]{
                ArithmeticalFunctions.hex2Bin("4801EC808AE004560F0047E44440C2483951A1A5CC81A5CC818481D195CDD0")
        });
    }

    public P72(String[] d) {
        

        P3Variables = new ArrayList<>();

        // Initialize all variables
        P3Variables.add((NID_PACKET) new NID_PACKET().initValueSet(d));
        

        P3Variables.add((Q_DIR) new Q_DIR().initValueSet(d));
        

        P3Variables.add((L_PACKET) new L_PACKET().initValueSet(d));
        

        P3Variables.add((Q_SCALE) new Q_SCALE().initValueSet(d));
        

        P3Variables.add((Q_TEXTCLASS) new Q_TEXTCLASS().initValueSet(d));
        

        P3Variables.add((Q_TEXTDISPLAY) new Q_TEXTDISPLAY()
                .setName("Q_TEXTDISPLAY začátek").initValueSet(d));
        

        P3Variables.add((D_TEXTDISPLAY) new D_TEXTDISPLAY()
                .setName("D_TEXTDISPLAY začátek").initValueSet(d));
        

        P3Variables.add((M_MODETEXTDISPLAY) new M_MODETEXTDISPLAY()
                .setName("M_MODETEXTDISPLAY začátek").initValueSet(d));
        

        P3Variables.add((M_LEVELTEXTDISPLAY) new M_LEVELTEXTDISPLAY()
                .setName("M_LEVELTEXTDISPLAY začátek").initValueSet(d));
        

        P3Variables.add((L_TEXTDISPLAY) new L_TEXTDISPLAY()
                .setName("L_TEXTDISPLAY konec").initValueSet(d));
        

        P3Variables.add((T_TEXTDISPLAY) new T_TEXTDISPLAY()
                .setName("T_TEXTDISPLAY konec").initValueSet(d));
        

        P3Variables.add((M_MODETEXTDISPLAY) new M_MODETEXTDISPLAY()
                .setName("M_MODETEXTDISPLAY konec").initValueSet(d));
        

        P3Variables.add((M_LEVELTEXTDISPLAY) new M_LEVELTEXTDISPLAY()
                .setName("M_LEVELTEXTDISPLAY konec").initValueSet(d));
        

        P3Variables.add((Q_TEXTCONFIRM) new Q_TEXTCONFIRM().initValueSet(d));
        

        P3Variables.add((X_TEXT) new X_TEXT().initValueSet(d));


        setIcon(loadAndScaleIcon("flags/pac/header.png"));
    }

    @Override
    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new MigLayout("wrap", "[]10[]10[]", "[]10[]10[]"));

        // Lay out each variable's component with special rules for Q_TEXTCONFIRM and X_TEXT
        for (Variables va : P3Variables) {
            if (va instanceof Q_TEXTCONFIRM) {
                jPanel.add(va.getComponent(), "newline");
            } else if (va instanceof X_TEXT) {
                jPanel.add(va.getComponent(), "newline,span,push,growx,top");
            } else {
                jPanel.add(va.getComponent(), "flowy, align left");
            }
        }

        JPanel jPanel1 = new JPanel();
        jPanel1.add(jPanel);

        // Refresh logic on combo box changes
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
        
        StringBuilder sb = new StringBuilder();

        // First pass
        for (Variables v : P3Variables) {
            sb.append(v.getFullData());
        }
        int firstPassLength = sb.length();
        

        // Update L_PACKET => index 2
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
                .cursive(" Textová zpráva")
                .underline("[2.Y]")
                .endTag()
                .getString();
    }

    public ArrayList<Variables> getP3Variables() {
        
        return P3Variables;
    }
}
