package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Interfaces.IterationData;
import packets.Var.D.*;
import packets.Var.L.L_PACKET;
import packets.Var.N.N_ITER;
import packets.Var.NID.NID_BG;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.*;
import tools.crypto.ArithmeticalFunctions;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;
import net.miginfocom.swing.MigLayout;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import static tools.ui.GUIHelper.createModernTheme;

public class P5 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P5.class);

    private final XYSeriesCollection dataset;
    NID_PACKET nid_packet;
    Q_DIR q_dir;
    L_PACKET l_packet;
    Q_SCALE q_scale;
    D_LINK d_link;
    Q_NEWCOUNTRY q_newcountry;
    NID_BG nid_bg;
    Q_LINKORIENTATION q_linkorientation;
    Q_LINKREACTION q_linkreaction;
    Q_LOCACC q_locacc;
    N_ITER n_iter;
    JFreeChart chart;
    ChartPanel chartPanel;

    public P5() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("05408A800000014000")});
    }

    public P5(String[] d) {
        

        this.nid_packet = (NID_PACKET) new NID_PACKET().initValueSet(d);
        this.q_dir = (Q_DIR) new Q_DIR().initValueSet(d);
        this.l_packet = (L_PACKET) new L_PACKET().initValueSet(d);
        this.q_scale = (Q_SCALE) new Q_SCALE().initValueSet(d);
        this.d_link = (D_LINK) new D_LINK().initValueSet(d);
        this.q_newcountry = (Q_NEWCOUNTRY) new Q_NEWCOUNTRY().initValueSet(d);
        this.nid_bg = (NID_BG) new NID_BG().initValueSet(d);
        this.q_linkorientation = (Q_LINKORIENTATION) new Q_LINKORIENTATION().initValueSet(d);
        this.q_linkreaction = (Q_LINKREACTION) new Q_LINKREACTION().initValueSet(d);
        this.q_locacc = (Q_LOCACC) new Q_LOCACC().initValueSet(d);

        this.n_iter = new N_ITER("Následující BG")
                .addNewIterVar(new D_LINK())
                .addNewIterVar(new Q_NEWCOUNTRY())
                .addNewIterVar(new NID_BG())
                .addNewIterVar(new Q_LINKORIENTATION())
                .addNewIterVar(new Q_LINKREACTION())
                .addNewIterVar(new Q_LOCACC());
        this.n_iter = (N_ITER) this.n_iter.initValueSet(d);

        setIcon(GUIHelper.getImageIconFromResources("icons8-link-80"));
        

        dataset = new XYSeriesCollection();
        chart = ChartFactory.createXYLineChart(
                "Linkování",
                "Vzdálenost [" + q_scale.getCombo().get(q_scale.getDecValue()) + "]",
                "-",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );
        chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRenderer(new P27.CustomStrokeRenderer());

        StandardChartTheme theme = createModernTheme();
        theme.apply(chart);

        
    }

    @Override
    public Component getPacketComponent() {
        

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new MigLayout("nogrid", "[]10[]10[]", "[]10[]10[]10[]"));
        jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Adding variable components in a structured layout
        jPanel.add(nid_packet.getComponent());
        jPanel.add(q_dir.getComponent());
        jPanel.add(l_packet.getComponent());
        jPanel.add(q_scale.getComponent(), "newline");
        jPanel.add(d_link.getComponent());
        jPanel.add(nid_bg.getComponent());
        jPanel.add(q_newcountry.getComponent(), "newline");
        jPanel.add(q_linkorientation.getComponent(), "newline");
        jPanel.add(q_linkreaction.getComponent());
        jPanel.add(q_locacc.getComponent());
        jPanel.add(n_iter.getComponent(), "span,push,growx,newline");

        // Refresh action
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

    @Override
    public String getHexData() {
        String hexData = ArithmeticalFunctions.bin2Hex(getBinData());
        
        return hexData;
    }

    @Override
    public String getBinData() {
        

        // First pass
        String tmp = "";
        tmp += nid_packet.getFullData();
        tmp += q_dir.getFullData();
        tmp += l_packet.getFullData();
        tmp += q_scale.getFullData();
        tmp += d_link.getFullData();
        tmp += q_newcountry.getFullData();
        tmp += nid_bg.getFullData();
        tmp += q_linkorientation.getFullData();
        tmp += q_linkreaction.getFullData();
        tmp += q_locacc.getFullData();
        tmp += n_iter.getFullData();

        int lengthFirstPass = tmp.length();
        

        // Update l_packet with the new length
        l_packet.setBinValue(
                ArithmeticalFunctions.dec2XBin(String.valueOf(lengthFirstPass), 13));

        // Second pass
        tmp = "";
        tmp += nid_packet.getFullData();
        tmp += q_dir.getFullData();
        tmp += l_packet.getFullData();
        tmp += q_scale.getFullData();
        tmp += d_link.getFullData();
        tmp += q_newcountry.getFullData();
        tmp += nid_bg.getFullData();
        tmp += q_linkorientation.getFullData();
        tmp += q_linkreaction.getFullData();
        tmp += q_locacc.getFullData();
        tmp += n_iter.getFullData();

        LOG.debug("Hex Data: " + ArithmeticalFunctions.bin2Hex(tmp.toString()));
        return tmp;
    }

    public void updateG() {
        

        dataset.removeAllSeries();

        int x = d_link.getDecValue();
        int y = 0;

        XYSeries xySeries2 = new XYSeries("BG " + nid_bg.getDecValue());
        dataset.addSeries(xySeries2);

        xySeries2.add(x - q_locacc.getDecValue(), y);
        xySeries2.add(x + q_locacc.getDecValue(), y);

        for (IterationData iterationData : n_iter.getData()) {
            // iterationData indexes:
            // 0 -> D_LINK
            // 1 -> Q_NEWCOUNTRY
            // 2 -> NID_BG
            // 3 -> Q_LINKORIENTATION
            // 4 -> Q_LINKREACTION
            // 5 -> Q_LOCACC

            XYSeries xySeries = new XYSeries("BG " + iterationData.get(2).getDecValue() + ":" + new Random().nextInt());
            dataset.addSeries(xySeries);

            x += iterationData.get(0).getDecValue();

            xySeries.add(x - iterationData.get(5).getDecValue(), y);
            xySeries.add(x + iterationData.get(5).getDecValue(), y);
        }

        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis newAxis = new NumberAxis("Vzdálenost [" +
                q_scale.getCombo().get(q_scale.getDecValue()) + "]");
        plot.setDomainAxis(newAxis);

        
    }

    @Override
    public Component getGraphicalVisualization() {
        
        updateG();
        return chartPanel;
    }

    @Override
    public String toString() {
        
        return new HTMLTagGenerator()
                .startTag()
                .bold(getClass().getSimpleName())
                .cursive("Linkování")
                .underline("[X.Y]")
                .endTag()
                .getString();
    }

    @Override
    public String getSimpleView() {
        
        StringBuilder sb = new StringBuilder();
        sb.append(nid_packet.getSimpleView());
        sb.append(q_dir.getSimpleView());
        sb.append(l_packet.getSimpleView());
        sb.append(q_scale.getSimpleView());
        sb.append(d_link.getSimpleView());
        sb.append(q_newcountry.getSimpleView());
        sb.append(nid_bg.getSimpleView());
        sb.append(q_linkorientation.getSimpleView());
        sb.append(q_linkreaction.getSimpleView());
        sb.append(q_locacc.getSimpleView());
        sb.append(n_iter.getSimpleView());

        String simpleView = sb.toString();
        
        return simpleView;
    }
}
