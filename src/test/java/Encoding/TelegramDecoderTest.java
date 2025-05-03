package Encoding;

import org.junit.Test;

import static org.junit.Assert.*;

public class TelegramDecoderTest {


    @Test
    public void decode_long()
    {
        for (String[] temp:TestValues.telegrams_long)
        {
            assertEquals(temp[0].replace(" ",""),TelegramDecoder.decodeTelegram(temp[1].replace(" ","")));
        }

    }

    @Test
    public void decode_short()
    {

        for (String[] temp:TestValues.telegrams_short)
        {
            assertEquals(temp[0].replace(" ",""),TelegramDecoder.decodeTelegram(temp[1].replace(" ","")));
        }


    }







}