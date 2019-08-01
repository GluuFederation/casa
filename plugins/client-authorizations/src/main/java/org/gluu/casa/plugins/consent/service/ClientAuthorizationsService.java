package org.gluu.casa.plugins.consent.service;

import com.unboundid.ldap.sdk.Filter;
import org.gluu.casa.plugins.consent.ldap.Client;
import org.gluu.casa.plugins.consent.ldap.ClientAuthorization;
import org.gluu.casa.plugins.consent.ldap.Scope;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.consent.ldap.Token;
import org.gluu.casa.service.ILdapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles client authorizations for a user
 * @author jgomer
 */
public class ClientAuthorizationsService {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private ILdapService ldapService;

    public ClientAuthorizationsService() {
        ldapService = Utils.managedBean(ILdapService.class);
    }

    public Map<Client, Set<Scope>> getUserClientPermissions(String userId) {

        List<ClientAuthorization> authorizations = ldapService.find(ClientAuthorization.class, authorizationDNOf(userId), null);

        //Obtain client ids from all this user's client authorizations
        Set<String> clientIds = authorizations.stream().map(ClientAuthorization::getOxAuthClientId).collect(Collectors.toSet());

        //Create a filter based on client Ids, alternatively one can make n queries to obtain client references one by one
        Filter[] filters = clientIds.stream().map(id -> Filter.createEqualityFilter("inum", id))
                .collect(Collectors.toList()).toArray(new Filter[]{});
        List<Client> clients = ldapService.find(Client.class, ldapService.getClientsDn(), Filter.createORFilter(filters));

        //Obtain all scope ids ... displayNames :(
        Set<String> scopeNames = authorizations.stream().map(ClientAuthorization::getOxAuthScopeAsList).flatMap(List::stream)
                .collect(Collectors.toSet());

        //Do the analog for scopes
        filters = scopeNames.stream().map(id -> Filter.createEqualityFilter("displayName", id))
                .collect(Collectors.toList()).toArray(new Filter[]{});
        List<Scope> scopes = ldapService.find(Scope.class, ldapService.getScopesDn(), Filter.createORFilter(filters));

        logger.info("Found {} client authorizations for user {}", clients.size(), userId);
        Map<Client, Set<Scope>> perms = new HashMap<>();

        for (Client client : clients) {
            Set<Scope> clientScopes = new HashSet<>();

            for (ClientAuthorization auth : authorizations) {
                if (auth.getOxAuthClientId().equals(client.getInum())) {
                    for (String scopeName : auth.getOxAuthScopeAsList()) {
                        scopes.stream().filter(sc -> sc.getDisplayName().equals(scopeName)).findAny().ifPresent(clientScopes::add);
                    }
                }
            }
            perms.put(client, clientScopes);
        }

        return perms;

    }

    public void removeClientAuthorizations(String userId, String userName, String clientId) {

        ClientAuthorization sampleAuth = new ClientAuthorization();
        sampleAuth.setOxAuthClientId(clientId);

        logger.info("Removing client authorizations for user {}", userName);
        //Here we ignore the return value of deletion
        ldapService.find(sampleAuth, ClientAuthorization.class, authorizationDNOf(userId))
                .forEach(auth -> ldapService.delete(auth, ClientAuthorization.class));

        Token sampleToken = new Token();
        sampleToken.setOxAuthClientId(clientId);
        sampleToken.setOxAuthTokenType("refresh_token");
        sampleToken.setOxAuthUserId(userName);

        logger.info("Removing refresh tokens associated to this user/client pair");
        //Here we ignore the return value of deletion
        ldapService.find(sampleToken, Token.class, ldapService.getClientsDn())
                .forEach(token -> {
                    logger.debug("Deleting token {}", token.getUniqueIdentifier());
                    ldapService.delete(token, Token.class);
                });

    }

    private String authorizationDNOf(String userId) {
        return "ou=clientAuthorizations," + ldapService.getPersonDn(userId);
    }

}
