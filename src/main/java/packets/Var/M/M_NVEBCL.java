package packets.Var.M;

import packets.Var.Variables;

import java.util.ArrayList;

public class M_NVEBCL extends Variables {

    public M_NVEBCL() {
        super("M_NVEBCL",
                4,
                "Na základě požadované úrovně spolehlivosti zvolí palubní zařízení odpovídající korekční faktor kolejových vozidel Kdry_rst(V).\n" +
                        "Úroveň spolehlivosti bezpečného zpomalení při nouzovém brzdění představuje pravděpodobnost následující jednotlivé události: subsystém nouzového brzdění kolejových vozidel vlaku zajistí zpomalení, které se rovná alespoň A_brake_emergency(V) * Kdry_rst(V), když je nouzová brzda spuštěna na suchých kolejích.");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(0, "50 %");
        s.set(1, "90 %");
        s.set(2, "99 %");
        s.set(3, "99.9 %");
        s.set(4, "99.99%");
        s.set(5, "99.999 %");
        s.set(6, "99.9999 %");
        s.set(7, "99.99999 %");
        s.set(8, "99.999999 %");
        s.set(9, "99.9999999 %");


        return s;

    }
}
