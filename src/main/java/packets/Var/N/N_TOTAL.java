package packets.Var.N;

import packets.Var.Variables;

import java.util.ArrayList;

public class N_TOTAL extends Variables {

    public N_TOTAL() {
        super("N_TOTAL",
                3,
                "Total number of balise(s) in the group");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(0, "1x BG");
        s.set(1, "2x BG");
        s.set(2, "3x BG");
        s.set(3, "4x BG");
        s.set(4, "5x BG");
        s.set(5, "6x BG");
        s.set(6, "7x BG");
        s.set(7, "8x BG");


        return s;

    }
}