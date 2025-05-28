package tools.packetgraph;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

// Custom axis that only shows used values
public class CustomNumberAxis extends NumberAxis {
    private Set<Double> valueSet = new TreeSet<>();

    public CustomNumberAxis(String label) {
        super(label);
        setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    }

    public void setValues(Set<Double> values) {
        this.valueSet = values;
        setAutoTickUnitSelection(false);
    }

    @Override
    protected List refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
        List ticks = new ArrayList();

        for (Double value : valueSet) {
            if (value <= getRange().getUpperBound() && value >= getRange().getLowerBound()) {
                ticks.add(new NumberTick(value, value.intValue() + "", TextAnchor.TOP_CENTER,
                        TextAnchor.CENTER, 0.0));
            }
        }

        return ticks;
    }

    @Override
    protected List refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
        List ticks = new ArrayList();

        for (Double value : valueSet) {
            if (value <= getRange().getUpperBound() && value >= getRange().getLowerBound()) {
                ticks.add(new NumberTick(value, value.intValue() + "", TextAnchor.CENTER_RIGHT,
                        TextAnchor.CENTER, 0.0));
            }
        }

        return ticks;
    }
}
