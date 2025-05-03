package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_CONFTEXTDISPLAY extends Variables {

    public Q_CONFTEXTDISPLAY() {
        super("Q_CONFTEXTDISPLAY",
                1,
                "Udává vztah mezi událostí \"potvrzení řidiče\" a seznamem událostí \"umístění\", \"čas\", \"režim\", \"úroveň\", které definují koncovou podmínku pro zobrazení textu");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Vždy ukončí zobrazení");
        s.add("Je další podmínkou");


        return s;

    }
}