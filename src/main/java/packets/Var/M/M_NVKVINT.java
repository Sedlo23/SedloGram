package packets.Var.M;

import packets.Var.Variables;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class M_NVKVINT extends Variables {

    public M_NVKVINT() {
        super("M_NVKVINT",
                7,
                "Jedná se o integrovaný korekční faktor závislý na rychlosti.\n" +
                        "M_NVKVINT(n) platí pro odhadovanou rychlost mezi V_NVKV INT(n) a V_NVKVINT(n+1).\n" +
                        "M_NVKVINT platí mezi 0 km/h a V_NVKVINT(1)\n" +
                        "Tato proměnná je součástí národních hodnot\n");
    }

    @Override
    public ArrayList<String> getCombo() {

        DecimalFormat df = new DecimalFormat("0.00");

        ArrayList<String> s = super.getCombo();

        int i = 0;

        for (i = 0; i < Math.pow(2, getMaxSize()); i++)
            s.set(i, String.valueOf(df.format(i * 0.02)));


        return s;

    }


}