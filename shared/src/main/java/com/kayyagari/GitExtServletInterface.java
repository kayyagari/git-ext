package com.kayyagari;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
    public static final String PLUGIN_NAME = "Git Extension";
    
    @GET
    @Path("/history")
    @Operation(summary = "Returns a List of all revisions of the given filename")
    @MirthOperation(name = "getHistory", display = "Get all revisions of a file", permission = Permissions.CHANNELS_VIEW, type = ExecuteType.ASYNC, auditable = false)
    public List<RevisionInfo> getHistory(@Param("fileName") @Parameter(description = "The name of the file", required = true) @QueryParam("fileName") String fileName) throws ClientException;

    @GET
    @Path("/content")
    @Operation(summary = "Returns the content of the given file at the specified revision")
    @MirthOperation(name = "getContent", display = "Get the content of the file at a specific revision", permission = Permissions.CHANNELS_VIEW, type = ExecuteType.SYNC, auditable = false)
    public String getContent(@Param("fileName") @Parameter(description = "The name of the file", required = true) @QueryParam("fileName") String fileName,
            @Param("revision") @Parameter(description = "The value of revision", required = true) @QueryParam("revision") String revision) throws ClientException;
}
