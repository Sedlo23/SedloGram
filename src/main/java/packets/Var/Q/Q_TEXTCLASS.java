package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_TEXTCLASS extends Variables {

    public Q_TEXTCLASS() {
        super("Q_TEXTCLASS",
                2,
                "Q_TEXTCLASS určuje třídu textové zprávy obsažené ve stejném paketu (prostá nebo pevná zpráva) ");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();
        s.add("Doplňkové informace");
        s.add("Důležité informace");
        s.add("NOT_USED");
        s.add("NOT_USED");

        return s;

    }
}