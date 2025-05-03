package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_FRONT extends Variables {

    public Q_FRONT() {
        super("Q_FRONT",
                1,
                "Kvalifikátor určující, zda se má omezení rychlosti dané pro prvek profilu použít, dokud přední část vlaku (bez zpoždění délky vlaku) nebo konec vlaku (zpoždění délky vlaku) neopustí prvek.");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Konec");
        s.add("Čelo");

        return s;

    }

    @Override
    public Variables deepCopy() {
        Q_FRONT tmp = new Q_FRONT();

        tmp.setBinValue(getBinValue());

        return tmp;
    }
}