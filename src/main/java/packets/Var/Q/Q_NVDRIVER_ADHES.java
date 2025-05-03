package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_NVDRIVER_ADHES extends Variables {
    public Q_NVDRIVER_ADHES() {
        super("Q_NVDRIVER_ADHES",
                1,
                "Kvalifikace pro změnu součinitele adheze na trati strojvedoucím ");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();
        s.add("Ne");
        s.add("Ano");


        return s;

    }
}
