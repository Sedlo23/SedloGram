package packets.Var.L;

import packets.Var.Variables;

import java.util.ArrayList;

public class L_NVKRINT extends Variables {

    public L_NVKRINT() {
        super("L_NVKRINT",
                5,
                "Krok délky vlaku používaný k definování integrovaného korekčního faktoru Kr");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(0, "0 m");
        s.set(1, "25 m");
        s.set(2, "50 m");
        s.set(3, "75 m");
        s.set(4, "100 m");
        s.set(5, "150 m");
        s.set(6, "200 m");
        s.set(7, "300 m");

        int x = 400;
        for (int i = 8; i <= 31; i++) {
            s.set(i, x + " m");

            x += 100;
        }

        return s;

    }

}
