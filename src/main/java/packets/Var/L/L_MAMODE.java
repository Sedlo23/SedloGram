package packets.Var.L;

import packets.Var.Variables;

import java.util.ArrayList;

public class L_MAMODE extends Variables {

    public L_MAMODE() {
        super("L_MAMODE",
                15,
                "Délka oblasti požadovaného režimu");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(32767, "∞");

        return s;

    }

}
