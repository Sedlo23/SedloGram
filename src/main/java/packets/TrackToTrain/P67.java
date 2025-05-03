package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Var.D.D_TRACKCOND;
import packets.Var.L.L_PACKET;
import packets.Var.L.L_TRACKCOND;
import packets.Var.N.N_ITER;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.Q_DIR;
import packets.Var.Q.Q_SCALE;
import tools.crypto.ArithmeticalFunctions;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class P67 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P67.class);

    NID_PACKET nid_packet;
    Q_DIR q_dir;
    L_PACKET l_packet;
    Q_SCALE q_scale;
    D_TRACKCOND d_trackcond;
    L_TRACKCOND l_trackcond;
    N_ITER n_iter;

    public P67() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("4340B4045708AE1115C22B8")});
    }

    public P67(String[] d) {
        

        // Initialize fields
        this.nid_packet = (NID_PACKET) new NID_PACKET().initValueSet(d);
        

        this.q_dir = (Q_DIR) new Q_DIR().initValueSet(d);
        

        this.l_packet = (L_PACKET) new L_PACKET().initValueSet(d);
        

        this.q_scale = (Q_SCALE) new Q_SCALE().initValueSet(d);
        

        this.d_trackcond = (D_TRACKCOND) new D_TRACKCOND().initValueSet(d);
        

        this.l_trackcond = (L_TRACKCOND) new L_TRACKCOND().initValueSet(d);
        

        // Initialize n_iter
        this.n_iter = new N_ITER();
        this.n_iter.addNewIterVar(new D_TRACKCOND())
                .addNewIterVar(new L_TRACKCOND());
        this.n_iter.initValueSet(d);
        this.n_iter.setWRAPINT(2);
        

        setIcon(GUIHelper.getImageIconFromResources("icons8-wind-80"));
        
    }

    @Override
    public String getSimpleView() {
        
        StringBuilder sb = new StringBuilder();
        sb.append(nid_packet.getSimpleView());
        sb.append(q_dir.getSimpleView());
        sb.append(l_packet.getSimpleView());
        sb.append(q_scale.getSimpleView());
        sb.append(d_trackcond.getSimpleView());
        sb.append(l_trackcond.getSimpleView());
        // n_iter is used in getBinData, but the original code does not append in getSimpleView()
        // – if needed, we can add or remove that per your logic.
        String simpleView = sb.toString();
        
        return simpleView;
    }

    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel(new MigLayout("wrap", "[]10[]10[]", "[]10[]10[]"));
        jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        jPanel.add(nid_packet.getComponent());
        jPanel.add(q_dir.getComponent());
        jPanel.add(l_packet.getComponent());
        jPanel.add(q_scale.getComponent());
        jPanel.add(d_trackcond.getComponent());
        jPanel.add(l_trackcond.getComponent());
        jPanel.add(n_iter.getComponent(), "span,newline,grow");

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

    public String getHexData() {
        
        String hexData = ArithmeticalFunctions.bin2Hex(getBinData());
        
        return hexData;
    }

    public String getBinData() {
        
        String tmp = "";
        tmp = getBinDataPrivately(tmp);

        // Append n_iter data
        tmp += n_iter.getFullData();

        // First pass length
        int firstPassLength = tmp.length();
        

        // Update l_packet
        l_packet.setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(firstPassLength), 13));

        // Second pass
        tmp = "";
        tmp = getBinDataPrivately(tmp);
        tmp += n_iter.getFullData();

        LOG.debug("Hex Data: " + ArithmeticalFunctions.bin2Hex(tmp.toString()));
        return tmp;
    }

    private String getBinDataPrivately(String tmp) {
        
        tmp += nid_packet.getFullData();
        tmp += q_dir.getFullData();
        tmp += l_packet.getFullData();
        tmp += q_scale.getFullData();
        tmp += d_trackcond.getFullData();
        tmp += l_trackcond.getFullData();
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
                .cursive("BMM")
                .underline("[X.Y]")
                .endTag()
                .getString();
    }
}
