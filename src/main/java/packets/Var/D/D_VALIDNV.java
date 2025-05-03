package packets.Var.D;

import packets.Var.Variables;

import java.util.ArrayList;

public class D_VALIDNV extends Variables {
    public D_VALIDNV() {
        super("D_VALIDNV",
                15,
                "Vzdálenost od počátku platnosti národních hodnot");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(32767, "Nyní");


        return s;

    }
}
