package packets.Var.M;

import packets.Var.Variables;

import java.util.ArrayList;

public class M_NVCONTACT extends Variables {

    public M_NVCONTACT() {
        super("M_NVCONTACT",
                2,
                "Označuje reakci, která se provede po uplynutí časovače T_NVCONTACT.");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(0, "Trip");
        s.set(1, "Provozní brzda");
        s.set(2, "Bez reakce");
        s.set(3, "NOT_USED");

        return s;

    }
}
