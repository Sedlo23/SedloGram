package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_SRSTOP extends Variables {

    public Q_SRSTOP() {
        super("Q_SRSTOP",
                1,
                "Určuje, zda se palubní zařízení odpovědného personálu musí zastavit nebo ne");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Zastavit v SR");
        s.add("Jet v SR");

        return s;

    }
}
