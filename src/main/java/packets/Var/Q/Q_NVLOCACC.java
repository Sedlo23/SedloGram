package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class Q_NVLOCACC extends Variables {

    public Q_NVLOCACC() {
        super("Q_NVLOCACC",
                6,
                "Default accuracy of the balise location (absolute value)");
    }

    public ArrayList<String> getCombo() {
        ArrayList<String> s = new ArrayList<>();

        IntStream.range(0, (int) Math.pow(2, getMaxSize())).forEach(n -> s.add("+- " + String.valueOf(n)));

        return s;

    }
}
