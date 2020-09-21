package com.kayyagari;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("/connectors/git-ext")
//@Tag(name = "Git Version Controller Extension")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface GitServletInterface {
    public RevisionInfo get();
}
