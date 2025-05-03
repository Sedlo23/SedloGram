package packets.Var.NID;

import packets.Var.Variables;
import tools.ui.GUIHelper;
import tools.ui.IconListRenderer;
import tools.ui.InputJCombobox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static tools.ui.GUIHelper.addLabel;

public class NID_C extends Variables {


    public NID_C() {
        super("NID_C",
                10,
                "Kód používaný k identifikaci země nebo regionu, ve kterém se nachází skupina balíků, RBC nebo RIU. Nemusí se nutně řídit administrativními nebo politickými hranicemi.");
        setBinValue("1");

    }


    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> s = new ArrayList<>();

        IntStream.range(0, (int) Math.pow(2, getMaxSize())).forEach(n -> s.add("NOT_USED"));

        for (int i = 1; i <= 50; i++)
            s.set(i, i + " ENG");

        for (int i = 51; i <= 53; i++)
            s.set(i, i + " POR");

        for (int i = 56; i <= 63; i++)
            s.set(i, i + " LUX");

        for (int i = 64; i <= 127; i++)
            s.set(i, i + " GER");

        for (int i = 128; i <= 191; i++)
            s.set(i, i + " FRA");

        for (int i = 251; i <= 255; i++)
            s.set(i, i + " BEL");

        for (int i = 256; i <= 290; i++)
            s.set(i, i + " ITA");

        for (int i = 322; i <= 335; i++)
            s.set(i, i + " FIN");

        for (int i = 339; i <= 343; i++)
            s.set(i, i + " POL");

        for (int i = 344; i <= 350; i++)
            s.set(i, i + " DEN");

        for (int i = 352; i <= 383; i++)
            s.set(i, i + " SPA");

        for (int i = 384; i <= 390; i++)
            s.set(i, i + " AUS");

        for (int i = 951; i <= 960; i++)
            s.set(i, i + " POL");

        for (int i = 1022; i <= 1023; i++)
            s.set(i, i + " IRE");

        s.set(513, "513 CZE");
        s.set(514, "514 CZE");
        s.set(515, "515 CZE");
        s.set(516, "516 CZE");
        s.set(517, "517 CZE");
        s.set(518, "518 CZE");
        s.set(519, "519 CZE");

        return s;
    }

    Component component;

    @Override
    public Component getComponent(String comment) {
        component = super.getComponent(comment);
        jComboBoxRR.setEditable(false);

        ((JPanel) component).setLayout(new BoxLayout(((JPanel) component), BoxLayout.X_AXIS));

        component = (JPanel) addLabel(jComboBoxRR, "NID_C", getDescription(), new JLabel());

        new InputJCombobox(jComboBoxRR);

        return component;
    }

}