package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.*;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import packets.Interfaces.IterationData;
import packets.Var.A.G_A;
import packets.Var.D.D_GRADIENT;
import packets.Var.L.L_PACKET;
import packets.Var.N.N_ITER;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.Q_DIR;
import packets.Var.Q.Q_GDIR;
import packets.Var.Q.Q_SCALE;
import tools.crypto.ArithmeticalFunctions;
import tools.packetgraph.CustomNumberAxis;
import tools.packetgraph.CustomStrokeRenderer;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;
import net.miginfocom.swing.MigLayout;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

import static tools.ui.GUIHelper.createModernTheme;
import static tools.ui.GUIHelper.loadAndScaleIcon;

public class P21 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P21.class);

    private final XYSeriesCollection dataset;
    NID_PACKET nid_packet;
    Q_DIR q_dir;
    L_PACKET l_packet;
    Q_SCALE q_scale;
    D_GRADIENT d_gradient;
    Q_GDIR q_gdir;
    G_A g_a;
    N_ITER n_iter;
    JFreeChart chart;
    ChartPanel chartPanel;

    public P21() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("15406C80000000")});
    }

    public P21(String[] d) {
        

        this.nid_packet = (NID_PACKET) new NID_PACKET().initValueSet(d);
        

        this.q_dir = (Q_DIR) new Q_DIR().initValueSet(d);
        

        this.l_packet = (L_PACKET) new L_PACKET().initValueSet(d);
        

        this.q_scale = (Q_SCALE) new Q_SCALE().initValueSet(d);
        

        this.d_gradient = (D_GRADIENT) new D_GRADIENT().initValueSet(d);
        

        this.q_gdir = (Q_GDIR) new Q_GDIR().initValueSet(d);
        

        this.g_a = (G_A) new G_A().initValueSet(d);
        

        this.n_iter = new N_ITER();
        n_iter.addNewIterVar(new D_GRADIENT())
                .addNewIterVar(new Q_GDIR())
                .addNewIterVar(new G_A());
        this.n_iter.initValueSet(d);


        setIcon(loadAndScaleIcon("flags/pac/header.png"));

        dataset = new XYSeriesCollection();
        chart = ChartFactory.createXYLineChart(
                "Gradient",
                "Vzdálenost [" + q_scale.getCombo().get(q_scale.getDecValue()) + "]",
                "Promile",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );
        chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);

        XYPlot plot = (XYPlot) chart.getPlot();

        // Custom stroke renderer
        plot.setRenderer(new CustomStrokeRenderer());

        StandardChartTheme theme = createModernTheme();

        theme.apply(chart);

        
    }

    @Override
    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel(new MigLayout("wrap", "[]10[]10[]10[]", "[]10[]10[]10[]"));
        jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        jPanel.add(nid_packet.getComponent());
        jPanel.add(q_dir.getComponent());
        jPanel.add(l_packet.getComponent());
        jPanel.add(q_scale.getComponent());
        jPanel.add(d_gradient.getComponent());
        jPanel.add(q_gdir.getComponent());
        jPanel.add(g_a.getComponent());

        jPanel.add(n_iter.getComponent(), "span,newline,grow");

        GUIHelper.addActionListenerToAllComboBoxes(jPanel, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                updateG();
                getjProgressBar().doClick();
            }
        });

        JPanel jPanel1 = new JPanel();
        jPanel1.add(jPanel);
        
        return new JScrollPane(jPanel1);
    }

    public void updateG() {
        
        dataset.removeAllSeries();

        int z;
        int x = d_gradient.getDecValue();
        int y = g_a.getDecValue();
        z = (q_dir.getDecValue() == 0) ? -1 : 1;

        XYSeries xySeries2 = new XYSeries("Gradient");
        dataset.addSeries(xySeries2);

        xySeries2.add(x, y * z);

        for (IterationData iterationData : n_iter.getData()) {
            x += iterationData.get(0).getDecValue();
            xySeries2.add(x, y * z);

            y = iterationData.get(2).getDecValue();
            z = (iterationData.get(1).getDecValue() == 0) ? -1 : 1;
            xySeries2.add(x, y * z);

            xySeries2.add(x, y * z);
        }

        xySeries2.add(32676, y * z);




        XYPlot plot = (XYPlot) chart.getPlot();

        NumberAxis newAxis = new NumberAxis("Vzdálenost [" + q_scale.getCombo().get(q_scale.getDecValue()) + "]");

        plot.setDomainAxis(newAxis);

        

        
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
        tmp = getBinDataPrivately(tmp);
        int firstPassLength = tmp.length();
        

        l_packet.setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(firstPassLength), 13));

        // Second pass
        tmp = "";
        tmp = getBinDataPrivately(tmp);

        LOG.debug("Hex Data: " + ArithmeticalFunctions.bin2Hex(tmp.toString()));

        return tmp;
    }

    private String getBinDataPrivately(String tmp) {
        
        tmp += nid_packet.getFullData();
        tmp += q_dir.getFullData();
        tmp += l_packet.getFullData();
        tmp += q_scale.getFullData();
        tmp += d_gradient.getFullData();
        tmp += q_gdir.getFullData();
        tmp += g_a.getFullData();
        tmp += n_iter.getFullData();
        return tmp;
    }

    @Override
    public Component getGraphicalVisualization() {
        
        updateG();
        return chartPanel;
    }

    @Override
    public String getSimpleView() {
        
        StringBuilder sb = new StringBuilder();
        sb.append(nid_packet.getSimpleView());
        sb.append(q_dir.getSimpleView());
        sb.append(l_packet.getSimpleView());
        sb.append(q_scale.getSimpleView());
        sb.append(d_gradient.getSimpleView());
        sb.append(q_gdir.getSimpleView());
        sb.append(g_a.getSimpleView());
        sb.append(n_iter.getSimpleView());

        String simpleView = sb.toString();
        
        return simpleView;
    }

    @Override
    public String toString() {
        
        return new HTMLTagGenerator()
                .startTag()
                .bold(getClass().getSimpleName())
                .cursive("Gradient")
                .underline("[X.Y]")
                .endTag()
                .getString();
    }
}
