package packets.Var.D;

import packets.Var.Variables;

import java.util.ArrayList;

public class D_TEXTDISPLAY extends Variables {

    public D_TEXTDISPLAY() {
        super("D_TEXTDISPLAY",
                15,
                "Vzdálenost od místa, kde má být text zobrazen");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(32767, "Není omezeno");


        return s;

    }


}
