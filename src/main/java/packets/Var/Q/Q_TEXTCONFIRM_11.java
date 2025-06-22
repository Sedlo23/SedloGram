package packets.Var.Q;

import packets.Var.Variables;
import tools.string.StringHelper;

import java.util.ArrayList;

public class Q_TEXTCONFIRM_11 extends Variables {

    public Q_TEXTCONFIRM_11() {
        super("Q_TEXTCONFIRM",
                2,
                "Kvalifikacer ");
    }

    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> s = new ArrayList<>();
        s.add("Není vyžadováno");
        s.add("Je vyžadováno");
        s.add("Je a provozní brzda");
        s.add("Je a Nouzová przda");
        return s;
    }

    @Override
    public Variables deepCopy() {
        Q_TEXTCONFIRM_11 tmp = new Q_TEXTCONFIRM_11();
        tmp.setBinValue(getBinValue());
        return tmp;
    }

    @Override
    public Variables initValueSet(String[] s) {
        setBinValue(StringHelper.TrimAR(s, getMaxSize()));
        return this;
    }

    @Override
    public String getFullData() {
        String tmp = getBinValue();
        return tmp;
    }

    @Override
    public String getSimpleView() {
        String tmp = super.getSimpleView();
        return tmp;
    }
}