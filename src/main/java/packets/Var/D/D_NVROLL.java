package packets.Var.D;

import packets.Var.Variables;

import java.util.ArrayList;

public class D_NVROLL extends Variables {
    public D_NVROLL() {
        super("D_NVROLL",
                15,
                "Tato proměnná je součástí národních hodnot a používá se pro ochranu proti odjezdu a ochranu proti zpětnému pohybu. V rámci (národních/výchozích) limitů D_NVROLL může být vlak posunut pro odpojení.");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(32767, "∞");


        return s;

    }

}
