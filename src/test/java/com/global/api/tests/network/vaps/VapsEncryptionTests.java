package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.payroll.PayrollEncoder;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import com.global.api.utils.MessageReader;
import com.global.api.utils.MessageWriter;
import org.apache.commons.codec.binary.Base64;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VapsEncryptionTests {
    private CreditCardData card;
    private CreditCardData cardWithCvn;
    private CreditTrackData track;
    private DebitTrackData debit;

    public VapsEncryptionTests() throws ApiException {
        Address address = new Address();
        address.setName("My STORE");
        address.setStreetAddress1("1 MY STREET");
        address.setCity("MYTOWN");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactEmv_ContactlessMsd_MagStripe_KeyEntry);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportedEncryptionType(EncryptionType.TEP2);

        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        ServicesContainer.configureService(config);

        // AMEX
        card = TestCards.AmexManualEncrypted();
        cardWithCvn = TestCards.AmexManualEncrypted();
        cardWithCvn.setCvn("9072488");
        track = TestCards.AmexSwipeEncrypted();

        // DISCOVER
//        card = TestCards.DiscoverManualEncrypted();
//        cardWithCvn = TestCards.DiscoverManualEncrypted();
//        cardWithCvn.setCvn("7803754");
//        cashCard = TestCards.DiscoverSwipeEncryptedV2();

        // MASTERCARD
//        card = TestCards.MasterCardManualEncrypted();
//        cardWithCvn = TestCards.MasterCardManualEncrypted();
//        cardWithCvn.setCvn("7803754");
//        cashCard = TestCards.MasterCardSwipeEncryptedV2();

        // VISA
//        card = TestCards.VisaManualEncrypted();
//        cardWithCvn = TestCards.VisaManualEncrypted();
//        cardWithCvn.setCvn("7803754");
//        cashCard = TestCards.VisaSwipeEncryptedV2();

        // DEBIT
        debit = new DebitTrackData();
        debit.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4gcOTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0m+/d4SO9TEshhRGUUQzVBrBvP/Os1qFx+6zdQp1ejjUCoDmzoUMbil9UG73zBxxTOy25f3Px0p8joyCh8PEWhADz1BkROJT3q6JnocQE49yYBHuFK0obm5kqUcYPfTY09vPOpmN+wp45gJY9PhkJF5XvPsMlcxX4/JhtCshegz4AYrcU/sFnI+nDwhy295BdOkVN1rn00jwCbRcE900kj3UsFfyc", "2"));
    }

    @Test
    public void test_000_batch_close() throws ApiException {
        BatchSummary summary = BatchService.closeBatch();
        assertNotNull(summary);
        assertTrue(summary.isBalanced());
    }

    @Test
    public void test_001_credit_manual_auth_cvn() throws ApiException {
        Transaction response = cardWithCvn.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_credit_manual_auth() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_003_credit_manual_sale_cvn() throws ApiException {
        Transaction response = cardWithCvn.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #7
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_004_credit_manual_sale() throws ApiException {
        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #8
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_005_credit_manual_refund_cvn() throws ApiException {
        Transaction response = cardWithCvn.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_006_credit_manual_refund() throws ApiException {
        Transaction response = card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_034_credit_swipe_auth() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_036_credit_swipe_sale() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // reverse the transaction test case #40
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());

        // reverse the transaction test case #39
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_038_credit_swipe_refund() throws ApiException {
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_000_encryption_base64() {
        // initial value
        String initialString = "This is the initial value";

        // convert to byte array through MessageWriter class
        MessageWriter mw = new MessageWriter();
        mw.addRange(initialString.getBytes());
        byte[] initialBuffer = mw.toArray();

        // base64 encode the buffer & convert to encoded string
        byte[] encodedBuffer = Base64.encodeBase64(initialBuffer);
        String encodedString = new String(encodedBuffer);

        // encrypt the encoded string
        PayrollEncoder encoder = new PayrollEncoder("username", "apikey");
        String encryptedString = encoder.encode(encodedString);

        // add the STX/ETX and LRC
        mw = new MessageWriter();
        mw.add(ControlCodes.STX);
        mw.addRange(encryptedString.getBytes());
        mw.add(ControlCodes.ETX);
        mw.add(TerminalUtilities.calculateLRC(mw.toArray()));

        // final string
        String framedString = new String(mw.toArray());

        // validate
        assertTrue(TerminalUtilities.checkLRC(framedString));

        // check the outcome
        MessageReader mr = new MessageReader(framedString.getBytes());
        ControlCodes stx = mr.readCode();
        assertEquals(ControlCodes.STX, stx);

        String decryptedCheckString = mr.readToCode(ControlCodes.ETX, false);

        ControlCodes etx = mr.readCode();
        assertEquals(ControlCodes.ETX, etx);

        // decrypt the encrypted string
        String decryptedString = encoder.decode(decryptedCheckString);

        // decode the decrypted string
        byte[] decodedBuffer = Base64.decodeBase64(decryptedString);
        String decodedString = new String(decodedBuffer);

        // compare the initial and end values
        assertEquals(initialString, decodedString);
    }

//    @Test
//    public void test_000_token_test() throws ApiException {
//        String terminalString = "000%s9911";
//
//        int storeNumber = 736716;
//        while(storeNumber++ < 999999) {
//            if(storeNumber % 1000 == 0) {
//                System.out.println(storeNumber);
//            }
//
//            String terminalId = String.format(terminalString, storeNumber);
//            PayrollEncoder encoder = new PayrollEncoder("0044", terminalId);
//
//            String framedString = "bE1KW+xLqbgPdVMyoZ+Fa94zGXBYywYOiAxFyIK5SrBdjt3DQcxthFfOnRN2eZMCJik3p9D18SBTywBdqa3CBIv8lYhKt+iJ9D3ywuHc4z1GPEG8krq8BVO5aOpgXPXQwcfIpE4QRA7/c3LUm5hNTYYrFh/v7DKmAeorB+mH79aQv0sfEMHpJfsnaSRFwI+v2l7xuBo2oIDGyzSNRYju8idZA0WNmLs8A/lAP3sQp34Xr+2ILH5ayzrZ93Z9ie5gNN6oYTHmbXQ6nlr92VUOO7I8cWDQv+dSTOAtXcKjIkztOvpWAl2Rvvs7ponw2pCCgCzXc3OaOcj8F35pkuXvTA+2SuMu1ztabkGCyhzG12X0crRMpFEgeG+EhHaCT66+NVwrgLcbvXHi6vaCUZT2pEjJ6K0aGWErE3Fo1qrBqE3WF8ltRqX9HmM9D9pUn0x5nZlDzVRT7iO77r9+WnaJ5h3ONJFk6OUlVL1sY0NNp8VZY1J3hJPwR/+SUxfBR+vVsXFZt+IXoZxkIalLnBLufv/10sprQRboGsYia/VMQC72lp7ODHmWntnYqza22uRuW1ymjd377IZ/T7vH6pzLjUkVx/SfFy37icoXA4QlTaI=";
//            //String framedString = "9b4sO7nSybc2q3aCq6jtCtvd9d0Nc7jZsIsk2Y63COIXVvY6q1ZG4ADjCxN7+mL1PdZMGntFwN5XRu16tKM0tmU4Xu2AU8EEdF8Br1Lp6sIFXCBcG7mU7c5ee/H8L4PY/EpmvIyzyZvNm2VoRi114qiGY32l7G0SnOZ7GfUz0nTVg8k8Rawcix33/5K+6Tb2R96TwD3G/yxhSpKnEB7ypIH/+P0wq5ANpTcdnMo0meBuIqNPfbmGgwKVrsHKygEjE73LnognfxYc6tOMDzsik+UQJMI/jXN3xCB1IpSg9S3ZoQN+gc46wuoXzKY/5IsiudwDn7+OatuVPb9OnNUaCN2HCRQRwhUnyzN7clcm6nL30ZTWtydNGH37pDuHegadhYpL8CyzYlAm/alZloHY/wVe2NCfKgnmTuABGLfpg6TMJUa4hwRc4iFnGyVW/Yi25oFMVYlX70BRLTJFKxMVY28hoyoWD2eG2UjoNxqufdY4SjeKgj6waarnAkWO8q9iB6Uncu4CVOzEYBsjbUchTg==";
//            MessageReader mr = new MessageReader(framedString.getBytes());
//
//            String decryptedCheckString = mr.readToCode(ControlCodes.ETX, false);
//
//            // decrypt the encrypted string
//            String decryptedString = encoder.decode(decryptedCheckString);
//
//            // decode the decrypted string
//            byte[] decodedBuffer = Base64.decodeBase64(decryptedString);
//            String decodedString = new String(decodedBuffer);
//            if(decodedString.startsWith("1220")) {
//                System.out.print(storeNumber);
//                assertTrue(decodedString.startsWith("1220"));
//                break;
//            }
//        }
//    }
}
