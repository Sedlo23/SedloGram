package packets.Var.T;

import packets.Var.Variables;

import java.util.ArrayList;

public class T_TEXTDISPLAY extends Variables {
    public T_TEXTDISPLAY() {
        super("T_TEXTDISPLAY",
                10,
                "Duration for which a text shall be displayed");
    }

    @Override
    public ArrayList<String> getCombo() {

        ArrayList<String> s = super.getCombo();

        s.set(1023, "Display of text not limited by time.");

        return s;

    }


}
