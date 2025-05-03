package packets.Var.M;

import packets.Var.Variables;

import java.util.ArrayList;

public class M_VERSION extends Variables {

    public M_VERSION() {
        super("M_VERSION",
                7,
                "Tím se získá verze systému ETCS.\n" +
                        "Každá část označuje první, resp. druhé číslo verze.\n" +
                        "- První číslo odlišuje nekompatibilní verze. (Tři MSB)\n" +
                        "- Druhé číslo označuje kompatibilitu v rámci verze X. (čtyři LSB)");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        for (int i = 0;i<s.size();i++)
        {
            s.set(i, "NOT_USED");
        }

        s.set(16, "[1.0]");
        s.set(17, "[1.1]");
        s.set(33, "[2.1]");
        s.set(32, "[2.0]");

        return s;

    }



}