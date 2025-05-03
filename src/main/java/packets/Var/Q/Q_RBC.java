package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_RBC extends Variables {

    public Q_RBC() {
        super("Q_RBC",
                1,
                "Kvalifikátor pro pořadí komunikační relace");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Ukončení");
        s.add("Navázání");


        return s;

    }
}