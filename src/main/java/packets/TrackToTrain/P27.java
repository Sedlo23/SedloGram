package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Interfaces.IterationData;
import packets.Var.D.D_STATIC;
import packets.Var.L.L_PACKET;
import packets.Var.N.N_ITER;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.*;
import packets.Var.V.V_DIFF;
import packets.Var.V.V_STATIC;
import tools.crypto.ArithmeticalFunctions;
import tools.packetgraph.CustomNumberAxis;
import tools.packetgraph.CustomStrokeRenderer;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;
import net.miginfocom.swing.MigLayout;

import org.jfree.chart.*;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.plot.*;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import static tools.ui.GUIHelper.createModernTheme;
import static tools.ui.GUIHelper.loadAndScaleIcon;

public class P27 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P27.class);

    public static final int MaxDistanceETCS = 32726;

    NID_PACKET nid_packet;
    Q_DIR q_dir;
    L_PACKET l_packet;
    Q_SCALE q_scale;
    D_STATIC d_static;
    V_STATIC v_static;
    Q_FRONT q_front;
    N_ITER n_iter;
    N_ITER n_iter2;
    JFreeChart chart;
    ChartPanel chartPanel;
    XYSeriesCollection dataset;

    // Series for different speeds
    XYSeries ssp = new XYSeries("Základní");
    XYSeries m80mm = new XYSeries(" 80mm");
    XYSeries m100mm = new XYSeries("100mm");
    XYSeries m130mm = new XYSeries("130mm");
    XYSeries m150mm = new XYSeries("150mm");
    XYSeries m165mm = new XYSeries("165mm");
    XYSeries m180mm = new XYSeries("180mm");
    XYSeries m210mm = new XYSeries("210mm");
    XYSeries m225mm = new XYSeries("225mm");
    XYSeries m245mm = new XYSeries("245mm");
    XYSeries m275mm = new XYSeries("275mm");
    XYSeries m300mm = new XYSeries("300mm");
    XYSeries NakP = new XYSeries("Nákládní P");
    XYSeries NakG = new XYSeries("Nákladní G");
    XYSeries Oso = new XYSeries("Osobní");

    public P27() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("1B01EC83E81910040128307D040200A02601F40D0802409607D1FC20000270")});
    }

    public P27(String[] d) {

        this.nid_packet = (NID_PACKET) new NID_PACKET().initValueSet(d);

        this.q_dir = (Q_DIR) new Q_DIR().initValueSet(d);

        this.l_packet = (L_PACKET) new L_PACKET().initValueSet(d);

        this.q_scale = (Q_SCALE) new Q_SCALE().initValueSet(d);

        this.d_static = (D_STATIC) new D_STATIC().initValueSet(d);

        this.v_static = (V_STATIC) new V_STATIC().initValueSet(d);

        this.q_front = (Q_FRONT) new Q_FRONT().initValueSet(d);

        this.n_iter = new N_ITER();
        n_iter.addNewIterVar(new Q_DIFF())
                .addNewIterVar(new V_DIFF())
                .setWRAPINT(2);
        this.n_iter.initValueSet(d);

        this.n_iter2 = new N_ITER("Rychlostní zlom");
        this.n_iter2
                .addNewIterVar(new D_STATIC())
                .addNewIterVar(new V_STATIC())
                .addNewIterVar(new Q_FRONT())
                .addNewIterVar(
                        (N_ITER) new N_ITER()
                                .addNewIterVar(new Q_DIFF())
                                .addNewIterVar(new V_DIFF())
                                .setWRAPINT(5)
                ).setWRAPINT(3);
        this.n_iter2 = (N_ITER) this.n_iter2.initValueSet(d);

        setIcon(loadAndScaleIcon("flags/pac/icons8-graph-50.png"));
        // Chart creation
        dataset = new XYSeriesCollection();
        chart = ChartFactory.createXYLineChart(
                "Rychlostní profil " + q_dir.getCombo().get(q_dir.getDecValue()),
                "Vzdálenost [" + q_scale.getCombo().get(q_scale.getDecValue()) + "]",
                "Rychlost [km/h]",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Add series to dataset
        dataset.addSeries(ssp);
        dataset.addSeries(m80mm);
        dataset.addSeries(m100mm);
        dataset.addSeries(m130mm);
        dataset.addSeries(m150mm);
        dataset.addSeries(m165mm);
        dataset.addSeries(m180mm);
        dataset.addSeries(m210mm);
        dataset.addSeries(m225mm);
        dataset.addSeries(m245mm);
        dataset.addSeries(m275mm);
        dataset.addSeries(m300mm);
        dataset.addSeries(NakP);
        dataset.addSeries(NakG);
        dataset.addSeries(Oso);

        // Configure chart appearance
        XYPlot plot = (XYPlot) chart.getPlot();

        // Setup custom axes that only show used values
        CustomNumberAxis domainAxis = new CustomNumberAxis("Vzdálenost [" + q_scale.getCombo().get(q_scale.getDecValue()) + "]");
        CustomNumberAxis rangeAxis = new CustomNumberAxis("Rychlost [km/h]");

        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);

        // Configure grid to only show at tick marks
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        plot.setDomainMinorGridlinesVisible(false);
        plot.setRangeMinorGridlinesVisible(false);

        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);

        chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);

        chartPanel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                ChartEntity entity = event.getEntity();
                if (entity instanceof LegendItemEntity) {
                    LegendItemEntity legendItemEntity = (LegendItemEntity) entity;
                    Comparable seriesKey = legendItemEntity.getSeriesKey();
                    int seriesIndex = dataset.getSeriesIndex(seriesKey);
                    boolean seriesVisible = plot.getRenderer().isSeriesVisible(seriesIndex);
                    plot.getRenderer().setSeriesVisible(seriesIndex, !seriesVisible);
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
                // No action needed on mouse move
            }
        });

        // Custom stroke renderer
        plot.setRenderer(new CustomStrokeRenderer());

        // Apply custom theme
        StandardChartTheme theme = createModernTheme();

        theme.apply(chart);
    }

    /**
     * Updates the XYSeries with computed speed profile segments.
     */
    public void updateG() {
        // Clear all series
        ssp.clear();
        m80mm.clear();
        m100mm.clear();
        m130mm.clear();
        m150mm.clear();
        m165mm.clear();
        m180mm.clear();
        m210mm.clear();
        m225mm.clear();
        m245mm.clear();
        m275mm.clear();
        m300mm.clear();
        NakP.clear();
        NakG.clear();
        Oso.clear();

        // Sets to collect unique X and Y values
        Set<Double> xValues = new TreeSet<>();
        Set<Double> yValues = new TreeSet<>();

        int x = 0;
        int y = v_static.getDecValue() * 5;
        ssp.add(d_static.getDecValue(), y);
        xValues.add((double)d_static.getDecValue());
        yValues.add((double)y);

        x = d_static.getDecValue();
        IterationData n = null;

        // Build the main series from n_iter2
        for (int i = 0; i < n_iter2.getData().size(); i++) {
            n = n_iter2.getData().get(i);

            // Increment x by D_STATIC from iteration
            x += n.get(0).getDecValue();
            ssp.add(x, y);
            xValues.add((double)x);
            yValues.add((double)y);

            int checkValue = n.get(1).getDecValue();
            if (checkValue == 127) {
                // 127 => end of infinite speed profile
                break;
            }
            y = checkValue * 5;
            ssp.add(x, y);
            yValues.add((double)y);
        }

        // Additional speed-difference series from n_iter
        x = d_static.getDecValue();
        for (IterationData t : n_iter.getData()) {
            int u = x;
            if (!n_iter2.getData().isEmpty()) {
                u += n_iter2.getData().get(0).get(0).getDecValue();
            }
            if (v_static.getDecValue() == 127) {
                u = 250; // Potential fallback value for infinite profile
            }

            addDiffSeries(t, x, u, xValues, yValues);
        }

        // Additional differences from second iteration array
        for (int i = 0; i < n_iter2.getData().size() - 1; i++) {
            IterationData nx = n_iter2.getData().get(i);
            IterationData n1 = n_iter2.getData().get(i + 1);

            x += nx.get(0).getDecValue();
            int x1 = n1.get(0).getDecValue();

            // Sub-iteration (N_ITER)
            N_ITER subIter = (N_ITER) nx.get(3);
            for (IterationData subT : subIter.getData()) {
                int u = x1 + x;
                addDiffSeries(subT, x, u, xValues, yValues);
            }
        }

        // If the last v_static or last iteration hasn't signaled infinite
        // then we add an extended line up to MaxDistanceETCS
        int z = v_static.getDecValue();
        if (n != null) {
            z = n.get(1).getDecValue();
        }

        if (z != 127) {
            double xNo = (Double) ssp.getDataItem(ssp.getItemCount() - 1).getX();
            ssp.add(MaxDistanceETCS, y);
            xValues.add((double)MaxDistanceETCS);

            for (IterationData t : n_iter.getData()) {
                int u = x;
                if (!n_iter2.getData().isEmpty()) {
                    u += n_iter2.getData().get(0).get(0).getDecValue();
                }
                u = MaxDistanceETCS;
                addDiffSeries(t, (int) xNo, u, xValues, yValues);
            }
        }

        if (chart == null) {
            return;
        }

        // Update axes with collected unique values
        XYPlot plot = (XYPlot) chart.getPlot();

        // Update custom axes with the collected values
        CustomNumberAxis domainAxis = (CustomNumberAxis) plot.getDomainAxis();
        CustomNumberAxis rangeAxis = (CustomNumberAxis) plot.getRangeAxis();

        domainAxis.setValues(xValues);
        rangeAxis.setValues(yValues);

        // Set range to fit all data points plus margin
        double xMin = Collections.min(xValues);
        double xMax = Collections.max(xValues);
        double yMin = Collections.min(yValues);
        double yMax = Collections.max(yValues);

        double xMargin = (xMax - xMin) * 0.05;
        double yMargin = (yMax - yMin) * 0.05;

        domainAxis.setRange(xMin - xMargin, xMax + xMargin);
        rangeAxis.setRange(yMin - yMargin, yMax + yMargin);

        // Update axis labels
        domainAxis.setLabel("Vzdálenost [" + q_scale.getCombo().get(q_scale.getDecValue()) + "]");
        chart.setTitle("Rychlostní profil " + q_dir.getCombo().get(q_dir.getDecValue()));

        // Rebuild legend items
        LegendItemCollection legendItems = new LegendItemCollection();
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            legendItems.add(plot.getLegendItems().get(i));
        }
        plot.setFixedLegendItems(legendItems);
    }

    /**
     * Adds speed difference lines to the appropriate series based on Q_DIFF type.
     *
     * @param t the iteration data containing Q_DIFF and V_DIFF
     * @param x the start X coordinate
     * @param u the end X coordinate
     * @param xValues set to collect unique X values
     * @param yValues set to collect unique Y values
     */
    private void addDiffSeries(IterationData t, int x, int u, Set<Double> xValues, Set<Double> yValues) {
        int speedVal = t.get(1).getDecValue() * 5;
        Q_DIFF qDiff = (Q_DIFF) t.get(0);

        // Track X and Y values for axis labels
        xValues.add((double)x);
        xValues.add((double)u);
        yValues.add((double)speedVal);

        if (qDiff.getDecValue() == 0) {
            switch (qDiff.getNc_cddiff().getDecValue()) {
                case 0:
                    m80mm.add(x, speedVal);
                    m80mm.add(u, speedVal);
                    break;
                case 1:
                    m100mm.add(x, speedVal);
                    m100mm.add(u, speedVal);
                    break;
                case 2:
                    m130mm.add(x, speedVal);
                    m130mm.add(u, speedVal);
                    break;
                case 3:
                    m150mm.add(x, speedVal);
                    m150mm.add(u, speedVal);
                    break;
                case 4:
                    m165mm.add(x, speedVal);
                    m165mm.add(u, speedVal);
                    break;
                case 5:
                    m180mm.add(x, speedVal);
                    m180mm.add(u, speedVal);
                    break;
                case 6:
                    m210mm.add(x, speedVal);
                    m210mm.add(u, speedVal);
                    break;
                case 7:
                    m225mm.add(x, speedVal);
                    m225mm.add(u, speedVal);
                    break;
                case 8:
                    m245mm.add(x, speedVal);
                    m245mm.add(u, speedVal);
                    break;
                case 9:
                    m275mm.add(x, speedVal);
                    m275mm.add(u, speedVal);
                    break;
                case 10:
                    m300mm.add(x, speedVal);
                    m300mm.add(u, speedVal);
                    break;
                default:
                    break;
            }
        } else {
            switch (qDiff.getNc_cddiff().getDecValue()) {
                case 0:
                    NakP.add(x, speedVal);
                    NakP.add(u, speedVal);
                    break;
                case 1:
                    NakG.add(x, speedVal);
                    NakG.add(u, speedVal);
                    break;
                case 2:
                    Oso.add(x, speedVal);
                    Oso.add(u, speedVal);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public Component getPacketComponent() {
        JPanel jPanel = new JPanel(new MigLayout("wrap", "[]10[]10[]10[]", "[]10[]10[]"));

        jPanel.add(nid_packet.getComponent());
        jPanel.add(q_dir.getComponent());
        jPanel.add(l_packet.getComponent());
        jPanel.add(q_scale.getComponent());
        jPanel.add(d_static.getComponent());
        jPanel.add(v_static.getComponent());
        jPanel.add(q_front.getComponent());
        jPanel.add(n_iter.getComponent(), "span,push,newline");
        jPanel.add(n_iter2.getComponent(), "span,push,newline");

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
        String tmp = getBinDataprivatly("");
        int firstPassLength = tmp.length();

        // Update l_packet
        l_packet.setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(firstPassLength), 13));

        // Second pass
        tmp = getBinDataprivatly("");
        LOG.debug("Hex Data: " + ArithmeticalFunctions.bin2Hex(tmp.toString()));

        return tmp;
    }

    private String getBinDataprivatly(String tmp) {
        tmp += nid_packet.getFullData();
        tmp += q_dir.getFullData();
        tmp += l_packet.getFullData();
        tmp += q_scale.getFullData();
        tmp += d_static.getFullData();
        tmp += v_static.getFullData();
        tmp += q_front.getFullData();
        tmp += n_iter.getFullData();
        tmp += n_iter2.getFullData();
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
        sb.append(d_static.getSimpleView());
        sb.append(v_static.getSimpleView());
        sb.append(q_front.getSimpleView());
        sb.append(n_iter.getSimpleView());
        sb.append(n_iter2.getSimpleView());
        String simpleView = sb.toString();
        return simpleView;
    }

    @Override
    public String toString() {
        return new HTMLTagGenerator().startTag()
                .bold(getClass().getSimpleName())
                .cursive("Rychlostní profil")
                .underline("[X.Y]")
                .endTag()
                .getString();
    }

}