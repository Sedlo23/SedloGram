package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_SLEEPSESSION extends Variables {

    public Q_SLEEPSESSION() {
        super("Q_SLEEPSESSION",
                1,
                "Kvalifikátor pro spací palubní zařízení k provedení nebo neprovedení příkazu \"navázání/ukončení relace\"");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Ignorovat");
        s.add("Provedení ");

        return s;

    }
}