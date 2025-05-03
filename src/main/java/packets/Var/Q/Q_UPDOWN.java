package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_UPDOWN extends Variables {

    public Q_UPDOWN() {
        super("Q_UPDOWN",
                1,
                "It defines the direction of the information in the balise telegram");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Downlink");
        s.add("Uplink");


        return s;

    }
}