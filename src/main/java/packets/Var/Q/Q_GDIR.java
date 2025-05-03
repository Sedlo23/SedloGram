package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_GDIR extends Variables {

    public Q_GDIR() {
        super("Q_GDIR",
                1,
                "Qualifier for gradient slope.");
    }


    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Klesání");
        s.add("Stoupaní");

        return s;

    }
}
