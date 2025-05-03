package packets.Var.T;

import packets.Var.Variables;

public class T_ENDTIMER extends Variables {
    public T_ENDTIMER() {
        super("T_ENDTIMER",
                10,
                "Time for which the End section is valid measured from the moment the train reaches the location defined by D_ENDTIMERSTARTLOC.");
    }
}
