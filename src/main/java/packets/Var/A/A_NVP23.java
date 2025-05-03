package packets.Var.A;

import packets.Var.Variables;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class A_NVP23 extends Variables {

    public A_NVP23() {
        super("A_NVP23",
                6,
                "Upper deceleration limit to determine the set of correction factor Kv to be used for Conventional Passenger trains.\n" +
                        "This variable is part of the National Values.");
    }

    @Override
    public ArrayList<String> getCombo() {
        DecimalFormat df = new DecimalFormat("0.00");

        ArrayList<String> s = new ArrayList<>();

        int i = 0;
        for (i = 0; i < 64; i++)
            s.add(String.valueOf(df.format(i * 0.05)) + " m/s2");


        return s;

    }
}
