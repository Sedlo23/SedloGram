package packets.Var.T;

import packets.Var.Variables;

public class T_NVCONTACT extends Variables {

    public T_NVCONTACT() {
        super("T_NVCONTACT",
                8,
                "If no “safe” message has been received from the track for more than T_NVCONTACT seconds, an appropriate action according to M_NVCONTACT must be triggered.\n" +
                        "This variable is part of the National Values");
    }
}
