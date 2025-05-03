package packets.Var.M;

import packets.Var.Variables;

import java.util.ArrayList;

public class M_MCOUNT extends Variables {

    public M_MCOUNT() {
        super("M_MCOUNT",
                8,
                "Účelem tohoto čítače je umožnit palubnímu systému ERTMS/ETCS zjistit, ke které skupině vyvážení telegram patří.");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();


        s.set(254, "Nikdy");
        s.set(255, "Vždy");


        return s;

    }
}