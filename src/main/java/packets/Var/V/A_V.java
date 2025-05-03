package packets.Var.V;

import packets.Var.Variables;

import java.util.ArrayList;

public abstract class A_V extends Variables {

    public A_V(String name, int maxSize, String description) {
        super(name, maxSize, description);
    }


    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        int i = 0;
        for (i = 0; i < 121; i++)
            s.add(String.valueOf(i * 5) + " km/h");

        for (int x = i; x < Math.pow(2, getMaxSize()); x++)
            s.add("NOT_USED");

        return s;

    }
}
