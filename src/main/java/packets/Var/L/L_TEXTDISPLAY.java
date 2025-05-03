package packets.Var.L;

import packets.Var.Variables;

import java.util.ArrayList;

public class L_TEXTDISPLAY extends Variables {

    public L_TEXTDISPLAY() {
        super("L_TEXTDISPLAY",
                15,
                "Délka, na které se text zobrazí");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(32767, "Není omezeno");

        return s;

    }

}
