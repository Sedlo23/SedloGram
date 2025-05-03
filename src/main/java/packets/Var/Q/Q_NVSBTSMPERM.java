package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_NVSBTSMPERM extends Variables {

    public Q_NVSBTSMPERM() {
        super("Q_NVSBTSMPERM",
                1,
                "Povolení používat provozní brzdu při sledování cílové rychlosti");
    }


    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Ne");
        s.add("Ano");

        return s;

    }
}
