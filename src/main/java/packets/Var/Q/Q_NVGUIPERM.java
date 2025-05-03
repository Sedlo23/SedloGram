package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_NVGUIPERM extends Variables {

    public Q_NVGUIPERM() {
        super("Q_NVGUIPERM",
                1,
                "Povolení používat orientační křivku");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Ne");
        s.add("Ano");


        return s;

    }
}
