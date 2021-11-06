package org.gluu.casa.plugins.consentmanagementportal.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.gluu.casa.rest.RSUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


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


    public  <T> T getClient(Class<T> serviceInterface, String url)
    {
        ResteasyWebTarget target = resteasyClient.target(url);
        return target.proxy(serviceInterface);
    }

    public <T> T resteasy(T t, String apiUrl, String path, String appType, Class<T> clazz ) throws IOException {
        ResteasyWebTarget target = resteasyClient.target(apiUrl).path(path);
        return target.request().put(Entity.entity(t, appType), clazz);
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
        ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        Response response = target.request().header("Content-Type", MediaType.APPLICATION_JSON).post(Entity.json(req));
        String value = response.readEntity(String.class);
        response.close();
        Res data= mapper.readValue(value, clazz);
        return data;
    }


    @PreDestroy
    private void destroy() {
        if (resteasyClient != null) {
            resteasyClient.close();
        }
    }
}
