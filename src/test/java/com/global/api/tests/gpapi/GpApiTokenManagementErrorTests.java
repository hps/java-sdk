package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class GpApiTokenManagementErrorTests extends BaseGpApiTest {

    private static CreditCardData card;
    private static String token;

    public GpApiTokenManagementErrorTests() throws ApiException {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId(APP_ID)
                .setAppKey(APP_KEY);

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");

        token = card.tokenize(GP_API_CONFIG_NAME);

        assertNotNull("Token could not be generated.", token);
    }

    @Test
    public void VerifyTokenizedPaymentMethod_WithMalformedId() {
        String token = "This_is_not_a_payment_id";

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        try {
            tokenizedCard
                    .verify()
                    .withCurrency("USD")
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40213", ex.getResponseText());
            assertEquals("Status Code: 400 - payment_method.id: " + token + " contains unexpected data", ex.getMessage());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void VerifyTokenizedPaymentMethod_WithMissingCardNumber() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");

        try {
            card
                    .tokenize(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
            assertEquals("Status Code: 400 - Request expects the following fields : number", ex.getMessage());
        }
    }

    @Test
    public void VerifyTokenizedPaymentMethod_WithRandomId() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("PMT_" + UUID.randomUUID());

        try {
            tokenizedCard
                    .verify()
                    .withCurrency("USD")
                    .execute(GP_API_CONFIG_NAME);

        } catch (GatewayException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40116", ex.getResponseText());
            assertEquals("Status Code: 404 - payment_method " + tokenizedCard.getToken() + " not found at this location.", ex.getMessage());
        }
    }

    @Test
    public void UpdateTokenizedPaymentMethod_WithMalformedId() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("This_is_not_a_payment_id");
        tokenizedCard.setExpMonth(12);
        tokenizedCard.setExpYear(2030);

        try {
            tokenizedCard.updateTokenExpiry(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40213", ex.getResponseText());
            assertTrue(ex.getMessage().startsWith("Status Code: 400 - payment_method.id: This_is_not_a_payment_id contains unexpected data"));
        }
    }

    @Test
    public void UpdateTokenizedPaymentMethod_WithRandomId() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("PMT_" + UUID.randomUUID());
        tokenizedCard.setExpMonth(12);
        tokenizedCard.setExpYear(2030);

        try {
            tokenizedCard.updateTokenExpiry(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40116", ex.getResponseText());
            assertTrue(ex.getMessage().startsWith("Status Code: 404 - payment_method"));
        }

    }

    @Ignore
    // Credentials on this test have not permissions to delete a tokenized card
    // Test passed using secret credentials with permissions to delete a tokenized card
    @Test
    public void DeleteTokenizedPaymentMethod_WithNonExistingId() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(card.tokenize(GP_API_CONFIG_NAME));

        assertNotNull(tokenizedCard.getToken());
        assertTrue(tokenizedCard.deleteToken(GP_API_CONFIG_NAME));
        assertFalse(tokenizedCard.deleteToken(GP_API_CONFIG_NAME));

        try {
            tokenizedCard
                    .verify()
                    .withCurrency("USD")
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40116", ex.getResponseText());
            assertTrue(ex.getMessage().startsWith("Status Code: 404 - payment_method"));
        }
    }

    @Ignore
    // Credentials on this test have not permissions to delete a tokenized card
    // Test passed using secret credentials with permissions to delete a tokenized card
    @Test
    public void DeleteTokenizedPaymentMethod_WithRandomId() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("PMT_" + UUID.randomUUID());

        assertFalse(tokenizedCard.deleteToken(GP_API_CONFIG_NAME));

        try {
            tokenizedCard
                    .verify()
                    .withCurrency("USD")
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40116", ex.getResponseText());
            assertTrue(ex.getMessage().startsWith("Status Code: 404 - payment_method"));
        }
    }

    @Ignore
    // Credentials on this test have not permissions to delete a tokenized card
    // Test passed using secret credentials with permissions to delete a tokenized card
    @Test
    public void DeleteTokenizedPaymentMethod_WithMalformedId() throws ApiException {
        String token = "This_is_not_a_payment_id";

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        assertFalse(tokenizedCard.deleteToken(GP_API_CONFIG_NAME));

        try {
            tokenizedCard
                    .verify()
                    .withCurrency("USD")
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40213", ex.getResponseText());
            assertEquals("Status Code: 400 - payment_method.id: " + token + " contains unexpected data", ex.getMessage());
        }
    }

}
