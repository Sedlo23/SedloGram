package Encoding;

import org.junit.Test;
import tools.crypto.ArithmeticalFunctions;

import static org.junit.Assert.*;

public class TelegramEncoderTest {

    @Test
    public void encode_short()
    {
        for (String[] temp:TestValues.telegrams_short)
        {
            assertEquals(
                           (temp[1].replace(" ","")), TelegramEncoder.encode(
                            ArithmeticalFunctions.hex2Bin( temp[0].replace(" ",""))));
        }
    }

    @Test
    public void encode_long()
    {
        for (String[] temp:TestValues.telegrams_long)
        {
            assertEquals(( temp[1].replace(" ","")),
                    TelegramEncoder.encode(
                            ArithmeticalFunctions.hex2Bin( temp[0].replace(" ",""))));
        }
    }




}