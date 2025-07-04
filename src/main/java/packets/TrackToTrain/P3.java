package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Var.A.A_NVMAXREDADH1;
import packets.Var.A.A_NVMAXREDADH2;
import packets.Var.A.A_NVMAXREDADH3;
import packets.Var.D.D_NVROLL;
import packets.Var.D.D_NVOVTRP;
import packets.Var.D.D_NVPOTRP;
import packets.Var.D.D_NVSTFF;
import packets.Var.D.D_VALIDNV;
import packets.Var.L.L_PACKET;
import packets.Var.M.M_NVCONTACT;
import packets.Var.M.M_NVDERUN;
import packets.Var.M.M_VERSION;
import packets.Var.M.M_NVAVADH;
import packets.Var.M.M_NVEBCL;
import packets.Var.N.N_ITER;
import packets.Var.NID.NID_C;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.Q_DIR;
import packets.Var.Q.Q_NVDRIVER_ADHES;
import packets.Var.Q.Q_NVEMRRLS;
import packets.Var.Q.Q_NVGUIPERM;
import packets.Var.Q.Q_NVINHSMICPERM;
import packets.Var.Q.Q_NVLOCACC;
import packets.Var.Q.Q_NVSBFBPERM;
import packets.Var.Q.Q_NVSBTSMPERM;
import packets.Var.Q.Q_NVKINT;
import packets.Var.Q.Q_SCALE;
import packets.Var.T.T_NVCONTACT;
import packets.Var.T.T_NVOVTRP;
import packets.Var.V.*;

import net.miginfocom.swing.MigLayout;
import packets.Var.Variables;
import tools.crypto.ArithmeticalFunctions;
import tools.string.HTMLTagGenerator;
import tools.ui.GUIHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static tools.crypto.ArithmeticalFunctions.bin2Hex;
import static tools.ui.GUIHelper.loadAndScaleIcon;

/**
 * P3 packet represents a complex packet containing multiple variable fields.
 * Each field is stored as an individual member. This class provides methods to
 * generate various views (simple view, binary data, hex data, and a packet component)
 * by concatenating data from each field in order.
 */
