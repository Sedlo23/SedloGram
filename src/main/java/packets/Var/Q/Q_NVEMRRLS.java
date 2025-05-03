package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_NVEMRRLS extends Variables {

    public Q_NVEMRRLS() {
        super("Q_NVEMRRLS",
                1,
                "Permission to revoke the emergency brake command when the Permitted Speed limit is no longer exceeded or at standstill (for ceiling speed and target speed monitoring).\n" +
                        "This variable is part of the National Values");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("Při zastavení");
        s.add("Při zpomalení");


        return s;

    }
}
