package packets.Var.D;

import packets.Var.Variables;

import java.util.ArrayList;

public class D_CYCLOC extends Variables {

    public D_CYCLOC() {
        super("D_CYCLOC",
                15,
                "Vlak musí hlásit svou polohu každých D_CYCLOC metrů.");
    }


    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(32767, "Nemusí být posilán");

        return s;

    }


}
