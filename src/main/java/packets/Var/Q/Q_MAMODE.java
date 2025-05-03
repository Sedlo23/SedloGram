package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_MAMODE extends Variables {

    public Q_MAMODE() {
        super("Q_MAMODE",
                1,
                "Tento kvalifikátor určuje, zda se za SvL považuje začátek profilu režimu, nebo zda se SvL odvozuje od MA.");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("odvodit SvL z MA");
        s.add("začátek profilu");

        return s;

    }
}