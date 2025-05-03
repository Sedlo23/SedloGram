package packets.Var.M;

import packets.Var.Variables;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class M_NVKTINT extends Variables {

    public M_NVKTINT() {
        super("M_NVKTINT",
                5,
                "Integrovaný faktor correction Kt");
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