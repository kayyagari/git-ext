package com.kayyagari;

/*
   Copyright [2024] [Kiran Ayyagari]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.mirth.connect.client.core.Operation.ExecuteType;
import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.Permissions;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;
import com.mirth.connect.client.core.api.Param;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
@Path("/extensions/git-ext")
@Tag(name = "Git Version Controller Extension")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface GitExtServletInterface extends BaseServletInterface {
    String PLUGIN_NAME = "Git Extension";
    
    @GET
    @Path("/history")
    @Operation(summary = "Returns a List of all revisions of the given filename")
    @MirthOperation(name = "getHistory", display = "Get all revisions of a file", permission = Permissions.CHANNELS_VIEW, type = ExecuteType.ASYNC, auditable = false)
    List<RevisionInfo> getHistory(@Param("fileName") @Parameter(description = "The name of the file", required = true) @QueryParam("fileName") String fileName) throws ClientException;

    @GET
    @Path("/content")
    @Operation(summary = "Returns the content of the given file at the specified revision")
    @MirthOperation(name = "getContent", display = "Get the content of the file at a specific revision", permission = Permissions.CHANNELS_VIEW, type = ExecuteType.SYNC, auditable = false)
    String getContent(@Param("fileName") @Parameter(description = "The name of the file", required = true) @QueryParam("fileName") String fileName,
            @Param("revision") @Parameter(description = "The value of revision", required = true) @QueryParam("revision") String revision) throws ClientException;

    @POST
    @Path("/revert")
    @Operation(summary = "Revert the given file to the specified revision")
    @MirthOperation(name = "revert", display = "Revert the given file to the specified revision", permission = Permissions.CHANNELS_VIEW, type = ExecuteType.SYNC, auditable = false)
    void revert(@Param("fileName") @Parameter(description = "The name of the file", required = true) @QueryParam("fileName") String fileName,
            @Param("revision") @Parameter(description = "The value of revision", required = true) @QueryParam("revision") String revision) throws ClientException;
}
