package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_LINKORIENTATION extends Variables {

    public Q_LINKORIENTATION() {
        super("Q_LINKORIENTATION",
                1,
                "Uvádí, zda bude propojená balízová skupina předjížděna vlakem v nominálním nebo reverzním směru.");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();
        s.add("Reverzní");
        s.add("Nominální");


        return s;

    }

}
