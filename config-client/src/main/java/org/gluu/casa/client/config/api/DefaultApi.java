package org.gluu.casa.client.config.api;

import org.gluu.casa.client.config.ApiException;
import org.gluu.casa.client.config.ApiClient;
import org.gluu.casa.client.config.ApiResponse;
import org.gluu.casa.client.config.Configuration;
import org.gluu.casa.client.config.Pair;

import javax.ws.rs.core.GenericType;

import org.gluu.casa.client.config.model.ClientSettings;
import org.gluu.casa.client.config.model.OxdConfiguration;
import org.gluu.casa.client.config.model.OxdSettings;
import org.gluu.casa.client.config.model.PluginDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-07-18T14:15:05.565Z")
public class DefaultApi {
  private ApiClient apiClient;

  public DefaultApi() {
    this(Configuration.getDefaultApiClient());
  }

  public DefaultApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * 
   * Assigns the responsible plugin for a given authentication method (in terms of enrollment). Responsibility can also be delegated to an internal (default) plugin implementation if available for the method in question
   * @param acr Identifier of the authentication method (required)
   * @param plugin Identifier of the plugin to assign. If this param is missing or empty, the default implementation is assigned (if existing) (optional)
   * @throws ApiException if fails to make API call
   */
  public void authnMethodsAssignPluginPost(String acr, String plugin) throws ApiException {

    authnMethodsAssignPluginPostWithHttpInfo(acr, plugin);
  }

  /**
   * 
   * Assigns the responsible plugin for a given authentication method (in terms of enrollment). Responsibility can also be delegated to an internal (default) plugin implementation if available for the method in question
   * @param acr Identifier of the authentication method (required)
   * @param plugin Identifier of the plugin to assign. If this param is missing or empty, the default implementation is assigned (if existing) (optional)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> authnMethodsAssignPluginPostWithHttpInfo(String acr, String plugin) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'acr' is set
    if (acr == null) {
      throw new ApiException(400, "Missing the required parameter 'acr' when calling authnMethodsAssignPluginPost");
    }
    
    // create path and map variables
    String localVarPath = "/authn-methods/assign-plugin";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (acr != null)
      localVarFormParams.put("acr", acr);
if (plugin != null)
      localVarFormParams.put("plugin", plugin);

    final String[] localVarAccepts = {
      "text/plain"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/x-www-form-urlencoded"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "config_auth" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * Returns the authentication methods that can be used in Casa whether enabled or not. Note that for any method to be reported here, there has to be an enabled custom script in the underlying Gluu installation and a plugin implementing its enrollment logic (unless it is a method supported out-of-the-box, where no plugin is required)
   * @return List&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public List<String> authnMethodsAvailableGet() throws ApiException {
    return authnMethodsAvailableGetWithHttpInfo().getData();
      }

  /**
   * 
   * Returns the authentication methods that can be used in Casa whether enabled or not. Note that for any method to be reported here, there has to be an enabled custom script in the underlying Gluu installation and a plugin implementing its enrollment logic (unless it is a method supported out-of-the-box, where no plugin is required)
   * @return ApiResponse&lt;List&lt;String&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<String>> authnMethodsAvailableGetWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/authn-methods/available";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "config_auth" };

    GenericType<List<String>> localVarReturnType = new GenericType<List<String>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * 
   * Disables a specific authentication method
   * @param acr Identifier of the authentication method to disable (required)
   * @throws ApiException if fails to make API call
   */
  public void authnMethodsDisablePost(String acr) throws ApiException {

    authnMethodsDisablePostWithHttpInfo(acr);
  }

  /**
   * 
   * Disables a specific authentication method
   * @param acr Identifier of the authentication method to disable (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> authnMethodsDisablePostWithHttpInfo(String acr) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'acr' is set
    if (acr == null) {
      throw new ApiException(400, "Missing the required parameter 'acr' when calling authnMethodsDisablePost");
    }
    
    // create path and map variables
    String localVarPath = "/authn-methods/disable";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (acr != null)
      localVarFormParams.put("acr", acr);

    final String[] localVarAccepts = {
      "text/plain"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/x-www-form-urlencoded"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "config_auth" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * Returns the authentication methods currently enabled for Casa
   * @return List&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public List<String> authnMethodsEnabledGet() throws ApiException {
    return authnMethodsEnabledGetWithHttpInfo().getData();
      }

  /**
   * 
   * Returns the authentication methods currently enabled for Casa
   * @return ApiResponse&lt;List&lt;String&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<String>> authnMethodsEnabledGetWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/authn-methods/enabled";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "config_auth" };

    GenericType<List<String>> localVarReturnType = new GenericType<List<String>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * 
   * Returns the CORS domains registered (so Casa REST services can be consumed from the browser)
   * @return List&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public List<String> corsGet() throws ApiException {
    return corsGetWithHttpInfo().getData();
      }

  /**
   * 
   * Returns the CORS domains registered (so Casa REST services can be consumed from the browser)
   * @return ApiResponse&lt;List&lt;String&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<String>> corsGetWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/cors";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "config_auth" };

    GenericType<List<String>> localVarReturnType = new GenericType<List<String>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * 
   * Replaces the current registered CORS domains with the list passed in the body of the request (as a JSON array of strings)
   * @param cors  (required)
   * @throws ApiException if fails to make API call
   */
  public void corsPut(List<String> cors) throws ApiException {

    corsPutWithHttpInfo(cors);
  }

