package packets.Var.NID;

import packets.Var.Variables;

import java.util.ArrayList;

public class NID_TSR extends Variables {

    public NID_TSR() {
        super("NID_TSR",
                8,
                "Identifikační číslo dočasného omezení rychlosti.");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        for (int i = 0; i <= 126; i++)
            s.set(i, i + " Balíza");

        for (int i = 127; i <= 254; i++)
            s.set(i, i + " RBC");


        s.set(255, "255 Neodvolatelné");


        return s;

    }

}


