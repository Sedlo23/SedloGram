package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_MEDIA extends Variables {

    public Q_MEDIA() {
        super("Q_MEDIA",
                1,
                "Indicates whether it is a balise telegram or a loop message");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Balíza");
        s.add("Euroloop");

        return s;

    }
}