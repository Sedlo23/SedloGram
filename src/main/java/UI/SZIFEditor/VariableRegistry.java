package UI.SZIFEditor;

import packets.Var.M.M_DUP;
import packets.Var.M.M_MCOUNT;
import packets.Var.M.M_VERSION;
import packets.Var.N.N_PIG;
import packets.Var.N.N_TOTAL;
import packets.Var.NID.NID_BG;
import packets.Var.NID.NID_C;
import packets.Var.Q.Q_LINK;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of variable values for standard variables
 */
class VariableRegistry {
    private static final Map<String, List<String>> VARIABLE_VALUES = new HashMap<>();

    static {
        // Initialize standard variable values
        List<String> mVersionValues = new M_VERSION().getCombo();
        VARIABLE_VALUES.put("M_VERSION", mVersionValues);

        List<String> nPigValues = new N_PIG().getCombo();

        VARIABLE_VALUES.put("N_PIG", nPigValues);

        List<String> nTotalValues = new N_TOTAL().getCombo();

        VARIABLE_VALUES.put("N_TOTAL", nTotalValues);

        List<String> mDupValues = new M_DUP().getCombo();

        VARIABLE_VALUES.put("M_DUP", mDupValues);

        List<String> mMcountValues = new M_MCOUNT().getCombo();

        VARIABLE_VALUES.put("M_MCOUNT", mMcountValues);

        List<String> nidCValues = new NID_C().getCombo();

        VARIABLE_VALUES.put("NID_C", nidCValues);

        List<String> nidBgValues = new NID_BG().getCombo();

        VARIABLE_VALUES.put("NID_BG", nidBgValues);

        List<String> qLinkValues = new Q_LINK().getCombo();

        VARIABLE_VALUES.put("Q_LINK", qLinkValues);
    }

    public static List<String> getValuesForVariable(String variableName) {
        return VARIABLE_VALUES.get(variableName);
    }

    public static void registerVariable(String name, List<String> values) {
        VARIABLE_VALUES.put(name, values);
    }
}
