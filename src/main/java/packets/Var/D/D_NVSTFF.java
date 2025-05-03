package packets.Var.D;

import packets.Var.Variables;

import java.util.ArrayList;

public class D_NVSTFF extends Variables {

    public D_NVSTFF() {
        super("D_NVSTFF",
                15,
                "Maximum distance for running in Staff Responsible mode");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(32767, "∞");


        return s;

    }

}
