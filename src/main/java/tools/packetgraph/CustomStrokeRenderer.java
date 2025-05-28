package tools.packetgraph;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import packets.TrackToTrain.P27;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * A custom stroke renderer for lines in the XY plot.
 */
public class CustomStrokeRenderer extends XYLineAndShapeRenderer {
    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state,
                         Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
                         ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
                         int series, int item, CrosshairState crosshairState, int pass) {

        // Get the data points
        double x2 = dataset.getXValue(series, item);

        // Check if this point is at MaxDistanceETCS
        if (x2 == P27.MaxDistanceETCS) {
            // Draw a shorter line
            if (item > 0) {
                double x1 = dataset.getXValue(series, item - 1);
                double y1 = dataset.getYValue(series, item - 1);
                double y2 = dataset.getYValue(series, item);

                // Convert data values to Java2D coordinates
                double xx1 = domainAxis.valueToJava2D(x1, dataArea, plot.getDomainAxisEdge());
                double yy1 = rangeAxis.valueToJava2D(y1, dataArea, plot.getRangeAxisEdge());

                // Calculate a shorter endpoint (e.g., 20% of the way)
                double shortenedX = x1 + (x2 - x1) * 0.2;
                double xx2 = domainAxis.valueToJava2D(shortenedX, dataArea, plot.getDomainAxisEdge());
                double yy2 = rangeAxis.valueToJava2D(y2, dataArea, plot.getRangeAxisEdge());

                // Draw the shortened line
                g2.setPaint(getItemPaint(series, item));
                g2.setStroke(getItemStroke(series, item));
                g2.draw(new Line2D.Double(xx1, yy1, xx2, yy2));

                // Draw arrow at the end
                double arrowLength = 10.0;
                double angle = Math.atan2(yy2 - yy1, xx2 - xx1);

                // Arrow head points
                double x3 = xx2 - arrowLength * Math.cos(angle - Math.PI / 6);
                double y3 = yy2 - arrowLength * Math.sin(angle - Math.PI / 6);
                double x4 = xx2 - arrowLength * Math.cos(angle + Math.PI / 6);
                double y4 = yy2 - arrowLength * Math.sin(angle + Math.PI / 6);

                // Draw arrow
                Path2D.Double arrow = new Path2D.Double();
                arrow.moveTo(xx2, yy2);
                arrow.lineTo(x3, y3);
                arrow.lineTo(x4, y4);
                arrow.closePath();
                g2.fill(arrow);

                // Add a "∞" label nearby
                String infinityLabel = "∞";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(infinityLabel, (float) (xx2 + 5), (float) yy2);

                return; // Skip the standard drawing
            }
        }

        // Standard drawing for non-MaxDistanceETCS points
        super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis,
                dataset, series, item, crosshairState, pass);

        if (item == 0) {
            return;
        }

        double x1 = dataset.getXValue(series, item - 1);
        double y1 = dataset.getYValue(series, item - 1);
        double y2 = dataset.getYValue(series, item);

        // If line is horizontal, place the label
        if (y1 == y2) {
            double xx1 = domainAxis.valueToJava2D(x1, dataArea, plot.getDomainAxisEdge());
            double yy1 = rangeAxis.valueToJava2D(y1, dataArea, plot.getRangeAxisEdge());
            double xx2 = domainAxis.valueToJava2D(x2, dataArea, plot.getDomainAxisEdge());
            double yy2 = rangeAxis.valueToJava2D(y2, dataArea, plot.getRangeAxisEdge());

            double mx = (xx1 + xx2) / 2;
            double my = (yy1 + yy2) / 2;

            // Draw series name
            String seriesName = (String) dataset.getSeriesKey(series);
            g2.setFont(g2.getFont().deriveFont(9f)); // small label
            FontMetrics fm = g2.getFontMetrics();
            int width = fm.stringWidth(seriesName);
            int height = fm.getAscent();

            if (isSeriesVisible(series)) {
                // White rectangle behind text
                g2.setPaint(Color.WHITE);
                g2.fillRect((int) (mx - width / 2) - 2, (int) (my - height / 2 - 2 - 8), width + 4, height + 4);

                g2.setPaint(Color.BLACK);
                g2.drawString(seriesName, (int) (mx - width / 2), (int) (my + height / 2) - 8);
            }
        }
    }

    @Override
    public Stroke getItemStroke(int row, int col) {
        if (col > 0) {
            double x1 = getPlot().getDataset().getXValue(row, col - 1);
            double y1 = getPlot().getDataset().getYValue(row, col - 1);
            double x2 = getPlot().getDataset().getXValue(row, col);
            double y2 = getPlot().getDataset().getYValue(row, col);

            // If it's not purely horizontal or vertical => dashed line
            if (x1 != x2 && y1 != y2) {
                return new BasicStroke(
                        1.0f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND,
                        1.0f,
                        new float[]{6.0f, 6.0f},
                        0.0f
                );
            }
        }
        return new BasicStroke(2f);
    }
}
