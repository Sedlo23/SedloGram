package packets.Var.D;

import packets.Var.Variables;

import java.util.ArrayList;

public class D_LRBG extends Variables {

    public D_LRBG() {
        super("D_LRBG",
                15,
                "Vzdálenost mezi poslední referenčí BG a předpokládaným čelem vlaku (strana aktivní kabiny).");
    }


    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(32767, "Unknown");


        return s;

    }


}
