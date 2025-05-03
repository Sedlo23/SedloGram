package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class Q_SCALE extends Variables {


    public Q_SCALE(String data) {
        super("Q_SCALE",
                2,
                "Kvalifikátor označující stejné měřítko používané pro popis všech vzdáleností uvnitř paketu, který obsahuje Q_SCALE.");

        setBinValue(data);

    }

    public Q_SCALE() {
        this("0");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        s.add("10cm");
        s.add("1m");
        s.add("10m");
        s.add("NOT_USED");


        return s;

    }


}
