package com.kayyagari;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class GitChannelRepository {
    
    private static GitChannelRepository channelRepo;

    private Repository repo;

    private Git git;
    
    private File dir;

    private ObjectXMLSerializer serializer;

    private Charset utf8 = Charset.forName("utf-8");

    private static Logger log = Logger.getLogger(GitChannelRepository.class);

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
                Repository repo = new FileRepositoryBuilder().setWorkTree(channelRepo.dir)
                        .readEnvironment().findGitDir().build();
                channelRepo.repo = repo;
                channelRepo.git = git;
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
}
