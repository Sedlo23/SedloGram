package packets.TrackToTrain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import packets.Var.D.D_TSR;
import packets.Var.L.L_PACKET;
import packets.Var.L.L_TSR;
import packets.Var.NID.NID_PACKET;
import packets.Var.NID.NID_TSR;
import packets.Var.Q.Q_DIR;
import packets.Var.Q.Q_FRONT;
import packets.Var.Q.Q_SCALE;
import packets.Var.V.V_TSR;
import packets.Var.Variables;
import tools.crypto.ArithmeticalFunctions;
import tools.packetgraph.CustomStrokeRenderer;
import tools.ui.GUIHelper;
import tools.string.HTMLTagGenerator;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import static tools.ui.GUIHelper.createModernTheme;
import static tools.ui.GUIHelper.loadAndScaleIcon;

public class P65 extends Packet {

    private static final Logger LOG = LogManager.getLogger(P65.class);

    private final XYSeriesCollection dataset;
    ArrayList<Variables> P3Variables;
    JFreeChart chart;
    ChartPanel chartPanel;

    public P65() {
        this(new String[]{ArithmeticalFunctions.hex2Bin("41808E9E05DC032110")});
    }

    public P65(String[] d) {
        

        P3Variables = new ArrayList<>();

        // Initialize each Variables element with the provided data
        P3Variables.add(new NID_PACKET().initValueSet(d));
        P3Variables.add(new Q_DIR().initValueSet(d));
        P3Variables.add(new L_PACKET().initValueSet(d));
        P3Variables.add(new Q_SCALE().initValueSet(d));
        P3Variables.add(new NID_TSR().initValueSet(d));
        P3Variables.add(new D_TSR().initValueSet(d));
        P3Variables.add(new L_TSR().initValueSet(d));
        P3Variables.add(new Q_FRONT().initValueSet(d));
        P3Variables.add(new V_TSR().initValueSet(d));

        setIcon(loadAndScaleIcon("flags/pac/icons8-restrict-50.png"));

        dataset = new XYSeriesCollection();
        chart = ChartFactory.createXYLineChart(
                "Dočasné omezení rychlosti",
                "Vzdálenost [" + P3Variables.get(3).getCombo().get(P3Variables.get(3).getDecValue()) + "]",
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
        plot.setRenderer(new CustomStrokeRenderer());

        StandardChartTheme theme = createModernTheme();
        theme.apply(chart);

        
    }

    @Override
    public Component getPacketComponent() {
        
        JPanel jPanel = new JPanel(new MigLayout("wrap", "[]10[]10[]", "[]10[]10[]"));

        // Add each variable's component to the panel
        for (Variables va : P3Variables) {
            jPanel.add(va.getComponent());
        }

        // Add action listener to handle user updates in combo boxes
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

        // Indices in P3Variables based on their addition order
        int d_tsrIndex = 5;
        int v_tsrIndex = 8;
        int l_tsrIndex = 6;
        int q_scaleIndex = 3;
        int nid_tsrIndex = 4;

        int x = P3Variables.get(d_tsrIndex).getDecValue();
        int y = P3Variables.get(v_tsrIndex).getDecValue() * 5;

        XYSeries xySeries2 = new XYSeries("NID " + P3Variables.get(nid_tsrIndex).getDecValue());
        dataset.addSeries(xySeries2);

        
        xySeries2.add(x, y);

        x += P3Variables.get(l_tsrIndex).getDecValue();
        xySeries2.add(x, y);

        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis newAxis = new NumberAxis("Vzdálenost [" +
                P3Variables.get(q_scaleIndex).getCombo().get(P3Variables.get(q_scaleIndex).getDecValue()) + "]");
        plot.setDomainAxis(newAxis);

        
    }

    @Override
    public Component getGraphicalVisualization() {
        
        updateG();
        return chartPanel;
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
        

        // Update L_PACKET (index=2) with new length
        P3Variables.get(2).setBinValue(ArithmeticalFunctions.dec2XBin(String.valueOf(firstPassLength), 13));

        // Second pass
        sb.setLength(0);
        for (Variables v : P3Variables) {
            sb.append(v.getFullData());
        }

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
    public String toString() {
        
        return new HTMLTagGenerator().startTag()
                .bold(getClass().getSimpleName())
                .cursive("TSR - nastavení")
                .underline("[X.Y]")
                .endTag()
                .getString();
    }
}
