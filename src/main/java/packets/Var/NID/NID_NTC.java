package packets.Var.NID;

import packets.Var.Variables;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class NID_NTC extends Variables {

    public NID_NTC() {
        super("NID_NTC",
                8,
                "Každá hodnota této proměnné představuje identitu národního systému.");
    }

    @Override
    public ArrayList<String> getCombo() {
        ArrayList<String> s = new ArrayList<>();

        IntStream.range(0, (int) Math.pow(2, getMaxSize())).forEach(n -> s.add(String.valueOf("NOT_USED")));


        s.set(0, "Spain ASFA");
        s.set(1, "Netherlands ATB");
        s.set(2, "Spain ASFA AVE");
        s.set(3, "Spain LZB Spain");
        s.set(5, "Belgium TBL 1 ");
        s.set(6, "Germany, Austria PZB 90");
        s.set(7, "Belgium TBL 2/3");
        s.set(8, "France KVB");
        s.set(9, "Germany,Austria, Israel PZB/LZB ");
        s.set(10, "Spain LZB");
        s.set(11, "Italy SCMT");
        s.set(12, "Luxembourg MEMOR II+");
        s.set(14, "TVM");
        s.set(15, "Italy BACC");
        s.set(16, "Italy RSDD");
        s.set(17, "Hungary EVM ");
        s.set(18, "Belgium Crocodile");
        s.set(19, "Spain EBICAB 900 TBS");
        s.set(20, "UK TPWS/AWS");
        s.set(21, "UK TPWS/AWS (SA)");
        s.set(22, "Norway, Sweden ATC2");
        s.set(23, "Finland EBICAB 900");
        s.set(24, "Poland EBICAB 900 (PL)");
        s.set(25, "Korea KNR ATS ");
        s.set(26, "Poland SHP");
        s.set(27, "Croatia, Slovenia, Serbia INDUSI I 60");
        s.set(28, "Belgium TBL1+");
        s.set(29, "France NExTEO");
        s.set(30, "Denmark ZUB 123");
        s.set(32, "France RPS");
        s.set(33, "LS CZ");
        s.set(34, "Portugal EBICAB 700 (P)");
        s.set(35, "UK SELCAB ");
        s.set(36, "Romania INDUSI I 60 ");
        s.set(37, "UK TBL");
        s.set(39, "Baltic ALSN ");
        s.set(40, "Bulgaria EBICAB ");
        s.set(45, "China CTCS-2");
        s.set(46, "Malaysia EBICAB 700");
        s.set(47, "France KCVP ");
        s.set(50, "UK TGMT");

        s.set(255, "Reserved for multicast");


        return s;

    }

}