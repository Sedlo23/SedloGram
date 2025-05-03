package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_ASPECT extends Variables {

    public Q_ASPECT() {
        super("Q_ASPECT",
                1,
                "Aspect of “danger for shunting” signal");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Stůj v SH");
        s.add("Volno v SH");


        return s;

    }
}