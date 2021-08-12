package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.GpApiService;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static com.global.api.entities.enums.IntervalToExpire.FIVE_MINUTES;
import static com.global.api.entities.enums.IntervalToExpire.THREE_HOURS;
import static org.junit.Assert.*;

public class GpApiAuthenticationTests extends BaseGpApiTest {

    final CreditCardData card = new CreditCardData();

    @Before
    public void Initialize() {
        card.setNumber("4263970000005262");
        card.setExpMonth(05);
        card.setExpYear(2025);
        card.setCvn("852");
    }

    private GpApiConfig configAccessTokenCall() {
        GpApiConfig config =
                new GpApiConfig()
                        .setAppId(APP_ID)
                        .setAppKey(APP_KEY);

        config.setEnableLogging(true);

        return config;
    }

    @Test
    public void GenerateAccessTokenManual() throws GatewayException {
        GpApiConfig config = configAccessTokenCall();

        AccessTokenInfo accessTokenInfo = GpApiService.generateTransactionKey(config);

        assertAccessTokenResponse(accessTokenInfo);
    }

    @Test
    public void GenerateAccessTokenManualWithPermissions() throws GatewayException {
        String[] permissions = new String[]{"PMT_POST_Create", "PMT_POST_Detokenize"};

        GpApiConfig config =
                configAccessTokenCall()
                        .setPermissions(permissions);

        AccessTokenInfo info =
                GpApiService
                        .generateTransactionKey(config);

        assertNotNull(info);
        assertNotNull(info.getToken());
        assertEquals("tokenization", info.getTokenizationAccountName());
        assertNull(info.getDataAccountName());
        assertNull(info.getDisputeManagementAccountName());
        assertNull(info.getTransactionProcessingAccountName());
    }

