package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Var.D.D_VALIDNV;
import packets.Var.D.D_NVROLL;
import packets.Var.D.D_NVOVTRP;
import packets.Var.D.D_NVPOTRP;
import packets.Var.D.D_NVSTFF;
import packets.Var.L.L_PACKET;
import packets.Var.M.M_NVCONTACT;
import packets.Var.M.M_NVDERUN;
import packets.Var.M.M_VERSION;
import packets.Var.N.N_ITER;
import packets.Var.NID.NID_C;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.Q_DIR;
import packets.Var.Q.Q_NVSRBKTRG;
import packets.Var.Q.Q_NVEMRRLS;
import packets.Var.Q.Q_NVDRIVER_ADHES;
import packets.Var.Q.Q_SCALE;
import packets.Var.T.T_NVCONTACT;
import packets.Var.T.T_NVOVTRP;
import packets.Var.V.*;
import packets.Var.Variables;
import net.miginfocom.swing.MigLayout;
import tools.crypto.ArithmeticalFunctions;
import tools.string.HTMLTagGenerator;
import tools.ui.GUIHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The P3_11 packet represents a specific type of packet containing multiple
 * variable fields. Instead of storing these fields in an array, each variable
 * is declared as a separate field. The class provides methods for obtaining
 * simple and binary views of the packet as well as a Swing UI component for display.
 */
