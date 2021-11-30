package org.gluu.casa.plugins.consent.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;






public class BaseExternalApiClient {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Client getClient() {
        HttpClient httpClient = HttpClientBuilder.create().setConnectionManager(new PoolingHttpClientConnectionManager()).build();
        ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(httpClient);
        return new ResteasyClientBuilder().httpEngine(engine).build();
    }
    public <T> T doGet( Class<T> clazz, String url, String path, String ...param) throws JsonProcessingException {
        WebTarget target = getClient().target(url).path(path);
        Response response = target.request().header("Content-Type", MediaType.APPLICATION_JSON).get();
        String value = response.readEntity(String.class);
        response.close();
        ObjectMapper mapper = new ObjectMapper();
        T data= mapper.readValue(value, clazz);
        return data;
    }
    public <Req,Res> Res doPost(Req req,Class<Res> clazz, String url, String path) throws JsonProcessingException {

        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        WebTarget target = getClient().target(url).path(path);
        String jsonRequest = mapper.writeValueAsString(req);

        Response response = target.request().header("Content-Type", MediaType.APPLICATION_JSON_TYPE).post(Entity.json(req));
        String value = response.readEntity(String.class);
        logger.info(value);
        response.close();
        Res data= mapper.reader().forType(clazz).readValue(value);
        return data;
    }

}
