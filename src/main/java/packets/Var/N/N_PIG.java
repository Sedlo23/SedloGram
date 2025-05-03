package packets.Var.N;

import packets.Var.Variables;

import java.util.ArrayList;

public class N_PIG extends Variables {

    public N_PIG() {
        super("N_PIG",
                3,
                "Defines the relative position in a balise group");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(0, "1.");
        s.set(1, "2.");
        s.set(2, "3.");
        s.set(3, "4.");
        s.set(4, "5.");
        s.set(5, "6.");
        s.set(6, "7.");
        s.set(7, "8.");


        return s;

    }
}