  /**
   * 
   * Replaces the current registered CORS domains with the list passed in the body of the request (as a JSON array of strings)
   * @param cors  (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> corsPutWithHttpInfo(List<String> cors) throws ApiException {
    Object localVarPostBody = cors;
    
    // verify the required parameter 'cors' is set
    if (cors == null) {
      throw new ApiException(400, "Missing the required parameter 'cors' when calling corsPut");
    }
    
    // create path and map variables
    String localVarPath = "/cors";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "text/plain"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "config_auth" };


    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * Returns the current logging level (Any of ERROR, WARN, INFO, DEBUG, or TRACE)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String logLevelGet() throws ApiException {
    return logLevelGetWithHttpInfo().getData();
      }

  /**
   * 
   * Returns the current logging level (Any of ERROR, WARN, INFO, DEBUG, or TRACE)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> logLevelGetWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/log-level";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "text/plain"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "config_auth" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * 
   * Sets the logging level in use by the application
   * @param level Any of ERROR, WARN, INFO, DEBUG, or TRACE (required)
   * @throws ApiException if fails to make API call
   */
  public void logLevelPost(String level) throws ApiException {

    logLevelPostWithHttpInfo(level);
  }

  /**
   * 
   * Sets the logging level in use by the application
   * @param level Any of ERROR, WARN, INFO, DEBUG, or TRACE (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> logLevelPostWithHttpInfo(String level) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'level' is set
    if (level == null) {
      throw new ApiException(400, "Missing the required parameter 'level' when calling logLevelPost");
    }
    
    // create path and map variables
    String localVarPath = "/log-level";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (level != null)
      localVarFormParams.put("level", level);

    final String[] localVarAccepts = {
      "text/plain"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/x-www-form-urlencoded"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "config_auth" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * Returns configuration details about the underlying OXD server and the registered OIDC client registered employed for authentication purposes
   * @return OxdConfiguration
   * @throws ApiException if fails to make API call
   */
  public OxdConfiguration oxdGet() throws ApiException {
    return oxdGetWithHttpInfo().getData();
      }

  /**
   * 
   * Returns configuration details about the underlying OXD server and the registered OIDC client registered employed for authentication purposes
   * @return ApiResponse&lt;OxdConfiguration&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<OxdConfiguration> oxdGetWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/oxd";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "config_auth" };

    GenericType<OxdConfiguration> localVarReturnType = new GenericType<OxdConfiguration>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * 
   * Replaces the current OXD configuration with the one passed in the payload. This will provoke the oxd server referenced in the payload to re-register or update the OIDC client used
   * @param settings  (required)
   * @return ClientSettings
   * @throws ApiException if fails to make API call
   */
  public ClientSettings oxdPut(OxdSettings settings) throws ApiException {
    return oxdPutWithHttpInfo(settings).getData();
      }

  /**
   * 
   * Replaces the current OXD configuration with the one passed in the payload. This will provoke the oxd server referenced in the payload to re-register or update the OIDC client used
   * @param settings  (required)
   * @return ApiResponse&lt;ClientSettings&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ClientSettings> oxdPutWithHttpInfo(OxdSettings settings) throws ApiException {
    Object localVarPostBody = settings;
    
    // verify the required parameter 'settings' is set
    if (settings == null) {
      throw new ApiException(400, "Missing the required parameter 'settings' when calling oxdPut");
    }
    
    // create path and map variables
    String localVarPath = "/oxd";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "config_auth" };

    GenericType<ClientSettings> localVarReturnType = new GenericType<ClientSettings>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * 
   * Returns data about the currently deployed plugins that implement enrollment logic for a particular authentication method
   * @param acr ACR corresponding to the authentication method (required)
   * @return List&lt;PluginDescriptor&gt;
   * @throws ApiException if fails to make API call
   */
  public List<PluginDescriptor> pluginsAuthnMethodImplAcrGet(String acr) throws ApiException {
    return pluginsAuthnMethodImplAcrGetWithHttpInfo(acr).getData();
      }

