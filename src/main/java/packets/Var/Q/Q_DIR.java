package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_DIR extends Variables {

    public Q_DIR() {
        super("Q_DIR",
                2,
                "Kvalifikátor pro označení příslušného směru platnosti přenášených dat s odkazem na směrovost skupiny balise, která informace odesílá, nebo na směrovost LRBG v případě informací odesílaných rádiem.");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Reverzní");
        s.add("Nominální");
        s.add("Oba směry");
        s.add("NOT_USED");

        return s;

    }


}
