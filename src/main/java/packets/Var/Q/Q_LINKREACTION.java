package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_LINKREACTION extends Variables {

    public Q_LINKREACTION() {
        super("Q_LINKREACTION",
                2,
                "Kvalifikátor pro reakci, která se provede, pokud dojde k problému s linkováním nebo konzistencí zprávy BG");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();
        s.add("Trip");
        s.add("Provozní brzdu");
        s.add("Bez reakce");
        s.add("NOT_USED");


        return s;

    }

}
