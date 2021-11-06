package org.gluu.casa.plugins.consentmanagementportal.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.gluu.casa.rest.RSUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;


public class BaseExternalApiClient {


    private ResteasyClient resteasyClient;
    public final ResteasyClient CLIENT = new ResteasyClientBuilderImpl()
            .connectionPoolSize(30)
            .connectTimeout(20, TimeUnit.SECONDS)
            .build();
    @PostConstruct
    public void inited() {

        resteasyClient = RSUtils.getClient();
    }


    public <T> T doGet( Class<T> clazz, String url, String path, String ...param) throws JsonProcessingException {

        ResteasyClient client = new ResteasyClientBuilderImpl()
                .connectionPoolSize(30)
                .connectTimeout(20, TimeUnit.SECONDS)
                .build();
        ResteasyWebTarget target = client.target(url).path(path);
        Response response = target.request().header("Content-Type", MediaType.APPLICATION_JSON).get();
        String value = response.readEntity(String.class);
        response.close();
        ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        T data= mapper.readValue(value, clazz);
        return data;
    }
    public <Req,Res> Res doPost(Req req,Class<Res> clazz, String url, String path) throws JsonProcessingException {

        ResteasyClient client = new ResteasyClientBuilderImpl()
                .connectionPoolSize(30)
                .connectTimeout(20, TimeUnit.SECONDS)
                .build();
        ResteasyWebTarget target = client.target(url).path(path);

        Response response = target.request().header("Content-Type", MediaType.APPLICATION_JSON).post(Entity.json(req));
        String value = response.readEntity(String.class);
        System.out.println(value);
        response.close();

        ObjectMapper mapper = getObjectMapper();

        Res data= mapper.reader().forType(clazz).readValue(value);
        System.out.println(data);
        return data;
    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        return mapper;
    }


    @PreDestroy
    private void destroy() {
        if (resteasyClient != null) {
            resteasyClient.close();
        }
    }
}
