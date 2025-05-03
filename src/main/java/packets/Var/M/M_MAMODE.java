package packets.Var.M;

import packets.Var.Variables;

import java.util.ArrayList;

public class M_MAMODE extends Variables {

    public M_MAMODE() {
        super("M_MAMODE",
                2,
                "Požadovaný režim pro část MA");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(0, "On Sight");
        s.set(1, "Posun");
        s.set(2, "Limited Supervision");
        s.set(3, "NOT_USED");


        return s;

    }
}
