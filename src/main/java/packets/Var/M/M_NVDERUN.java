package packets.Var.M;

import packets.Var.Variables;

import java.util.ArrayList;

public class M_NVDERUN extends Variables {

    public M_NVDERUN() {
        super("M_NVDERUN",
                1,
                "Zadání ID za jízdy");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(0, "Ne");
        s.set(1, "Ano");


        return s;

    }
}
