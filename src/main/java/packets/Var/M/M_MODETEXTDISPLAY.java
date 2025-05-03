package packets.Var.M;

import packets.Var.Variables;

import java.util.ArrayList;

public class M_MODETEXTDISPLAY extends Variables {

    public M_MODETEXTDISPLAY() {
        super("M_MODETEXTDISPLAY",
                4,
                "Text se při zadávání / zobrazuje tak dlouho, dokud je v definovaném režimu.");


    }


    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(0, "Full Supervision");
        s.set(1, "On Sight");
        s.set(2, "Staff Responsible");
        s.set(3, "NOT_USED");
        s.set(4, "Unfitted");
        s.set(5, "Nevyužito");
        s.set(6, "Stand By");
        s.set(7, "Trip");
        s.set(8, "Post Trip");
        s.set(9, "NOT_USED");
        s.set(10, "NOT_USED");
        s.set(11, "NOT_USED");
        s.set(12, "Limited Supervision");
        s.set(13, "NOT_USED");
        s.set(14, "Reversing");
        s.set(15, "Není omezeno");


        return s;

    }


}
