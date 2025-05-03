package packets.Var.M;

import packets.Var.Variables;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class M_NVKRINT extends Variables {

    public M_NVKRINT() {
        super("M_NVKRINT",
                5,
                "Jedná se o integrovaný korekční faktor závislý na délce vlaku.\n" +
                        "M_NVKRINT(l) platí pro délku vlaku mezi L_NVKRINT(l) a L_NVKRINT(l+1).\n" +
                        "M_NVKRINT platí v rozmezí 0 m až L_NVKRINT(1).");
    }

    @Override
    public ArrayList<String> getCombo() {

        DecimalFormat df = new DecimalFormat("0.00");

        ArrayList<String> s = super.getCombo();

        int i = 0;

        for (i = 0; i < Math.pow(2, getMaxSize()); i++)
            s.set(i, String.valueOf(df.format(i * 0.05)));


        return s;

    }
}
