package packets.Var.Q;

import packets.Var.Variables;

import java.util.ArrayList;

public class NC_CDDIFF extends Variables {

    boolean prev = true;

    public NC_CDDIFF() {
        super("NC_(CD)DIFF",
                4,
                "It is the “Cant Deficiency” SSP category for which a different value for the static line speed exists.\n" +
                        "Used together with V_DIFF to permit certain trains to go faster or lower than the “international basic static speed” given by V_STATIC.");
    }

    public boolean isPrev() {
        return prev;
    }

    public void setPrev(boolean prev) {
        this.prev = prev;
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = new ArrayList<>();

        if (!prev) {
            s.add("Nák. P");
            s.add("Nák.G");
            s.add("Oso.");
            s.add("NOT_USED");
            s.add("NOT_USED");
            s.add("NOT_USED");
            s.add("NOT_USED");
            s.add("NOT_USED");
            s.add("NOT_USED");
            s.add("NOT_USED");
            s.add("NOT_USED");
            s.add("NOT_USED");
            s.add("NOT_USED");
            s.add("NOT_USED");
            s.add("NOT_USED");
            s.add("NOT_USED");
        } else {
            s.add(" 80mm");
            s.add("100mm");
            s.add("130mm");
            s.add("150mm");
            s.add("165mm");
            s.add("180mm");
            s.add("210mm");
            s.add("225mm");
            s.add("245mm");
            s.add("275mm");
            s.add("300mm");
            s.add("NOT_USED");
            s.add("NOT_USED");
            s.add("NOT_USED");
            s.add("NOT_USED");
            s.add("NOT_USED");
        }

        return s;

    }


}