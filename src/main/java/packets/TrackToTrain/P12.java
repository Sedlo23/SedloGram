package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Interfaces.IterationData;
import packets.Var.L.L_ENDSECTION;
import packets.Var.L.L_PACKET;
import packets.Var.L.L_SECTION;
import packets.Var.N.N_ITER;
import packets.Var.NID.NID_PACKET;
import packets.Var.Q.*;
import packets.Var.T.T_EMA;
import packets.Var.V.V_EMA;
import packets.Var.V.V_MAIN;
import tools.crypto.ArithmeticalFunctions;
import tools.packetgraph.CustomStrokeRenderer;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;
import net.miginfocom.swing.MigLayout;

import org.jfree.chart.*;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.*;

import static tools.ui.GUIHelper.createModernTheme;

public class P12 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P12.class);

    private final XYSeriesCollection dataset;
    NID_PACKET nid_packet;
    Q_DIR q_dir;
    L_PACKET l_packet;
    Q_SCALE q_scale;
    V_MAIN v_main;
    V_EMA v_ema;
    T_EMA t_ema;
    N_ITER n_iter;
    L_ENDSECTION l_endsection;
    Q_SECTIONTIMER q_sectiontimer;
    Q_ENDTIMER q_endtimer;
    Q_DANGERPOINT q_dangerpoint;
    Q_OVERLAP q_overlap;
    JFreeChart chart;
    ChartPanel chartPanel;

    public P12() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("0C4092A001FF8009000")});
    }

    public P12(String[] d) {
        

        this.nid_packet = (NID_PACKET) new NID_PACKET().initValueSet(d);
        

        this.q_dir = (Q_DIR) new Q_DIR().initValueSet(d);
        

        this.l_packet = (L_PACKET) new L_PACKET().initValueSet(d);
        

        this.q_scale = (Q_SCALE) new Q_SCALE().initValueSet(d);
        

        this.v_main = (V_MAIN) new V_MAIN().initValueSet(d);
        

        this.v_ema = (V_EMA) new V_EMA().initValueSet(d);
        

        this.t_ema = (T_EMA) new T_EMA().initValueSet(d);
        

        this.n_iter = new N_ITER("Úsek oprávnění k jízdě")
                .addNewIterVar(new L_SECTION())
                .addNewIterVar(new Q_SECTIONTIMER());
        this.n_iter = (N_ITER) this.n_iter.initValueSet(d);
        this.n_iter.setWRAPINT(2);
        

        this.l_endsection = (L_ENDSECTION) new L_ENDSECTION().initValueSet(d);
        

        this.q_sectiontimer = (Q_SECTIONTIMER) new Q_SECTIONTIMER().initValueSet(d);
        

        this.q_endtimer = (Q_ENDTIMER) new Q_ENDTIMER().initValueSet(d);
        

        this.q_dangerpoint = (Q_DANGERPOINT) new Q_DANGERPOINT().initValueSet(d);
        

        this.q_overlap = (Q_OVERLAP) new Q_OVERLAP().initValueSet(d);
        

        setIcon(GUIHelper.getImageIconFromResources("icons8-stop-train-80"));
        

        dataset = new XYSeriesCollection();

        // Initial chart creation
        chart = ChartFactory.createXYLineChart(
                "Rychlostní profil",
                "Vzdálenost [" + q_scale.getCombo().get(q_scale.getDecValue()) + "]",
                "Rychlost [km/h]",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);

        chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRenderer(new CustomStrokeRenderer());

        StandardChartTheme theme = createModernTheme();
        theme.apply(chart);

        // Second chart creation (apparently a re-init with same parameters)
        chart = ChartFactory.createXYLineChart(
                "Rychlostní profil",
                "Vzdálenost [" + q_scale.getCombo().get(q_scale.getDecValue()) + "]",
                "Rychlost [km/h]",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);

        chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);

        plot = (XYPlot) chart.getPlot();
        plot.setRenderer(new CustomStrokeRenderer());

        theme = createModernTheme();
        theme.apply(chart);

        
    }

    @Override
    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new MigLayout("wrap 4", "[grow]10[grow]10[grow]10[grow]", "[]10[]10[]10[]"));
        jPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // First row
        jPanel.add(nid_packet.getComponent(), "grow");
        jPanel.add(q_dir.getComponent(), "grow");
        jPanel.add(l_packet.getComponent(), "grow");
        jPanel.add(q_scale.getComponent(), "grow, wrap");

        // Second row
        jPanel.add(v_main.getComponent(), "grow");
        jPanel.add(v_ema.getComponent(), "grow");
        jPanel.add(t_ema.getComponent(), "grow");
        jPanel.add(l_endsection.getComponent(), "span, grow, wrap");

        // Third row
        jPanel.add(q_endtimer.getComponent(), "grow");
        jPanel.add(q_dangerpoint.getComponent(), "grow");
        jPanel.add(q_sectiontimer.getComponent(), "grow");

        // Fourth row
        jPanel.add(q_overlap.getComponent(), "grow, wrap");

        // Fifth row: n_iter across full width
        jPanel.add(n_iter.getComponent(), "span, growx, wrap");

        // Add action listeners
        GUIHelper.addActionListenerToAllComboBoxes(jPanel, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                getjProgressBar().doClick();
                updateG();
            }
        });

        JPanel jPanel1 = new JPanel();
        jPanel1.add(jPanel);
        
        return new JScrollPane(jPanel1);
    }

    public void updateG() {
        
        dataset.removeAllSeries();

        int x = 0, y = v_main.getDecValue() * 5;
        XYSeries xySeries = new XYSeries("END");
        dataset.addSeries(xySeries);

        int sec = 1;
        for (IterationData iterationData : n_iter.getData()) {
            XYSeries xySeries2 = new XYSeries("" + (sec + 1));
            dataset.addSeries(xySeries2);

            xySeries2.add(x, y);
            x += iterationData.get(0).getDecValue();
            xySeries2.add(x, y);

            sec++;
        }
        xySeries.add(x, y);

        x += l_endsection.getDecValue();
        xySeries.add(x, y);

        y = v_ema.getDecValue() * 5;
        xySeries.add(x, y);

        if (y != 0) {
            String s = t_ema.getDecValue() + "s";
            if (t_ema.getDecValue() == 1023) {
                s = "";
            }
            XYSeries xySeries2 = new XYSeries("V_EMA " + s);
            dataset.addSeries(xySeries2);

            xySeries2.add(x, y);
            xySeries2.add(x + 75, y);
        }

        if (q_dangerpoint.getDecValue() == 1) {
            y = q_dangerpoint.getV_releasedp().getDecValue() * 5;
            XYSeries xySeries2 = new XYSeries("DP");
            dataset.addSeries(xySeries2);
            xySeries2.add(x, y);
            xySeries2.add(x + q_dangerpoint.getD_dp().getDecValue(), y);
        }

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.getDomainAxis().setLabel("Vzdálenost [" + q_scale.getCombo().get(q_scale.getDecValue()) + "]");
        
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
        tmp += nid_packet.getFullData();
        tmp += q_dir.getFullData();
        tmp += l_packet.getFullData();
        tmp += q_scale.getFullData();
        tmp += v_main.getFullData();
        tmp += v_ema.getFullData();
        tmp += t_ema.getFullData();
        tmp += n_iter.getFullData();
        tmp += l_endsection.getFullData();
        tmp += q_sectiontimer.getFullData();
        tmp += q_endtimer.getFullData();
        tmp += q_dangerpoint.getFullData();
        tmp += q_overlap.getFullData();

        int firstPassLength = tmp.length();
        
        l_packet.setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(firstPassLength), 13));

        // Second pass
        tmp = "";
        tmp += nid_packet.getFullData();
        tmp += q_dir.getFullData();
        tmp += l_packet.getFullData();
        tmp += q_scale.getFullData();
        tmp += v_main.getFullData();
        tmp += v_ema.getFullData();
        tmp += t_ema.getFullData();
        tmp += n_iter.getFullData();
        tmp += l_endsection.getFullData();
        tmp += q_sectiontimer.getFullData();
        tmp += q_endtimer.getFullData();
        tmp += q_dangerpoint.getFullData();
        tmp += q_overlap.getFullData();

        LOG.debug("Hex Data: " + ArithmeticalFunctions.bin2Hex(tmp));

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
        sb.append(v_main.getSimpleView());
        sb.append(v_ema.getSimpleView());
        sb.append(t_ema.getSimpleView());
        sb.append(n_iter.getSimpleView());
        sb.append(l_endsection.getSimpleView());
        sb.append(q_sectiontimer.getSimpleView());
        sb.append(q_endtimer.getSimpleView());
        sb.append(q_dangerpoint.getSimpleView());
        sb.append(q_overlap.getSimpleView());

        String simpleView = sb.toString();
        
        return simpleView;
    }

    @Override
    public String toString() {
        
        return new HTMLTagGenerator()
                .startTag()
                .bold(getClass().getSimpleName())
                .cursive("Level 1 MA")
                .underline("[X.Y]")
                .endTag()
                .getString();
    }

    /**
     * A custom XY renderer for vectorized line drawing.
     */
    private class XYVectorizedRenderer extends XYLineAndShapeRenderer {
        private static final long serialVersionUID = 1L;

        /**
         * The length of the base.
         */
        private double baseLength = 0.10;

        /**
         * The length of the head.
         */
        private double headLength = 0.14;

        public XYVectorizedRenderer() {
            super();
            
            setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6, 6));
        }

        @Override
        public boolean getDrawSeriesLineAsPath() {
            return false;
        }

        @Override
        public void drawItem(Graphics2D g2,
                             XYItemRendererState state,
                             Rectangle2D dataArea,
                             PlotRenderingInfo info,
                             XYPlot plot,
                             ValueAxis domainAxis,
                             ValueAxis rangeAxis,
                             XYDataset dataset,
                             int series,
                             int item,
                             CrosshairState crosshairState,
                             int pass) {

            // do nothing if item is not visible
            if (!getItemVisible(series, item)) {
                return;
            }

            // first pass draws the background (lines, for instance)
            if (isLinePass(pass)) {
                if (getItemLineVisible(series, item)) {
                    drawPrimaryLine(state, g2, plot, dataset, pass,
                            series, item, domainAxis, rangeAxis, dataArea);
                }
            }
            // second pass adds shapes where the items are
            else if (isItemPass(pass)) {
                EntityCollection entities = null;
                if (info != null && info.getOwner() != null) {
                    entities = info.getOwner().getEntityCollection();
                }
                drawSecondaryPass(g2, plot, dataset, pass, series, item,
                        domainAxis, dataArea, rangeAxis, crosshairState, entities);
            }
        }

        @Override
        protected void drawPrimaryLine(XYItemRendererState state,
                                       Graphics2D g2,
                                       XYPlot plot,
                                       XYDataset dataset,
                                       int pass,
                                       int series,
                                       int item,
                                       ValueAxis domainAxis,
                                       ValueAxis rangeAxis,
                                       Rectangle2D dataArea) {
            if (item == 0) {
                return;
            }
            
            drawItemVector(g2, dataArea, plot, domainAxis, rangeAxis, dataset, series, item);
        }

        public void drawItemVector(Graphics2D g2,
                                   Rectangle2D dataArea,
                                   XYPlot plot,
                                   ValueAxis domainAxis,
                                   ValueAxis rangeAxis,
                                   XYDataset dataset,
                                   int series,
                                   int item) {

            // get the data points
            double x1 = dataset.getXValue(series, item);
            double y1 = dataset.getYValue(series, item);
            if (Double.isNaN(y1) || Double.isNaN(x1)) {
                return;
            }

            double x0 = dataset.getXValue(series, item - 1);
            double y0 = dataset.getYValue(series, item - 1);
            if (Double.isNaN(y0) || Double.isNaN(x0)) {
                return;
            }

            

            RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
            RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

            double xx0 = domainAxis.valueToJava2D(x0, dataArea, xAxisLocation);
            double yy0 = rangeAxis.valueToJava2D(y0, dataArea, yAxisLocation);

            double xx1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
            double yy1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

            // Draw the line between points
            Line2D line;
            PlotOrientation orientation = plot.getOrientation();
            if (orientation.equals(PlotOrientation.HORIZONTAL)) {
                line = new Line2D.Double(yy0, xx0, yy1, xx1);
            } else {
                line = new Line2D.Double(xx0, yy0, xx1, yy1);
            }

            g2.setPaint(getItemPaint(series, item));
            g2.setStroke(getItemStroke(series, item));
            g2.draw(line);

            // Calculate the arrow head
            double dxx = (xx1 - xx0);
            double dyy = (yy1 - yy0);

            double bx = xx0 + (1.0 - this.baseLength) * dxx;
            double by = yy0 + (1.0 - this.baseLength) * dyy;

            double cx = xx0 + (1.0 - this.headLength) * dxx;
            double cy = yy0 + (1.0 - this.headLength) * dyy;

            double angle = 0.0;
            if (dxx != 0.0) {
                angle = Math.PI / 2.0 - Math.atan(dyy / dxx);
            }
            double deltaX = 5.0 * Math.cos(angle);
            double deltaY = 5.0 * Math.sin(angle);

            double leftx = cx + deltaX;
            double lefty = cy - deltaY;
            double rightx = cx - deltaX;
            double righty = cy + deltaY;

            // Draw the arrow
            GeneralPath p = new GeneralPath();
            if (orientation == PlotOrientation.VERTICAL) {
                p.moveTo((float) xx1, (float) yy1);
                p.lineTo((float) rightx, (float) righty);
                p.lineTo((float) bx, (float) by);
                p.lineTo((float) leftx, (float) lefty);
            } else {
                p.moveTo((float) yy1, (float) xx1);
                p.lineTo((float) righty, (float) rightx);
                p.lineTo((float) by, (float) bx);
                p.lineTo((float) lefty, (float) leftx);
            }
            p.closePath();
            g2.draw(p);
        }
    }
}
