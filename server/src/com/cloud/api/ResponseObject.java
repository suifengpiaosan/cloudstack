package com.cloud.api;

public interface ResponseObject {
    /**
     * Get the name of the API response
     * @return the name of the API response
     */
    String getResponseName();

    /**
     * Set the name of the API response
     * @param name
     */
    void setResponseName(String name);
    
    /**
     * Get the name of the API object
     * @return the name of the API object
     */
    String getObjectName();

    /**
     * Set the name of the APIobject
     * @param name
     */
    void setObjectName(String name);
}
