package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_NVINHSMICPERM extends Variables {

    public Q_NVINHSMICPERM() {
        super("Q_NVINHSMICPERM",
                1,
                "Kvalifikátor, který brání kompenzaci nepřesnosti měření rychlosti pro výpočet limitů dohledu souvisejících s EBI.");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Ne");
        s.add("Ano");


        return s;

    }
}
