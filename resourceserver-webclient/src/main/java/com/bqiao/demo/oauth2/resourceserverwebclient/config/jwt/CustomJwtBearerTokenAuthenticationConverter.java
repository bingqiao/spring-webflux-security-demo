package com.bqiao.demo.oauth2.resourceserverwebclient.config.jwt;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

import java.util.Collection;
import java.util.Map;

/**
 * Refer to {@link org.springframework.security.oauth2.server.resource.authentication.JwtBearerTokenAuthenticationConverter}
 * this step happens after decode see {@link org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager#authenticate(org.springframework.security.core.Authentication)}
 */
public class CustomJwtBearerTokenAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final CustomJwtGrantedAuthoritiesConverter converter = new CustomJwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt());
        Map<String, Object> attributes = jwt.getClaims();

        Collection<GrantedAuthority> authorities = this.converter.convert(jwt);

        OAuth2AuthenticatedPrincipal principal = new DefaultOAuth2AuthenticatedPrincipal(attributes, authorities);
        return new BearerTokenAuthentication(principal, accessToken, authorities);
    }
}
