package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_LINK extends Variables {

    public Q_LINK() {
        super("Q_LINK",
                1,
                "Tento kvalifikátor se používá k označení BG jako linkované");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Nelinkovaná");
        s.add("Linkovaná");

        return s;

    }
}