package org.gluu.casa.plugins.accounts.service;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.accounts.ldap.oxPassportConfiguration;
import org.gluu.casa.plugins.accounts.pojo.Provider;
import org.gluu.casa.plugins.accounts.pojo.ProviderType;
import org.gluu.casa.service.ILdapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdi.model.SimpleCustomProperty;
import org.xdi.model.passport.PassportConfiguration;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author jgomer
 */
public class AvailableProviders {

    private static final Path OXLDAP_PATH = Paths.get("/etc/gluu/conf/ox-ldap.properties");
    private static final String OXPASSPORT_PROPERTY = "oxpassport_ConfigurationEntryDN";

    private static List<Provider> providers;

    private static Logger logger = LoggerFactory.getLogger(AvailableProviders.class);

    //This has to be a codehaus mapper (see PassportConfiguration.class)
    private static ObjectMapper mapper;

    private static ILdapService ldapService;

    static {
        mapper = new ObjectMapper();
        ldapService = Utils.managedBean(ILdapService.class);
        //Lookup the authentication providers supported in the current Passport installation
        providers = retrieveProviders();
    }

    public static List<Provider> get() {
        return get(false);
    }

    public static List<Provider> get(boolean refresh) {
        if (refresh) {
            providers = retrieveProviders();
        }
        return providers;
    }

    public static Optional<Provider> getByName(String name) {
        return providers.stream().filter(p -> p.getName().equals(name)).findFirst();
    }

    private static List<Provider> retrieveProviders() {

        List<Provider> list = new ArrayList<>();
        list.addAll(retrieveSAMLIDPs());
        list.addAll(retrieveSocialProviders());
        return list;

    }

    private static List<Provider> retrieveSAMLIDPs() {

        List<Provider> providers = new ArrayList<>();
        logger.info("Loading IDPs list");
        try {
            logger.debug("Parsing passport-saml-config.json");
            byte[] bytes = Files.readAllBytes(Paths.get("/etc/gluu/conf/passport-saml-config.json"));
            Map<String, Object> data = mapper.readValue(new String(bytes, StandardCharsets.UTF_8), new TypeReference<Map<String, Object>>(){});

            for (String key : data.keySet()) {
                Map<String, Object> props = (Map<String, Object>) data.get(key);

                if (Optional.ofNullable(props.get("enable")).map(val -> Boolean.valueOf(val.toString())).orElse(false)) {
                    logger.info("Found provider {}", key);

                    Provider prv = new Provider();
                    prv.setType(ProviderType.SAML);
                    prv.setName(key);

                    String logo = Optional.ofNullable(props.get("logo_img")).map(Object::toString).orElse(null);
                    if (logo != null) {
                        if (!logo.startsWith("http")) {
                            logo = "/oxauth/auth/passport/" + logo;
                        }
                        prv.setLogo(logo);
                    }
                    providers.add(prv);
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return providers;
    }

    private static List<Provider> retrieveSocialProviders() {

        List<Provider> providers = new ArrayList<>();
        logger.info("Loading social strategies info");
        try {
            logger.debug("Reading DN of LDAP passport configuration");
            String dn = Files.newBufferedReader(OXLDAP_PATH).lines().filter(l -> l.startsWith(OXPASSPORT_PROPERTY))
                    .findFirst().map(l -> l.substring(OXPASSPORT_PROPERTY.length())).get();
            //skip uninteresting chars
            dn = dn.replaceFirst("[\\W]*=[\\W]*","");

            oxPassportConfiguration passportConfig = ldapService.get(oxPassportConfiguration.class, dn);

            if (passportConfig != null) {
                Stream.of(passportConfig.getGluuPassportConfiguration())
                        .forEach(cfg -> {
                            try {
                                PassportConfiguration pcf = mapper.readValue(cfg, PassportConfiguration.class);
                                Provider provider = new Provider();
                                provider.setType(ProviderType.SOCIAL);
                                provider.setName(pcf.getStrategy());

                                logger.info("Found provider {}", provider.getName());
                                //Search the logo
                                String logo = Optional.ofNullable(pcf.getFieldset()).orElse(Collections.emptyList()).stream()
                                        .filter(prop -> prop.getValue1().equals("logo_img")).map(SimpleCustomProperty::getValue2)
                                        .findFirst().orElse(null);
                                if (logo == null) {
                                    logo = "/oxauth/auth/passport/img/" + provider.getName() + ".png";
                                } else if (!logo.startsWith("http")) {
                                    logo = "/oxauth/auth/passport/" + logo;
                                }
                                provider.setLogo(logo);

                                providers.add(provider);
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        });
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return providers;

    }

}
