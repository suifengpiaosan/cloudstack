/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.cloud.api.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.api.ApiConstants;
import com.cloud.api.ApiResponseHelper;
import com.cloud.api.BaseListCmd;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.response.ClusterResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.dc.ClusterVO;

@Implementation(method="searchForClusters", description="Lists clusters.")
public class ListClustersCmd extends BaseListCmd {
    public static final Logger s_logger = Logger.getLogger(ListServiceOfferingsCmd.class.getName());

    private static final String s_name = "listclustersresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name=ApiConstants.ID, type=CommandType.LONG, description="lists clusters by the cluster ID")
    private Long id;

    @Parameter(name=ApiConstants.NAME, type=CommandType.STRING, description="lists clusters by the cluster name")
    private String clusterName;

    @Parameter(name=ApiConstants.POD_ID, type=CommandType.LONG, description="lists clusters by Pod ID")
    private Long podId;

    @Parameter(name=ApiConstants.ZONE_ID, type=CommandType.LONG, description="lists clusters by Zone ID")
    private Long zoneId;


    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public String getClusterName() {
        return clusterName;
    }

    public Long getPodId() {
        return podId;
    }

    public Long getZoneId() {
        return zoneId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getName() {
        return s_name;
    }

    @Override @SuppressWarnings("unchecked")
    public ListResponse<ClusterResponse> getResponse() {
        List<ClusterVO> clusters = (List<ClusterVO>)getResponseObject();

        ListResponse<ClusterResponse> response = new ListResponse<ClusterResponse>();
        List<ClusterResponse> clusterResponses = new ArrayList<ClusterResponse>();
        for (ClusterVO cluster : clusters) {
            ClusterResponse clusterResponse = ApiResponseHelper.createClusterResponse(cluster);
            clusterResponse.setObjectName("cluster");
            clusterResponses.add(clusterResponse);
        }

        response.setResponses(clusterResponses);
        response.setResponseName(getName());
        return response;
    }
}