public class P3_11 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P3_11.class);

    // Declare individual fields in the order defined by the packet specification:
    private final NID_PACKET nid_packet;
    private final Q_DIR q_dir;
    private final L_PACKET l_packet;
    private final Q_SCALE q_scale;
    private final D_VALIDNV d_validnv;
    private final N_ITER n_iter; // "Označení země", with an added NID_C iter variable
    private final V_NVSHUNT v_nvshunt;
    private final V_NVSTFF v_nvstff;
    private final V_NVONSIGHT v_nvonsight;
    private final V_NVUNFIT v_nvunfit;
    private final V_NVREL v_nvrel;
    private final D_NVROLL d_nvroll;
    private final Q_NVSRBKTRG q_nvsrbktrg;
    private final Q_NVEMRRLS q_nvemrrls;
    private final V_NVALLOWOVTRP v_nvallowovtrp;
    private final V_NVSUPOVTRP v_nvsupovtrp;
    private final D_NVOVTRP d_nvovtrp;
    private final T_NVOVTRP t_nvovtrp;
    private final D_NVPOTRP d_nvpotrp;
    private final M_NVCONTACT m_nvcontact;
    private final T_NVCONTACT t_nvcontact;
    private final M_NVDERUN m_nvderun;
    private final D_NVSTFF d_nvstff;
    private final Q_NVDRIVER_ADHES q_nvdriver_adhes;

    /**
     * Default constructor using a default hexadecimal string.
     */
    public P3_11() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("03017400000C38000000000000000000000000000000000")});
    }

    /**
     * Constructs a P3_11 packet by initializing each field using the provided data array.
     *
     * @param d the binary data array used to initialize all fields
     */
    public P3_11(String[] d) {
        

        nid_packet       = (NID_PACKET) new NID_PACKET().initValueSet(d);
        

        q_dir            = (Q_DIR) new Q_DIR().initValueSet(d);
        

        l_packet         = (L_PACKET) new L_PACKET().initValueSet(d);
        

        q_scale          = (Q_SCALE) new Q_SCALE().initValueSet(d);
        

        d_validnv        = (D_VALIDNV) new D_VALIDNV().initValueSet(d);
        

        n_iter           = (N_ITER) new N_ITER("Označení země")
                .addNewIterVar(new NID_C())
                .initValueSet(d);
        

        v_nvshunt        = (V_NVSHUNT) new V_NVSHUNT().initValueSet(d);
        

        v_nvstff         = (V_NVSTFF) new V_NVSTFF().initValueSet(d);
        

        v_nvonsight      = (V_NVONSIGHT) new V_NVONSIGHT().initValueSet(d);
        

        v_nvunfit        = (V_NVUNFIT) new V_NVUNFIT().initValueSet(d);
        

        v_nvrel          = (V_NVREL) new V_NVREL().initValueSet(d);
        

        d_nvroll         = (D_NVROLL) new D_NVROLL().initValueSet(d);
        

        q_nvsrbktrg      = (Q_NVSRBKTRG) new Q_NVSRBKTRG().initValueSet(d);
        

        q_nvemrrls       = (Q_NVEMRRLS) new Q_NVEMRRLS().initValueSet(d);
        

        v_nvallowovtrp   = (V_NVALLOWOVTRP) new V_NVALLOWOVTRP().initValueSet(d);
        

        v_nvsupovtrp     = (V_NVSUPOVTRP) new V_NVSUPOVTRP().initValueSet(d);
        

        d_nvovtrp        = (D_NVOVTRP) new D_NVOVTRP().initValueSet(d);
        

        t_nvovtrp        = (T_NVOVTRP) new T_NVOVTRP().initValueSet(d);
        

        d_nvpotrp        = (D_NVPOTRP) new D_NVPOTRP().initValueSet(d);
        

        m_nvcontact      = (M_NVCONTACT) new M_NVCONTACT().initValueSet(d);
        

        t_nvcontact      = (T_NVCONTACT) new T_NVCONTACT().initValueSet(d);
        

        m_nvderun        = (M_NVDERUN) new M_NVDERUN().initValueSet(d);
        

        d_nvstff         = (D_NVSTFF) new D_NVSTFF().initValueSet(d);
        

        q_nvdriver_adhes = (Q_NVDRIVER_ADHES) new Q_NVDRIVER_ADHES().initValueSet(d);
        

        setIcon(GUIHelper.getImageIconFromResources("icons8-registry-editor-80"));
        
    }

    /**
     * Returns a simplified view by concatenating the simple views of all fields.
     *
     * @return a string representing a simple view of the packet.
     */
    @Override
    public String getSimpleView() {
        
        StringBuilder sb = new StringBuilder();
        sb.append(nid_packet.getSimpleView());
        sb.append(q_dir.getSimpleView());
        sb.append(l_packet.getSimpleView());
        sb.append(q_scale.getSimpleView());
        sb.append(d_validnv.getSimpleView());
        sb.append(n_iter.getSimpleView());
        sb.append(v_nvshunt.getSimpleView());
        sb.append(v_nvstff.getSimpleView());
        sb.append(v_nvonsight.getSimpleView());
        sb.append(v_nvunfit.getSimpleView());
        sb.append(v_nvrel.getSimpleView());
        sb.append(d_nvroll.getSimpleView());
        sb.append(q_nvsrbktrg.getSimpleView());
        sb.append(q_nvemrrls.getSimpleView());
        sb.append(v_nvallowovtrp.getSimpleView());
        sb.append(v_nvsupovtrp.getSimpleView());
        sb.append(d_nvovtrp.getSimpleView());
        sb.append(t_nvovtrp.getSimpleView());
        sb.append(d_nvpotrp.getSimpleView());
        sb.append(m_nvcontact.getSimpleView());
        sb.append(t_nvcontact.getSimpleView());
        sb.append(m_nvderun.getSimpleView());
        sb.append(d_nvstff.getSimpleView());
        sb.append(q_nvdriver_adhes.getSimpleView());
        String simpleView = sb.toString();
        
        return simpleView;
    }

    /**
     * Builds and returns a Swing component representing the P3_11 packet.
     * <p>
     * The UI is arranged in a tabbed pane with one tab (since no Q_NVKINT is present)
     * containing all fields.
     * </p>
     *
     * @return a JScrollPane containing the packet's UI component.
     */
    @Override
    public Component getPacketComponent() {
        
        // Create a panel using MigLayout to arrange fields
        JPanel mainPanel = new JPanel(new MigLayout("wrap", "[]2[]2[]2[]2[]", "[]2[]2[]2[]"));
        // Add each field's component in order
        mainPanel.add(nid_packet.getComponent());
        mainPanel.add(q_dir.getComponent());
        mainPanel.add(l_packet.getComponent());
        mainPanel.add(q_scale.getComponent());
        mainPanel.add(d_validnv.getComponent());
        mainPanel.add(n_iter.getComponent(), "newline, spanx");
        mainPanel.add(v_nvshunt.getComponent());
        mainPanel.add(v_nvstff.getComponent());
        mainPanel.add(v_nvonsight.getComponent());
        mainPanel.add(v_nvunfit.getComponent());
        mainPanel.add(v_nvrel.getComponent());
        mainPanel.add(d_nvroll.getComponent());
        mainPanel.add(q_nvsrbktrg.getComponent());
        mainPanel.add(q_nvemrrls.getComponent());
        mainPanel.add(v_nvallowovtrp.getComponent());
        mainPanel.add(v_nvsupovtrp.getComponent());
        mainPanel.add(d_nvovtrp.getComponent());
        mainPanel.add(t_nvovtrp.getComponent());
        mainPanel.add(d_nvpotrp.getComponent());
        mainPanel.add(m_nvcontact.getComponent());
        mainPanel.add(t_nvcontact.getComponent());
        mainPanel.add(m_nvderun.getComponent());
        mainPanel.add(d_nvstff.getComponent());
        mainPanel.add(q_nvdriver_adhes.getComponent());

        // Since there is no Q_NVKINT field, we create a single tab.
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Národní nastavení", mainPanel);

        JPanel container = new JPanel();
        container.add(tabbedPane);

        // Add a listener to update the progress bar when any combo box is changed.
        GUIHelper.addActionListenerToAllComboBoxes(container, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                getjProgressBar().doClick();
            }
        });

        
        return new JScrollPane(container);
    }



    /**
     * Builds the binary data for this packet in two passes.
     * <p>
     * The first pass concatenates the full data of all fields, then updates the L_PACKET field
     * with the new bit length (using 13 bits). The second pass rebuilds the binary data.
     * </p>
     *
     * @return the final binary data string.
     */
    @Override
    public String getBinData() {
        
        StringBuilder sb = new StringBuilder();

        // First pass: concatenate full data of all fields.
        sb.append(nid_packet.getFullData());
        sb.append(q_dir.getFullData());
        sb.append(l_packet.getFullData());
        sb.append(q_scale.getFullData());
        sb.append(d_validnv.getFullData());
        sb.append(n_iter.getFullData());
        sb.append(v_nvshunt.getFullData());
        sb.append(v_nvstff.getFullData());
        sb.append(v_nvonsight.getFullData());
        sb.append(v_nvunfit.getFullData());
        sb.append(v_nvrel.getFullData());
        sb.append(d_nvroll.getFullData());
        sb.append(q_nvsrbktrg.getFullData());
        sb.append(q_nvemrrls.getFullData());
        sb.append(v_nvallowovtrp.getFullData());
        sb.append(v_nvsupovtrp.getFullData());
        sb.append(d_nvovtrp.getFullData());
        sb.append(t_nvovtrp.getFullData());
        sb.append(d_nvpotrp.getFullData());
        sb.append(m_nvcontact.getFullData());
        sb.append(t_nvcontact.getFullData());
        sb.append(m_nvderun.getFullData());
        sb.append(d_nvstff.getFullData());
        sb.append(q_nvdriver_adhes.getFullData());
        int currentLength = sb.length();
        

        // Update L_PACKET with the current length (using 13 bits)
        l_packet.setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(currentLength), 13));

        // Second pass: rebuild binary data after updating L_PACKET.
        sb.setLength(0);
        sb.append(nid_packet.getFullData());
        sb.append(q_dir.getFullData());
        sb.append(l_packet.getFullData());
        sb.append(q_scale.getFullData());
        sb.append(d_validnv.getFullData());
        sb.append(n_iter.getFullData());
        sb.append(v_nvshunt.getFullData());
        sb.append(v_nvstff.getFullData());
        sb.append(v_nvonsight.getFullData());
        sb.append(v_nvunfit.getFullData());
        sb.append(v_nvrel.getFullData());
        sb.append(d_nvroll.getFullData());
        sb.append(q_nvsrbktrg.getFullData());
        sb.append(q_nvemrrls.getFullData());
        sb.append(v_nvallowovtrp.getFullData());
        sb.append(v_nvsupovtrp.getFullData());
        sb.append(d_nvovtrp.getFullData());
        sb.append(t_nvovtrp.getFullData());
        sb.append(d_nvpotrp.getFullData());
        sb.append(m_nvcontact.getFullData());
        sb.append(t_nvcontact.getFullData());
        sb.append(m_nvderun.getFullData());
        sb.append(d_nvstff.getFullData());
        sb.append(q_nvdriver_adhes.getFullData());

        LOG.debug("Hex Data: " + ArithmeticalFunctions.bin2Hex(sb.toString()));
        return sb.toString();
    }

    /**
     * Returns a graphical visualization of the packet.
     * <p>
     * This implementation returns a tabbed pane containing a table with field names
     * and their current values. (Note: This is a basic placeholder.)
     * </p>
     *
     * @return the graphical visualization component
     */
    @Override
    public Component getGraphicalVisualization() {
    return null;
    }

    @Override
    public String getHexData() {
        String hexData = ArithmeticalFunctions.bin2Hex(getBinData());
        
        return hexData;
    }

    /**
     * Returns an HTML-formatted string representation of the P3_11 packet.
     *
     * @return the HTML string representation.
     */
    @Override
    public String toString() {
        
        return new HTMLTagGenerator()
                .startTag()
                .bold(getClass().getSimpleName())
                .cursive("Národní hodnoty")
                .underline(" [1.Y] ")
                .endTag()
                .getString();
    }
}
