package packets.Var.NID;

import packets.Var.Variables;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class NID_LX extends Variables {

    public NID_LX() {
        super("NID_LX",
                8,
                "Identity number of the Level Crossing.");
    }

    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> s = new ArrayList<>();

        IntStream.range(0, (int) Math.pow(2, getMaxSize())).forEach(n -> s.add(String.valueOf(n)));

        for (int i = 0; i < 127; i++)
            s.set(i, i + " Balíza");

        for (int i = 127; i < 256; i++)
            s.set(i, i + " RBC");

        return s;

    }


}


