package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.TransactionStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.EBTCardData;
import com.global.api.paymentMethods.EBTTrackData;
import com.global.api.serviceConfigs.GpApiConfig;
import lombok.var;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GpApiEbtTest extends BaseGpApiTest {
    EBTCardData ebtCardData;
    EBTTrackData ebtTrackData;

    private final String CURRENCY = "USD";
    private final BigDecimal AMOUNT = new BigDecimal(10);

    public GpApiEbtTest() throws ApiException {

        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId(APP_ID)
                .setAppKey(APP_KEY)
                .setChannel(Channel.CardPresent.getValue());

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);
    }

    @Before
    public void testInitialize() {
        ebtCardData = new EBTCardData();
        ebtCardData.setNumber("4012002000060016");
        ebtCardData.setExpMonth(12);
        ebtCardData.setExpYear(2025);
        ebtCardData.setPinBlock("32539F50C245A6A93D123412324000AA");
        ebtCardData.setCardHolderName("Jane Doe");

        ebtTrackData = new EBTTrackData();
        ebtTrackData.setValue("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        ebtTrackData.setEntryMethod(EntryMethod.Swipe);
        ebtTrackData.setPinBlock("32539F50C245A6A93D123412324000AA");
        ebtTrackData.setCardHolderName("Jane Doe");
    }

    @Test
    public void EbtSale_CardData() throws ApiException {
        var response =
                ebtCardData
                        .charge(AMOUNT)
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertEbtResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void EbtSale_TrackData() throws ApiException {
        var response =
                ebtTrackData
                        .charge(AMOUNT)
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertEbtResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void EbtSaleRefund_CardData() throws ApiException {
        var response =
                ebtCardData
                        .refund(AMOUNT)
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertEbtResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void EbtSaleRefund_TrackData() throws ApiException {
        var response =
                ebtTrackData
                        .refund(AMOUNT)
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertEbtResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void EbtTransaction_Refund_TrackData() throws ApiException {
        var transaction =
                ebtTrackData
                        .charge(AMOUNT)
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertEbtResponse(transaction, TransactionStatus.Captured);

        var response =
                transaction
                        .refund()
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertEbtResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void EbtTransaction_Refund_CreditData() throws ApiException {
        var transaction =
                ebtCardData
                        .charge(AMOUNT)
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertEbtResponse(transaction, TransactionStatus.Captured);

        var response =
                transaction
                        .refund()
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertEbtResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void EbtTransaction_Reverse_TrackData() throws ApiException {
        var transaction =
                ebtTrackData
                        .charge(AMOUNT)
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertEbtResponse(transaction, TransactionStatus.Captured);

        var response =
                transaction
                        .reverse()
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertEbtResponse(response, TransactionStatus.Reversed);
    }

    @Test
    public void EbtTransaction_Reverse_CreditData() throws ApiException {
        var transaction =
                ebtCardData
                        .charge(AMOUNT)
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertEbtResponse(transaction, TransactionStatus.Captured);

        var response =
                transaction
                        .reverse()
                        .withCurrency(CURRENCY)
                        .execute(GP_API_CONFIG_NAME);

        assertEbtResponse(response, TransactionStatus.Reversed);
    }

    private void assertEbtResponse(Transaction response, TransactionStatus transactionStatus) {
        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(transactionStatus.getValue(), response.getResponseMessage());
    }

}