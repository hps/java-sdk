package com.global.api.serviceConfigs;

import com.global.api.ConfiguredServices;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.gateways.*;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
public class GpApiConfig extends GatewayConfig {
    // GP-API app Id
    @Accessors(chain = true)
    private String appId;           // For example: OWTP5ptQZKGj7EnvPt3uqO844XDBt8Oj

    // GP-API app Key
    @Accessors(chain = true)
    private String appKey;          // For example: abcDefgHijkLmn12

    // The time left in seconds before the token expires
    @Accessors(chain = true)
    private int secondsToExpire;

    // The time interval set for when the token will expire
    @Accessors(chain = true)
    private IntervalToExpire intervalToExpire;

    // GP-API channel
    private String channel = Channel.CardNotPresent.getValue();

    // GP-API language
    @Accessors(chain = true)
    private Language language = Language.English;

    // GP-API Country. Two letter ISO 3166 country
    @Accessors(chain = true)
    private String country = "US";

    // The list of the permissions the integrator want the access token to have
    @Accessors(chain = true)
    // public IStringConstant[] permissions;
    public String[] permissions;

    // GP-API Access token information
    private AccessTokenInfo accessTokenInfo;

    // 3DSecure challenge return url
    @Getter @Setter public String challengeNotificationUrl;

    // 3DSecure method return url
    @Getter @Setter public String methodNotificationUrl;

    // 3DSecure merchant contact url
    @Getter @Setter public String merchantContactUrl;

    public void configureContainer(ConfiguredServices services) {
        if (StringUtils.isNullOrEmpty(serviceUrl)) {
            serviceUrl =
                    environment.equals(Environment.TEST) ?
                            ServiceEndpoints.GP_API_TEST.getValue() :
                            ServiceEndpoints.GP_API_PRODUCTION.getValue();
        }

        GpApiConnector gpApiConnector = new GpApiConnector(this);

        gpApiConnector.setServiceUrl(serviceUrl);
        gpApiConnector.setEnableLogging(this.isEnableLogging());

        services.setGatewayConnector(gpApiConnector);

        services.setReportingService(gpApiConnector);

        services.setSecure3dProvider(Secure3dVersion.ONE, gpApiConnector);
        services.setSecure3dProvider(Secure3dVersion.TWO, gpApiConnector);
    }

    @Override
    public void validate() throws ConfigurationException {
        super.validate();

        if ( accessTokenInfo == null && (appId == null || appKey == null))
            throw new ConfigurationException("accessTokenInfo or appId and appKey cannot be null.");
    }

}