public class P3 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P3.class);

    // Individual variable fields (order is significant)
    private final NID_PACKET nid_packet;
    private final Q_DIR q_dir;
    private final L_PACKET l_packet;
    private final Q_SCALE q_scale;
    private final D_VALIDNV d_validnv;
    private final NID_C nid_c;
    private final N_ITER n_iter;
    private final V_NVSHUNT v_nvshunt;
    private final V_NVSTFF v_nvstff;
    private final V_NVONSIGHT v_nvonsight;
    private final V_NVLIMSUPERV v_nvlimsuperv;
    private final V_NVUNFIT v_nvunfit;
    private final V_NVREL v_nvrel;
    private final D_NVROLL d_nvroll;
    private final Q_NVSBTSMPERM q_nvsbtsmperm;
    private final Q_NVEMRRLS q_nvemrrls;
    private final Q_NVGUIPERM q_nvguiperm;
    private final Q_NVSBFBPERM q_nvsbfbperm;
    private final Q_NVINHSMICPERM q_nvinhsmicperm;
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
    private final A_NVMAXREDADH1 a_nvmaxredadh1;
    private final A_NVMAXREDADH2 a_nvmaxredadh2;
    private final A_NVMAXREDADH3 a_nvmaxredadh3;
    private final Q_NVLOCACC q_nvlocacc;
    private final M_NVAVADH m_nvavadah;
    private final M_NVEBCL m_nvebcl;
    private final Q_NVKINT q_nvkint;

    /**
     * Default constructor using a default hex string.
     */
    public P3() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("03424E814480403100840804800A089100323FC0055FFFFFEFFFFCCA0DFBF00A548000A000")});
    }

    /**
     * Constructs a P3 packet by initializing each field with the given data.
     *
     * @param d the binary data array to initialize all variable fields
     */
    public P3(String[] d) {
        

        nid_packet       = (NID_PACKET) new NID_PACKET().initValueSet(d);
        

        q_dir            = (Q_DIR) new Q_DIR().initValueSet(d);
        

        l_packet         = (L_PACKET) new L_PACKET().initValueSet(d);
        

        q_scale          = (Q_SCALE) new Q_SCALE().initValueSet(d);
        

        d_validnv        = (D_VALIDNV) new D_VALIDNV().initValueSet(d);
        

        nid_c            = (NID_C) new NID_C().initValueSet(d);
        

        n_iter           = (N_ITER) new N_ITER("Platnost").addNewIterVar(new NID_C()).initValueSet(d);
        

        v_nvshunt        = (V_NVSHUNT) new V_NVSHUNT().initValueSet(d);
        

        v_nvstff         = (V_NVSTFF) new V_NVSTFF().initValueSet(d);
        

        v_nvonsight      = (V_NVONSIGHT) new V_NVONSIGHT().initValueSet(d);
        

        v_nvlimsuperv    = (V_NVLIMSUPERV) new V_NVLIMSUPERV().initValueSet(d);
        

        v_nvunfit        = (V_NVUNFIT) new V_NVUNFIT().initValueSet(d);
        

        v_nvrel          = (V_NVREL) new V_NVREL().initValueSet(d);
        

        d_nvroll         = (D_NVROLL) new D_NVROLL().initValueSet(d);
        

        q_nvsbtsmperm    = (Q_NVSBTSMPERM) new Q_NVSBTSMPERM().initValueSet(d);
        

        q_nvemrrls       = (Q_NVEMRRLS) new Q_NVEMRRLS().initValueSet(d);
        

        q_nvguiperm      = (Q_NVGUIPERM) new Q_NVGUIPERM().initValueSet(d);
        

        q_nvsbfbperm     = (Q_NVSBFBPERM) new Q_NVSBFBPERM().initValueSet(d);
        

        q_nvinhsmicperm  = (Q_NVINHSMICPERM) new Q_NVINHSMICPERM().initValueSet(d);
        

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
        

        a_nvmaxredadh1   = (A_NVMAXREDADH1) new A_NVMAXREDADH1().initValueSet(d);
        

        a_nvmaxredadh2   = (A_NVMAXREDADH2) new A_NVMAXREDADH2().initValueSet(d);
        

        a_nvmaxredadh3   = (A_NVMAXREDADH3) new A_NVMAXREDADH3().initValueSet(d);
        

        q_nvlocacc       = (Q_NVLOCACC) new Q_NVLOCACC().initValueSet(d);
        

        m_nvavadah       = (M_NVAVADH) new M_NVAVADH().initValueSet(d);
        

        m_nvebcl         = (M_NVEBCL) new M_NVEBCL().initValueSet(d);
        

        q_nvkint         = (Q_NVKINT) new Q_NVKINT().initValueSet(d);


        setIcon(loadAndScaleIcon("flags/pac/citizenship.png"));
    }

    @Override
    public String getSimpleView() {
        
        StringBuilder sb = new StringBuilder();
        sb.append(nid_packet.getSimpleView());
        sb.append(q_dir.getSimpleView());
        sb.append(l_packet.getSimpleView());
        sb.append(q_scale.getSimpleView());
        sb.append(d_validnv.getSimpleView());
        sb.append(nid_c.getSimpleView());
        sb.append(n_iter.getSimpleView());
        sb.append(v_nvshunt.getSimpleView());
        sb.append(v_nvstff.getSimpleView());
        sb.append(v_nvonsight.getSimpleView());
        sb.append(v_nvlimsuperv.getSimpleView());
        sb.append(v_nvunfit.getSimpleView());
        sb.append(v_nvrel.getSimpleView());
        sb.append(d_nvroll.getSimpleView());
        sb.append(q_nvsbtsmperm.getSimpleView());
        sb.append(q_nvemrrls.getSimpleView());
        sb.append(q_nvguiperm.getSimpleView());
        sb.append(q_nvsbfbperm.getSimpleView());
        sb.append(q_nvinhsmicperm.getSimpleView());
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
        sb.append(a_nvmaxredadh1.getSimpleView());
        sb.append(a_nvmaxredadh2.getSimpleView());
        sb.append(a_nvmaxredadh3.getSimpleView());
        sb.append(q_nvlocacc.getSimpleView());
        sb.append(m_nvavadah.getSimpleView());
        sb.append(m_nvebcl.getSimpleView());
        sb.append(q_nvkint.getSimpleView());
        String simpleView = sb.toString();
        
        return simpleView;
    }

    @Override
    public Component getPacketComponent() {
        
        // Create two panels to hold fields in two groups
        JPanel panel1 = new JPanel(new MigLayout("wrap", "[]2[]2[]2[]2[]", "[]2[]2[]2[]"));
        JPanel panel2 = new JPanel(new MigLayout("wrap", "[]", "[]"));

        // Add fields to panel1
        panel1.add(nid_packet.getComponent());
        panel1.add(q_dir.getComponent());
        panel1.add(l_packet.getComponent());
        panel1.add(q_scale.getComponent());
        panel1.add(d_validnv.getComponent());
        panel1.add(nid_c.getComponent());
        panel1.add(n_iter.getComponent(), "newline, spanx");
        panel1.add(v_nvshunt.getComponent());
        panel1.add(v_nvstff.getComponent());
        panel1.add(v_nvonsight.getComponent());
        panel1.add(v_nvlimsuperv.getComponent());
        panel1.add(v_nvunfit.getComponent());
        panel1.add(v_nvrel.getComponent());
        panel1.add(d_nvroll.getComponent());
        panel1.add(q_nvsbtsmperm.getComponent());
        panel1.add(q_nvemrrls.getComponent());
        panel1.add(q_nvguiperm.getComponent());
        panel1.add(q_nvsbfbperm.getComponent());
        panel1.add(q_nvinhsmicperm.getComponent());
        panel1.add(v_nvallowovtrp.getComponent());
        panel1.add(v_nvsupovtrp.getComponent());
        panel1.add(d_nvovtrp.getComponent());
        panel1.add(t_nvovtrp.getComponent());
        panel1.add(d_nvpotrp.getComponent());
        panel1.add(m_nvcontact.getComponent());
        panel1.add(t_nvcontact.getComponent());
        panel1.add(m_nvderun.getComponent());
        panel1.add(d_nvstff.getComponent());
        panel1.add(q_nvdriver_adhes.getComponent());
        panel1.add(a_nvmaxredadh1.getComponent());
        panel1.add(a_nvmaxredadh2.getComponent());
        panel1.add(a_nvmaxredadh3.getComponent());
        panel1.add(q_nvlocacc.getComponent());
        panel1.add(m_nvavadah.getComponent());
        panel1.add(m_nvebcl.getComponent());

        // Add field to panel2: Q_NVKINT is separated
        panel2.add(q_nvkint.getComponent(), "span, newline, grow");

        // Create a tabbed pane to hold both panels
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Národní nastavení", panel1);
        tabbedPane.addTab("Hodnoty pro Brzdné křivky", panel2);

        JPanel container = new JPanel();
        container.add(tabbedPane);

        // Add an action listener to update a progress bar when any combo box is changed
        GUIHelper.addActionListenerToAllComboBoxes(container, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                getjProgressBar().doClick();
            }
        });

        
        return new JScrollPane(container);
    }



    @Override
    public String getBinData() {
        
        StringBuilder sb = new StringBuilder();

        // First pass: build binary data from all fields
        sb.append(nid_packet.getFullData());
        sb.append(q_dir.getFullData());
        sb.append(l_packet.getFullData());
        sb.append(q_scale.getFullData());
        sb.append(d_validnv.getFullData());
        sb.append(nid_c.getFullData());
        sb.append(n_iter.getFullData());
        sb.append(v_nvshunt.getFullData());
        sb.append(v_nvstff.getFullData());
        sb.append(v_nvonsight.getFullData());
        sb.append(v_nvlimsuperv.getFullData());
        sb.append(v_nvunfit.getFullData());
        sb.append(v_nvrel.getFullData());
        sb.append(d_nvroll.getFullData());
        sb.append(q_nvsbtsmperm.getFullData());
        sb.append(q_nvemrrls.getFullData());
        sb.append(q_nvguiperm.getFullData());
        sb.append(q_nvsbfbperm.getFullData());
        sb.append(q_nvinhsmicperm.getFullData());
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
        sb.append(a_nvmaxredadh1.getFullData());
        sb.append(a_nvmaxredadh2.getFullData());
        sb.append(a_nvmaxredadh3.getFullData());
        sb.append(q_nvlocacc.getFullData());
        sb.append(m_nvavadah.getFullData());
        sb.append(m_nvebcl.getFullData());
        sb.append(q_nvkint.getFullData());
        int currentLength = sb.length();
        

        // Update l_packet with the current length (13 bits field)
        l_packet.setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(currentLength), 13));

        // Second pass: rebuild binary data with updated l_packet
        sb.setLength(0);
        sb.append(nid_packet.getFullData());
        sb.append(q_dir.getFullData());
        sb.append(l_packet.getFullData());
        sb.append(q_scale.getFullData());
        sb.append(d_validnv.getFullData());
        sb.append(nid_c.getFullData());
        sb.append(n_iter.getFullData());
        sb.append(v_nvshunt.getFullData());
        sb.append(v_nvstff.getFullData());
        sb.append(v_nvonsight.getFullData());
        sb.append(v_nvlimsuperv.getFullData());
        sb.append(v_nvunfit.getFullData());
        sb.append(v_nvrel.getFullData());
        sb.append(d_nvroll.getFullData());
        sb.append(q_nvsbtsmperm.getFullData());
        sb.append(q_nvemrrls.getFullData());
        sb.append(q_nvguiperm.getFullData());
        sb.append(q_nvsbfbperm.getFullData());
        sb.append(q_nvinhsmicperm.getFullData());
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
        sb.append(a_nvmaxredadh1.getFullData());
        sb.append(a_nvmaxredadh2.getFullData());
        sb.append(a_nvmaxredadh3.getFullData());
        sb.append(q_nvlocacc.getFullData());
        sb.append(m_nvavadah.getFullData());
        sb.append(m_nvebcl.getFullData());
        sb.append(q_nvkint.getFullData());

        LOG.debug("Hex Data: " + ArithmeticalFunctions.bin2Hex(sb.toString()));
        return sb.toString();
    }

    @Override
    public Component getGraphicalVisualization() {
        return null;
    }

    @Override
    public String getHexData() {
        String hexData = bin2Hex(getBinData());
        
        return hexData;
    }

    @Override
    public String toString() {
        
        return new HTMLTagGenerator()
                .startTag()
                .bold(getClass().getSimpleName())
                .cursive("Národní hodnoty")
                .underline(" [2.Y] ")
                .endTag()
                .getString();
    }
}
