package com.global.api.entities.gpApi;

import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.DisputeDocument;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.gateways.GpApiConnector;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.ITokenizable;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class GpApiManagementRequestBuilder {

    public static GpApiRequest buildRequest(ManagementBuilder builder, GpApiConnector gateway) throws GatewayException {
        JsonDoc data = new JsonDoc();

        TransactionType builderTransactionType = builder.getTransactionType();
        IPaymentMethod builderPaymentMethod = builder.getPaymentMethod();

        if (builderTransactionType == TransactionType.Capture) {
            data.set("amount", StringUtils.toNumeric(builder.getAmount()));
            data.set("gratuity_amount", StringUtils.toNumeric(builder.getGratuity()));

            return
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Post)
                            .setEndpoint("/transactions/" + builder.getTransactionId() + "/capture")
                            .setRequestBody(data.toString());

        }
        else if (builderTransactionType == TransactionType.Refund) {
            data.set("amount", StringUtils.toNumeric(builder.getAmount()));

            return
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Post)
                            .setEndpoint("/transactions/" + builder.getTransactionId() + "/refund")
                            .setRequestBody(data.toString());

        }
        else if (builderTransactionType == TransactionType.Reversal) {
            data.set("amount", StringUtils.toNumeric(builder.getAmount()));

            return
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Post)
                            .setEndpoint("/transactions/" + builder.getTransactionId() + "/reversal")
                            .setRequestBody(data.toString());

        }
        else if (builderTransactionType == TransactionType.TokenUpdate && builderPaymentMethod instanceof CreditCardData) {
            CreditCardData cardData = (CreditCardData) builderPaymentMethod;

            JsonDoc card =
                    new JsonDoc()
                            .set("expiry_month", cardData.getExpMonth() != null ? StringUtils.padLeft(cardData.getExpMonth().toString(), 2, '0') : "")
                            .set("expiry_year", cardData.getExpYear() != null ? StringUtils.padLeft(cardData.getExpYear().toString(), 4, '0').substring(2, 4) : "");

            data =
                    new JsonDoc()
                            .set("card", card);

            return
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Patch)
                            .setEndpoint("/payment-methods/" + ((ITokenizable) builderPaymentMethod).getToken())
                            .setRequestBody(data.toString());
        }
        else if (builderTransactionType == TransactionType.TokenDelete && builderPaymentMethod instanceof ITokenizable) {
            return
                    new GpApiRequest()
                        .setVerb(GpApiRequest.HttpMethod.Delete)
                        .setEndpoint("/payment-methods/" + ((ITokenizable) builderPaymentMethod).getToken());
        }
        else if (builderTransactionType == TransactionType.DisputeAcceptance) {
            return
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Post)
                            .setEndpoint("/disputes/" + builder.getDisputeId() + "/acceptance");
        }
        else if (builderTransactionType == TransactionType.DisputeChallenge) {
            JsonArray documentsJsonArray = new JsonArray();
            for(DisputeDocument document : builder.getDisputeDocuments()) {
                JsonObject innerJsonDoc = new JsonObject();

                if(document.getType() != null ) {
                    innerJsonDoc.add("type", new JsonPrimitive(document.getType()));
                }

                if (document.getBase64Content() != null) {
                    innerJsonDoc.add("b64_content", new JsonPrimitive(document.getBase64Content()));
                }

                documentsJsonArray.add(innerJsonDoc);
            }

            JsonObject disputeChallengeData = new JsonObject();
            disputeChallengeData.add("documents", documentsJsonArray);

            return
                    new GpApiRequest()
                            .setVerb(GpApiRequest.HttpMethod.Post)
                            .setEndpoint("/disputes/" + builder.getDisputeId() + "/challenge")
                            .setRequestBody(disputeChallengeData.toString());
        }
        else if (builderTransactionType == TransactionType.BatchClose) {
            return new GpApiRequest()
                    .setVerb(GpApiRequest.HttpMethod.Post)
                    .setEndpoint("/batches/" + builder.getBatchReference());
        }
        else if (builderTransactionType == TransactionType.Reauth) {
            data = new JsonDoc()
                            .set("amount", builder.getAmount());

            return new GpApiRequest()
                    .setVerb(GpApiRequest.HttpMethod.Post)
                    .setEndpoint("/transactions/" + builder.getTransactionId() + "/reauthorization")
                    .setRequestBody(data.toString());
        }
        return null;
    }
}
