package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_MPOSITION extends Variables {

    public Q_MPOSITION() {
        super("Q_MPOSITION",
                1,
                "Kvalifikátor pro označení směru počítání kilometru zeměpisné polohy ve vztahu ke směru referenční skupiny zeměpisné polohy.");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Opačný směr");
        s.add("Stejné směr");

        return s;

    }
}