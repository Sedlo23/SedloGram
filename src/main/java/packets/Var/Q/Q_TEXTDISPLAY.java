package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_TEXTDISPLAY extends Variables {

    public Q_TEXTDISPLAY() {
        super("Q_TEXTDISPLAY",
                1,
                "Q_TEXTDISPLAY definuje, zda mají být eventdílčí podmínky začátku a konce s  pro textovou zprávu kombinovány nebo ne ");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();
        s.add("Právě jedna");
        s.add("Všechny");

        return s;

    }

}