  /**
   * 
   * Returns data about the currently deployed plugins that implement enrollment logic for a particular authentication method
   * @param acr ACR corresponding to the authentication method (required)
   * @return ApiResponse&lt;List&lt;PluginDescriptor&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<PluginDescriptor>> pluginsAuthnMethodImplAcrGetWithHttpInfo(String acr) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'acr' is set
    if (acr == null) {
      throw new ApiException(400, "Missing the required parameter 'acr' when calling pluginsAuthnMethodImplAcrGet");
    }
    
    // create path and map variables
    String localVarPath = "/plugins/authn-method-impl/{acr}"
      .replaceAll("\\{" + "acr" + "\\}", apiClient.escapeString(acr.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "config_auth" };

    GenericType<List<PluginDescriptor>> localVarReturnType = new GenericType<List<PluginDescriptor>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * 
   * Returns data about the currently deployed plugins
   * @return List&lt;PluginDescriptor&gt;
   * @throws ApiException if fails to make API call
   */
  public List<PluginDescriptor> pluginsGet() throws ApiException {
    return pluginsGetWithHttpInfo().getData();
      }

  /**
   * 
   * Returns data about the currently deployed plugins
   * @return ApiResponse&lt;List&lt;PluginDescriptor&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<PluginDescriptor>> pluginsGetWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/plugins";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "config_auth" };

    GenericType<List<PluginDescriptor>> localVarReturnType = new GenericType<List<PluginDescriptor>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * 
   * Provokes the internal plugin checker timer to remove this plugin upon its next run
   * @param id Identifier of the plugin to remove (required)
   * @return Integer
   * @throws ApiException if fails to make API call
   */
  public Integer pluginsScheduleRemovalIdPost(String id) throws ApiException {
    return pluginsScheduleRemovalIdPostWithHttpInfo(id).getData();
      }

  /**
   * 
   * Provokes the internal plugin checker timer to remove this plugin upon its next run
   * @param id Identifier of the plugin to remove (required)
   * @return ApiResponse&lt;Integer&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Integer> pluginsScheduleRemovalIdPostWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling pluginsScheduleRemovalIdPost");
    }
    
    // create path and map variables
    String localVarPath = "/plugins/schedule-removal/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "config_auth" };

    GenericType<Integer> localVarReturnType = new GenericType<Integer>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * 
   * Returns if password reset feature is available in this installation
   * @return Boolean
   * @throws ApiException if fails to make API call
   */
  public Boolean pwdResetAvailableGet() throws ApiException {
    return pwdResetAvailableGetWithHttpInfo().getData();
      }

  /**
   * 
   * Returns if password reset feature is available in this installation
   * @return ApiResponse&lt;Boolean&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Boolean> pwdResetAvailableGetWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/pwd-reset/available";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "config_auth" };

    GenericType<Boolean> localVarReturnType = new GenericType<Boolean>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * 
   * Returns if password reset feature is enabled
   * @return Boolean
   * @throws ApiException if fails to make API call
   */
  public Boolean pwdResetEnabledGet() throws ApiException {
    return pwdResetEnabledGetWithHttpInfo().getData();
      }

  /**
   * 
   * Returns if password reset feature is enabled
   * @return ApiResponse&lt;Boolean&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Boolean> pwdResetEnabledGetWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/pwd-reset/enabled";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "config_auth" };

    GenericType<Boolean> localVarReturnType = new GenericType<Boolean>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * 
   * Disables password reset
   * @throws ApiException if fails to make API call
   */
  public void pwdResetTurnOffPost() throws ApiException {

    pwdResetTurnOffPostWithHttpInfo();
  }

  /**
   * 
   * Disables password reset
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> pwdResetTurnOffPostWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/pwd-reset/turn-off";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "text/plain"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "config_auth" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * Enables password reset
   * @throws ApiException if fails to make API call
   */
  public void pwdResetTurnOnPost() throws ApiException {

    pwdResetTurnOnPostWithHttpInfo();
  }

  /**
   * 
   * Enables password reset
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> pwdResetTurnOnPostWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/pwd-reset/turn-on";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "text/plain"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "config_auth" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
