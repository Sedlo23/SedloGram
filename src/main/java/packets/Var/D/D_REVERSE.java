package packets.Var.D;

import packets.Var.Variables;

import java.util.ArrayList;

public class D_REVERSE extends Variables {

    public D_REVERSE() {
        super("D_REVERSE",
                15,
                "Vzdálenost od referenčního místa ke koncovému místu vzdálenosti pro běh v režimu RV");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(32767, "\uF0A5");


        return s;

    }


}
