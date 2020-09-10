package com.kayyagari;

import java.io.File;
import java.util.UUID;

import org.eclipse.jgit.lib.PersonIdent;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.rules.TemporaryFolder;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class ChannelRepositoryTest {
    @ClassRule
    public static TemporaryFolder tmpFolder = new TemporaryFolder();
    
    private static ObjectXMLSerializer serializer;
    
    private PersonIdent author = new PersonIdent("admin", "admin@local");

    @BeforeClass
    public static void setup() throws Exception {
        serializer = ObjectXMLSerializer.getInstance();
        serializer.init("3.9.1");
    }
    
    @Test
    public void testChannelRevision() throws Exception {
        String parentDir = tmpFolder.newFolder().getAbsolutePath();
        parentDir = "/tmp";
        GitChannelRepository.init(parentDir, serializer);
        GitChannelRepository repo = GitChannelRepository.getInstance();
        
        Channel channel = new Channel(UUID.randomUUID().toString());
        File chFile = new File(repo.getDir(), channel.getId());
        assertFalse(chFile.exists());
        channel.setName("channel-to-be-added");
        repo.updateChannel(channel, author);
        assertTrue(chFile.exists());

        repo.removeChannel(channel, author);
        assertFalse(chFile.exists());
    }
}
