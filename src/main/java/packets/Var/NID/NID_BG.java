package packets.Var.NID;

import packets.Var.Variables;

import java.util.ArrayList;

public class NID_BG extends Variables {

    public NID_BG() {
        super("NID_BG",
                14,
                "Identifikační číslo balizové skupiny v rámci země nebo regionu definovaného pomocí NID_C");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(16383, "Neznámá");


        return s;

    }
}


