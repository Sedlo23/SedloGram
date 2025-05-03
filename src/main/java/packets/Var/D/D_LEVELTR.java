package packets.Var.D;

import packets.Var.Variables;

import java.util.ArrayList;

public class D_LEVELTR extends Variables {

    public D_LEVELTR() {
        super("D_LEVELTR",
                15,
                "Vzdálenost k přechodu úrovní");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(32767, "Nyní");


        return s;

    }


}
