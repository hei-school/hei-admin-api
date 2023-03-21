# TeachingApi

All URIs are relative to *https://api-dev.hei.school*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createOrUpdateCourses**](TeachingApi.md#createOrUpdateCourses) | **PUT** /courses | Create new courses or update existing courses
[**createOrUpdateCoursesWithHttpInfo**](TeachingApi.md#createOrUpdateCoursesWithHttpInfo) | **PUT** /courses | Create new courses or update existing courses
[**createOrUpdateGroups**](TeachingApi.md#createOrUpdateGroups) | **PUT** /groups | Create new groups or update existing groups
[**createOrUpdateGroupsWithHttpInfo**](TeachingApi.md#createOrUpdateGroupsWithHttpInfo) | **PUT** /groups | Create new groups or update existing groups
[**getCourseById**](TeachingApi.md#getCourseById) | **GET** /courses/{id} | Get course by identifier
[**getCourseByIdWithHttpInfo**](TeachingApi.md#getCourseByIdWithHttpInfo) | **GET** /courses/{id} | Get course by identifier
[**getCourses**](TeachingApi.md#getCourses) | **GET** /courses | Get all courses
[**getCoursesWithHttpInfo**](TeachingApi.md#getCoursesWithHttpInfo) | **GET** /courses | Get all courses
[**getGroupById**](TeachingApi.md#getGroupById) | **GET** /groups/{id} | Get group by identifier
[**getGroupByIdWithHttpInfo**](TeachingApi.md#getGroupByIdWithHttpInfo) | **GET** /groups/{id} | Get group by identifier
[**getGroups**](TeachingApi.md#getGroups) | **GET** /groups | Get all groups
[**getGroupsWithHttpInfo**](TeachingApi.md#getGroupsWithHttpInfo) | **GET** /groups | Get all groups



## createOrUpdateCourses

> Course createOrUpdateCourses(course)

Create new courses or update existing courses

Update courses when id are provided, create them otherwise.

### Example

```java
// Import classes:
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.client.Configuration;
import school.hei.haapi.endpoint.rest.client.auth.*;
import school.hei.haapi.endpoint.rest.client.models.*;
import school.hei.haapi.endpoint.rest.api.TeachingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-dev.hei.school");
        
        // Configure HTTP bearer authorization: BearerAuth
        HttpBearerAuth BearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("BearerAuth");
        BearerAuth.setBearerToken("BEARER TOKEN");

        TeachingApi apiInstance = new TeachingApi(defaultClient);
        Course course = new Course(); // Course | 
        try {
            Course result = apiInstance.createOrUpdateCourses(course);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TeachingApi#createOrUpdateCourses");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **course** | [**Course**](Course.md)|  |

### Return type

[**Course**](Course.md)


### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | The created or updated courses |  -  |
| **400** | Bad request |  -  |
| **403** | Forbidden |  -  |
| **404** | Not found |  -  |
| **429** | Too many requests to the API |  -  |
| **500** | Internal server error |  -  |

## createOrUpdateCoursesWithHttpInfo

> ApiResponse<Course> createOrUpdateCourses createOrUpdateCoursesWithHttpInfo(course)

Create new courses or update existing courses

Update courses when id are provided, create them otherwise.

### Example

```java
// Import classes:
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.client.ApiResponse;
import school.hei.haapi.endpoint.rest.client.Configuration;
import school.hei.haapi.endpoint.rest.client.auth.*;
import school.hei.haapi.endpoint.rest.client.models.*;
import school.hei.haapi.endpoint.rest.api.TeachingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-dev.hei.school");
        
        // Configure HTTP bearer authorization: BearerAuth
        HttpBearerAuth BearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("BearerAuth");
        BearerAuth.setBearerToken("BEARER TOKEN");

        TeachingApi apiInstance = new TeachingApi(defaultClient);
        Course course = new Course(); // Course | 
        try {
            ApiResponse<Course> response = apiInstance.createOrUpdateCoursesWithHttpInfo(course);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling TeachingApi#createOrUpdateCourses");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **course** | [**Course**](Course.md)|  |

### Return type

ApiResponse<[**Course**](Course.md)>


### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | The created or updated courses |  -  |
| **400** | Bad request |  -  |
| **403** | Forbidden |  -  |
| **404** | Not found |  -  |
| **429** | Too many requests to the API |  -  |
| **500** | Internal server error |  -  |


## createOrUpdateGroups

> List<Group> createOrUpdateGroups(group)

Create new groups or update existing groups

Update groups when &#x60;id&#x60; are provided, create them otherwise.

### Example

```java
// Import classes:
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.client.Configuration;
import school.hei.haapi.endpoint.rest.client.auth.*;
import school.hei.haapi.endpoint.rest.client.models.*;
import school.hei.haapi.endpoint.rest.api.TeachingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-dev.hei.school");
        
        // Configure HTTP bearer authorization: BearerAuth
        HttpBearerAuth BearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("BearerAuth");
        BearerAuth.setBearerToken("BEARER TOKEN");

        TeachingApi apiInstance = new TeachingApi(defaultClient);
        List<Group> group = Arrays.asList(); // List<Group> | Groups to update
        try {
            List<Group> result = apiInstance.createOrUpdateGroups(group);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TeachingApi#createOrUpdateGroups");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **group** | [**List&lt;Group&gt;**](Group.md)| Groups to update |

### Return type

[**List&lt;Group&gt;**](Group.md)


### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | The created or updated groups |  -  |
| **400** | Bad request |  -  |
| **403** | Forbidden |  -  |
| **404** | Not found |  -  |
| **429** | Too many requests to the API |  -  |
| **500** | Internal server error |  -  |

## createOrUpdateGroupsWithHttpInfo

> ApiResponse<List<Group>> createOrUpdateGroups createOrUpdateGroupsWithHttpInfo(group)

Create new groups or update existing groups

Update groups when &#x60;id&#x60; are provided, create them otherwise.

### Example

```java
// Import classes:
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.client.ApiResponse;
import school.hei.haapi.endpoint.rest.client.Configuration;
import school.hei.haapi.endpoint.rest.client.auth.*;
import school.hei.haapi.endpoint.rest.client.models.*;
import school.hei.haapi.endpoint.rest.api.TeachingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-dev.hei.school");
        
        // Configure HTTP bearer authorization: BearerAuth
        HttpBearerAuth BearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("BearerAuth");
        BearerAuth.setBearerToken("BEARER TOKEN");

        TeachingApi apiInstance = new TeachingApi(defaultClient);
        List<Group> group = Arrays.asList(); // List<Group> | Groups to update
        try {
            ApiResponse<List<Group>> response = apiInstance.createOrUpdateGroupsWithHttpInfo(group);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling TeachingApi#createOrUpdateGroups");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **group** | [**List&lt;Group&gt;**](Group.md)| Groups to update |

### Return type

ApiResponse<[**List&lt;Group&gt;**](Group.md)>


### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | The created or updated groups |  -  |
| **400** | Bad request |  -  |
| **403** | Forbidden |  -  |
| **404** | Not found |  -  |
| **429** | Too many requests to the API |  -  |
| **500** | Internal server error |  -  |


## getCourseById

> Course getCourseById(id)

Get course by identifier

### Example

```java
// Import classes:
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.client.Configuration;
import school.hei.haapi.endpoint.rest.client.auth.*;
import school.hei.haapi.endpoint.rest.client.models.*;
import school.hei.haapi.endpoint.rest.api.TeachingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-dev.hei.school");
        
        // Configure HTTP bearer authorization: BearerAuth
        HttpBearerAuth BearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("BearerAuth");
        BearerAuth.setBearerToken("BEARER TOKEN");

        TeachingApi apiInstance = new TeachingApi(defaultClient);
        String id = "id_example"; // String | 
        try {
            Course result = apiInstance.getCourseById(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TeachingApi#getCourseById");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **String**|  |

### Return type

[**Course**](Course.md)


### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | The identified course |  -  |
| **400** | Bad request |  -  |
| **403** | Forbidden |  -  |
| **404** | Not found |  -  |
| **429** | Too many requests to the API |  -  |
| **500** | Internal server error |  -  |

## getCourseByIdWithHttpInfo

> ApiResponse<Course> getCourseById getCourseByIdWithHttpInfo(id)

Get course by identifier

### Example

```java
// Import classes:
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.client.ApiResponse;
import school.hei.haapi.endpoint.rest.client.Configuration;
import school.hei.haapi.endpoint.rest.client.auth.*;
import school.hei.haapi.endpoint.rest.client.models.*;
import school.hei.haapi.endpoint.rest.api.TeachingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-dev.hei.school");
        
        // Configure HTTP bearer authorization: BearerAuth
        HttpBearerAuth BearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("BearerAuth");
        BearerAuth.setBearerToken("BEARER TOKEN");

        TeachingApi apiInstance = new TeachingApi(defaultClient);
        String id = "id_example"; // String | 
        try {
            ApiResponse<Course> response = apiInstance.getCourseByIdWithHttpInfo(id);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling TeachingApi#getCourseById");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **String**|  |

### Return type

ApiResponse<[**Course**](Course.md)>


### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | The identified course |  -  |
| **400** | Bad request |  -  |
| **403** | Forbidden |  -  |
| **404** | Not found |  -  |
| **429** | Too many requests to the API |  -  |
| **500** | Internal server error |  -  |


## getCourses

> List<Course> getCourses()

Get all courses

### Example

```java
// Import classes:
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.client.Configuration;
import school.hei.haapi.endpoint.rest.client.auth.*;
import school.hei.haapi.endpoint.rest.client.models.*;
import school.hei.haapi.endpoint.rest.api.TeachingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-dev.hei.school");
        
        // Configure HTTP bearer authorization: BearerAuth
        HttpBearerAuth BearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("BearerAuth");
        BearerAuth.setBearerToken("BEARER TOKEN");

        TeachingApi apiInstance = new TeachingApi(defaultClient);
        try {
            List<Course> result = apiInstance.getCourses();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TeachingApi#getCourses");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**List&lt;Course&gt;**](Course.md)


### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | List of all courses |  -  |
| **400** | Bad request |  -  |
| **403** | Forbidden |  -  |
| **404** | Not found |  -  |
| **429** | Too many requests to the API |  -  |
| **500** | Internal server error |  -  |

## getCoursesWithHttpInfo

> ApiResponse<List<Course>> getCourses getCoursesWithHttpInfo()

Get all courses

### Example

```java
// Import classes:
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.client.ApiResponse;
import school.hei.haapi.endpoint.rest.client.Configuration;
import school.hei.haapi.endpoint.rest.client.auth.*;
import school.hei.haapi.endpoint.rest.client.models.*;
import school.hei.haapi.endpoint.rest.api.TeachingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-dev.hei.school");
        
        // Configure HTTP bearer authorization: BearerAuth
        HttpBearerAuth BearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("BearerAuth");
        BearerAuth.setBearerToken("BEARER TOKEN");

        TeachingApi apiInstance = new TeachingApi(defaultClient);
        try {
            ApiResponse<List<Course>> response = apiInstance.getCoursesWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling TeachingApi#getCourses");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

ApiResponse<[**List&lt;Course&gt;**](Course.md)>


### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | List of all courses |  -  |
| **400** | Bad request |  -  |
| **403** | Forbidden |  -  |
| **404** | Not found |  -  |
| **429** | Too many requests to the API |  -  |
| **500** | Internal server error |  -  |


## getGroupById

> Group getGroupById(id)

Get group by identifier

### Example

```java
// Import classes:
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.client.Configuration;
import school.hei.haapi.endpoint.rest.client.auth.*;
import school.hei.haapi.endpoint.rest.client.models.*;
import school.hei.haapi.endpoint.rest.api.TeachingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-dev.hei.school");
        
        // Configure HTTP bearer authorization: BearerAuth
        HttpBearerAuth BearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("BearerAuth");
        BearerAuth.setBearerToken("BEARER TOKEN");

        TeachingApi apiInstance = new TeachingApi(defaultClient);
        String id = "id_example"; // String | 
        try {
            Group result = apiInstance.getGroupById(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TeachingApi#getGroupById");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **String**|  |

### Return type

[**Group**](Group.md)


### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | The identified group |  -  |
| **400** | Bad request |  -  |
| **403** | Forbidden |  -  |
| **404** | Not found |  -  |
| **429** | Too many requests to the API |  -  |
| **500** | Internal server error |  -  |

## getGroupByIdWithHttpInfo

> ApiResponse<Group> getGroupById getGroupByIdWithHttpInfo(id)

Get group by identifier

### Example

```java
// Import classes:
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.client.ApiResponse;
import school.hei.haapi.endpoint.rest.client.Configuration;
import school.hei.haapi.endpoint.rest.client.auth.*;
import school.hei.haapi.endpoint.rest.client.models.*;
import school.hei.haapi.endpoint.rest.api.TeachingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-dev.hei.school");
        
        // Configure HTTP bearer authorization: BearerAuth
        HttpBearerAuth BearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("BearerAuth");
        BearerAuth.setBearerToken("BEARER TOKEN");

        TeachingApi apiInstance = new TeachingApi(defaultClient);
        String id = "id_example"; // String | 
        try {
            ApiResponse<Group> response = apiInstance.getGroupByIdWithHttpInfo(id);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling TeachingApi#getGroupById");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **String**|  |

### Return type

ApiResponse<[**Group**](Group.md)>


### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | The identified group |  -  |
| **400** | Bad request |  -  |
| **403** | Forbidden |  -  |
| **404** | Not found |  -  |
| **429** | Too many requests to the API |  -  |
| **500** | Internal server error |  -  |


## getGroups

> List<Group> getGroups()

Get all groups

### Example

```java
// Import classes:
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.client.Configuration;
import school.hei.haapi.endpoint.rest.client.auth.*;
import school.hei.haapi.endpoint.rest.client.models.*;
import school.hei.haapi.endpoint.rest.api.TeachingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-dev.hei.school");
        
        // Configure HTTP bearer authorization: BearerAuth
        HttpBearerAuth BearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("BearerAuth");
        BearerAuth.setBearerToken("BEARER TOKEN");

        TeachingApi apiInstance = new TeachingApi(defaultClient);
        try {
            List<Group> result = apiInstance.getGroups();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling TeachingApi#getGroups");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**List&lt;Group&gt;**](Group.md)


### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | List of groups |  -  |
| **400** | Bad request |  -  |
| **403** | Forbidden |  -  |
| **404** | Not found |  -  |
| **429** | Too many requests to the API |  -  |
| **500** | Internal server error |  -  |

## getGroupsWithHttpInfo

> ApiResponse<List<Group>> getGroups getGroupsWithHttpInfo()

Get all groups

### Example

```java
// Import classes:
import school.hei.haapi.endpoint.rest.client.ApiClient;
import school.hei.haapi.endpoint.rest.client.ApiException;
import school.hei.haapi.endpoint.rest.client.ApiResponse;
import school.hei.haapi.endpoint.rest.client.Configuration;
import school.hei.haapi.endpoint.rest.client.auth.*;
import school.hei.haapi.endpoint.rest.client.models.*;
import school.hei.haapi.endpoint.rest.api.TeachingApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-dev.hei.school");
        
        // Configure HTTP bearer authorization: BearerAuth
        HttpBearerAuth BearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("BearerAuth");
        BearerAuth.setBearerToken("BEARER TOKEN");

        TeachingApi apiInstance = new TeachingApi(defaultClient);
        try {
            ApiResponse<List<Group>> response = apiInstance.getGroupsWithHttpInfo();
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling TeachingApi#getGroups");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

ApiResponse<[**List&lt;Group&gt;**](Group.md)>


### Authorization

[BearerAuth](../README.md#BearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | List of groups |  -  |
| **400** | Bad request |  -  |
| **403** | Forbidden |  -  |
| **404** | Not found |  -  |
| **429** | Too many requests to the API |  -  |
| **500** | Internal server error |  -  |