    @Test
    public void GenerateAccessTokenManualWithWrongPermissions() {
        String[] permissions = new String[]{"TEST_1", "TEST_2"};

        GpApiConfig config =
                configAccessTokenCall()
                        .setPermissions(permissions);

        boolean exceptionCaught = false;
        try {
            GpApiService
                    .generateTransactionKey(config);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40119", ex.getResponseText());
            assertEquals("Status Code: 400 - Invalid permissions [ TEST_1,TEST_2 ] provided in the input field - permissions", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void GenerateAccessTokenManualWithSecondsToExpire() throws ApiException {
        GpApiConfig gpApiConfig = configAccessTokenCall()
                .setSecondsToExpire(60);

        AccessTokenInfo accessTokenInfo = GpApiService.generateTransactionKey(gpApiConfig);

        assertAccessTokenResponse(accessTokenInfo);

        ServicesContainer.configureService(gpApiConfig, GP_API_CONFIG_NAME);

        Transaction response =
                card
                        .verify()
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
    }

    @Test
    public void GenerateAccessTokenManualWithIntervalToExpire() throws ApiException {
        GpApiConfig gpApiConfig =
                configAccessTokenCall()
                        .setIntervalToExpire(THREE_HOURS);

        AccessTokenInfo accessTokenInfo = GpApiService.generateTransactionKey(gpApiConfig);

        assertAccessTokenResponse(accessTokenInfo);

        ServicesContainer.configureService(gpApiConfig, GP_API_CONFIG_NAME);

        Transaction response =
                card
                        .verify()
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
    }

    @Test
    public void GenerateAccessTokenManualWithSecondsToExpireAndIntervalToExpire() throws ApiException {
        GpApiConfig gpApiConfig =
                configAccessTokenCall()
                        .setSecondsToExpire(60)
                        .setIntervalToExpire(FIVE_MINUTES);

        AccessTokenInfo accessTokenInfo = GpApiService.generateTransactionKey(gpApiConfig);

        assertAccessTokenResponse(accessTokenInfo);

        ServicesContainer.configureService(gpApiConfig, GP_API_CONFIG_NAME);

        Transaction response =
                card
                        .verify()
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
    }

    @Test
    public void GenerateAccessToken_WrongAppId() {
        try {
            GpApiConfig config = configAccessTokenCall().setAppId(APP_ID + "a");
            GpApiService.generateTransactionKey(config);
        } catch (GatewayException ex) {
            assertEquals("40004", ex.getResponseText());
            assertEquals("ACTION_NOT_AUTHORIZED", ex.getResponseCode());
            assertEquals("Status Code: 403 - App credentials not recognized", ex.getMessage());
        }
    }

    @Test
    public void GenerateAccessToken_WrongAppKey() {
        try {
            GpApiConfig config = configAccessTokenCall().setAppKey(APP_KEY + "a");
            GpApiService.generateTransactionKey(config);
        } catch (GatewayException ex) {
            assertEquals("40004", ex.getResponseText());
            assertEquals("ACTION_NOT_AUTHORIZED", ex.getResponseCode());
            assertEquals("Status Code: 403 - Credentials not recognized to create access token.", ex.getMessage());
        }
    }

    @Test
    public void UseInvalidAccessTokenInfo() throws ApiException {
        GpApiConfig gpApiConfig = configAccessTokenCall();

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setToken("INVALID_Token_w23e9sd93w3d");
        accessTokenInfo.setDataAccountName("dataAccount");
        accessTokenInfo.setDisputeManagementAccountName("disputeAccount");
        accessTokenInfo.setTokenizationAccountName("tokenizationAccount");
        accessTokenInfo.setTransactionProcessingAccountName("transactionAccount");

        gpApiConfig.setAccessTokenInfo(accessTokenInfo);

        ServicesContainer.configureService(gpApiConfig, GP_API_CONFIG_NAME);

        try {
            card
                    .verify()
                    .withCurrency("USD")
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("NOT_AUTHENTICATED", ex.getResponseCode());
            assertEquals("40001", ex.getResponseText());
            assertEquals("Status Code: Unauthorized - Invalid access token", ex.getMessage());
        }
    }

    @Test
    public void UseExpiredAccessTokenInfo() throws ApiException {

        GpApiConfig gpApiConfig = configAccessTokenCall();

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setToken("r1SzGAx2K9z5FNiMHkrapfRh8BC8");
        accessTokenInfo.setDataAccountName("Settlement Reporting");
        accessTokenInfo.setDisputeManagementAccountName("Dispute Management");
        accessTokenInfo.setTokenizationAccountName("Tokenization");
        accessTokenInfo.setTransactionProcessingAccountName("Transaction_Processing");

        gpApiConfig.setAccessTokenInfo(accessTokenInfo);

        ServicesContainer.configureService(gpApiConfig, GP_API_CONFIG_NAME);

        try {
            card
                    .verify()
                    .withCurrency("USD")
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("NOT_AUTHENTICATED", ex.getResponseCode());
            assertEquals("40001", ex.getResponseText());
            assertEquals("Status Code: Unauthorized - Invalid access token", ex.getMessage());
        }
    }

    @Test
    public void ChargeCardWithAccessTokenWithLimitedPermissions() throws ApiException {
        String[] permissions = new String[]{"TRN_POST_Capture"};

        GpApiConfig gpApiConfig =
                configAccessTokenCall()
                    .setPermissions(permissions);

        AccessTokenInfo accessTokenInfo = GpApiService.generateTransactionKey(gpApiConfig);

        assertNotNull(accessTokenInfo);
        assertNotNull(accessTokenInfo.getToken());

        ServicesContainer.configureService(gpApiConfig, "GpApiConfig_2");

        boolean exceptionCaught = false;
        try {
            card
                    .charge(new BigDecimal("1"))
                    .withCurrency("USD")
                    .execute("GpApiConfig_2");
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40212", ex.getResponseText());
            assertEquals("ACTION_NOT_AUTHORIZED", ex.getResponseCode());
            assertEquals("Status Code: 403 - Permission not enabled to execute action", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    private void assertAccessTokenResponse(AccessTokenInfo accessTokenInfo) {
        assertNotNull(accessTokenInfo);
        assertNotNull(accessTokenInfo.getToken());
        assertEquals("transaction_processing", accessTokenInfo.getTransactionProcessingAccountName());
        assertEquals("settlement_reporting", accessTokenInfo.getDataAccountName());
        assertEquals("dispute_management", accessTokenInfo.getDisputeManagementAccountName());
        assertEquals("tokenization", accessTokenInfo.getTokenizationAccountName());
    }

}