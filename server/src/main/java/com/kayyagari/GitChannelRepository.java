package com.kayyagari;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.mirth.connect.server.controllers.ChannelController;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectStream;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

/**
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class GitChannelRepository {
    
    private static GitChannelRepository channelRepo;

    private Repository repo;

    private Git git;
    
    private File dir;

    private ObjectXMLSerializer serializer;

    private Charset utf8 = StandardCharsets.UTF_8;

    private static Logger log = LoggerFactory.getLogger(GitChannelRepository.class);

    public static final String DATA_DIR = "git-ext-data";

    private GitChannelRepository() {
    }
    
    public static synchronized void init(String parentDir, ObjectXMLSerializer serializer) {
        if(channelRepo == null) {
            channelRepo = new GitChannelRepository();
            try {
                log.info("initializing channel and code template repository...");
                if(parentDir == null) {
                    parentDir = Donkey.getInstance().getConfiguration().getAppData();
                }

                channelRepo.dir = new File(parentDir, DATA_DIR);
                channelRepo.dir.mkdir();
                
                Git git = Git.init().setDirectory(channelRepo.dir).call();
                StoredConfig config = git.getRepository().getConfig();
                if(config.getBoolean("commit", "gpgsign", false)) {
                    log.warn("gpg signing of commits is not yet supported, setting commit.gpgsign to false");
                    config.setBoolean("commit", null, "gpgsign", false);
                }
                channelRepo.repo = git.getRepository();
                channelRepo.git = git;
                serializer.allowTypes(Collections.EMPTY_LIST, Arrays.asList(RevisionInfo.class.getPackage().getName() + ".**"), Collections.EMPTY_LIST);
                channelRepo.serializer = serializer;
            }
            catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public static GitChannelRepository getInstance() {
        if(channelRepo == null) {
            throw new IllegalStateException("repository not initialized, call init() before calling getInstance()");
        }
        return channelRepo;
    }
    
    public File getDir() {
        return dir;
    }

    public void updateChannel(Channel channel, PersonIdent committer) {
        updateFile(channel.getId(), channel, committer);
    }

    private void updateFile(String id, Object obj, PersonIdent committer) {
        try {
            File f = new File(dir, id);
            String xml = serializer.serialize(obj);
            FileOutputStream fout = new FileOutputStream(f);
            fout.write(xml.getBytes(utf8));
            fout.close();

            git.add().addFilepattern(id).call();
            git.commit().setCommitter(committer).setMessage("").call();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void removeChannel(Channel channel, PersonIdent committer) {
        removeFile(channel.getId(), committer);
    }

    private void removeFile(String id, PersonIdent committer) {
        try {
            git.rm().addFilepattern(id).call();
            git.commit().setCommitter(committer).setMessage("").call();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateCodeTemplate(CodeTemplate ct, PersonIdent committer) {
        updateFile(ct.getId(), ct, committer);
    }

    public void removeCodeTemplate(CodeTemplate ct, PersonIdent committer) {
        removeFile(ct.getId(), committer);        
    }
    
    public void close() {
        repo.close();
        channelRepo = null;
    }

    public List<RevisionInfo> getHistory(String fileName) throws Exception {
        List<RevisionInfo> lst = new ArrayList<>();
        
        if(repo.resolve(Constants.HEAD) != null) {
        	Iterator<RevCommit> rcItr = git.log().addPath(fileName).call().iterator();
        	while(rcItr.hasNext()) {
        		RevCommit rc = rcItr.next();
        		lst.add(toRevisionInfo(rc));
        	}
        }

        return lst;
    }
    
    public String getContent(String fileName, String revision) throws Exception {
        if(StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("fileName is required");
        }

        if(StringUtils.isBlank(revision)) {
            throw new IllegalArgumentException("revision is required");
        }

        String content = null;
        try(TreeWalk tw = new TreeWalk(repo)) {
            ObjectId rcid = repo.resolve(revision);
            if(rcid != null) {
                RevCommit rc = repo.parseCommit(rcid);
                
                tw.setRecursive(true);
                tw.setFilter(PathFilter.create(fileName));
                tw.addTree(rc.getTree());
                if(tw.next()) {
                    ObjectLoader objLoader = repo.open(tw.getObjectId(0));
                    ObjectStream stream = objLoader.openStream();
                    byte[] buf = new byte[1024];
                    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                    while(true) {
                        int len = stream.read(buf);
                        if(len <= 0) {
                            break;
                        }
                        byteOut.write(buf, 0, len);
                    }
                    stream.close();
                    
                    content = new String(byteOut.toByteArray(), utf8);
                }
            }
        }
        catch(MissingObjectException e) {
            log.debug("commit {} not found for file {}", revision, fileName, e);
            throw e;
        }
        return content;
    }

    public Channel getChannelAtRevision(String channelId, String targetRevision) throws Exception {
        if(repo.resolve(Constants.HEAD) != null) {
            Iterator<RevCommit> rcItr = git.log().addPath(channelId).call().iterator();
            if(rcItr.hasNext()) {
                RevCommit rc = rcItr.next();
                if(rc.getName().equals(targetRevision)) {
                    throw new IllegalArgumentException("cannot revert to the same revision");
                }
            }

            String targetContent = getContent(channelId, targetRevision);
            return serializer.deserialize(targetContent, Channel.class);
        }
        throw new IllegalArgumentException("no revision " + targetRevision + " of Channel " + channelId + " exists");
    }

    private RevisionInfo toRevisionInfo(RevCommit rc) {
        RevisionInfo ri = new RevisionInfo();
        PersonIdent committer = rc.getCommitterIdent();
        ri.setCommitterEmail(committer.getEmailAddress());
        ri.setCommitterName(committer.getName());
        ri.setHash(rc.getName());
        ri.setMessage(rc.getFullMessage());
        ri.setTime(committer.getWhen().getTime());
        
        return ri;
    }
}
