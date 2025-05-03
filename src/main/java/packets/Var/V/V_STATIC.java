package packets.Var.V;

import java.util.ArrayList;

public class V_STATIC extends A_V {

    public V_STATIC() {
        super("V_STATIC",
                7,
                "Basic static speed profile speed after discontinuity (k).");
    }

    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> s = super.getCombo();


        s.set(127, "Profil není nekonečný");


        return s;

    }


}