package packets.Var.M;

import packets.Var.Variables;

import java.util.ArrayList;

public class M_DUP extends Variables {

    public M_DUP() {
        super("M_DUP",
                2,
                "Příznaky určující, zda je balíza duplikátem jedné ze sousedních balíz.");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(0, "Samostatná");
        s.set(1, "> Kopie následující");
        s.set(2, "< Kopie předcházející");
        s.set(3, "NOT_USED");

        return s;

    }
}