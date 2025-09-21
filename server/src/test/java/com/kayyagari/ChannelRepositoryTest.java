package com.kayyagari;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import com.mirth.connect.model.converters.ObjectXMLSerializer;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.lib.PersonIdent;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.mirth.connect.model.Channel;

public class ChannelRepositoryTest {
    @ClassRule
    public static TemporaryFolder tmpFolder = new TemporaryFolder();
    
    private static ObjectXMLSerializer serializer;
    
    private PersonIdent committer;

    private String parentDir;
    
    private GitChannelRepository repo;

    @BeforeClass
    public static void setup() throws Exception {
        serializer = ObjectXMLSerializer.getInstance();
        serializer.init("4.5.2");
    }

    @Before
    public void initRepo() throws Exception {
        parentDir = tmpFolder.newFolder().getAbsolutePath();
        //parentDir = "/tmp";
        GitChannelRepository.init(parentDir, serializer);
        repo = GitChannelRepository.getInstance();
        System.out.println(repo.getDir());
        committer = new PersonIdent("admin", "admin@local", System.currentTimeMillis(), 0);
    }

    @After
    public void deleteRepo() {
        repo.close();
        FileUtils.deleteQuietly(repo.getDir());
    }

    @Test
    public void testChannelRevision() throws Exception {
        Channel channel = new Channel(UUID.randomUUID().toString());
        File chFile = new File(repo.getDir(), channel.getId());
        assertFalse(chFile.exists());
        channel.setName("channel-to-be-added");
        repo.updateChannel(channel, committer);
        assertTrue(chFile.exists());

        repo.removeChannel(channel, committer);
        assertFalse(chFile.exists());
    }
    
    @Test
    public void testChannelHistory() throws Exception {
        Channel channel1 = new Channel(UUID.randomUUID().toString());
        File chFile = new File(repo.getDir(), channel1.getId());

        channel1.setRevision(1);
        channel1.setDescription("description update 1");
        channel1.setName("channel1-to-be-added");
        repo.updateChannel(channel1, committer);

        Channel channel2 = new Channel(UUID.randomUUID().toString());
        File chFile2 = new File(repo.getDir(), channel2.getId());

        channel2.setRevision(1);
        channel2.setDescription("description add 1");
        channel2.setName("channel2-to-be-added");
        repo.updateChannel(channel2, committer);

        int revCount = 5;
        for(int i=2; i<= revCount; i++) {
            channel1.setRevision(i);
            channel1.setDescription("description update " + i);
            repo.updateChannel(channel1, committer);

            channel2.setDescription("description update " + i);
            repo.updateChannel(channel2, committer);
        }

        
        List<RevisionInfo> commits = repo.getHistory(chFile.getName());
        assertEquals(revCount, commits.size());
        for(RevisionInfo ri : commits) {
            assertNotNull(ri.getCommitterEmail());
            assertNotNull(ri.getCommitterName());
            assertNotNull(ri.getHash());
            assertNotNull(ri.getMessage());
            assertTrue(ri.getTime() > 0);
        }
        
        String content = repo.getContent(channel1.getId(), commits.get(0).getHash());
        assertNotNull(content);
        
        content = repo.getContent("non-exiting-file", commits.get(0).getHash()); // invalid file but valid hash
        assertNull(content);

        content = repo.getContent(channel1.getId(), "abcde"); // valid file but invalid hash
        assertNull(content);
    }

    // test fetching history when no file was committed
    @Test
    public void testFetchHistoryOnNewRepo() throws Exception {
    	List<RevisionInfo> commits = repo.getHistory("non-existing-file");
    	assertTrue(commits.isEmpty());
    }

    @Test
    public void testRevert() throws Exception {
        String channelId = UUID.randomUUID().toString();
        Channel channel1 = new Channel(channelId);
        channel1.setRevision(1);
        channel1.setDescription("description update 1");
        channel1.setName("channel1");
        repo.updateChannel(channel1, committer);

        int revCount = 10;
        for(int i=2; i<= revCount; i++) {
            channel1.setRevision(i);
            channel1.setDescription("description update " + i);
            repo.updateChannel(channel1, committer);
        }

        List<RevisionInfo> historyBeforeRevert = repo.getHistory(channelId);
        RevisionInfo secondCommit = historyBeforeRevert.get(historyBeforeRevert.size() - 2);
        String secondCommitContent = repo.getContent(channelId, secondCommit.getHash());
        assertTrue(secondCommitContent.contains("description update 2"));

        String currentContent = repo.getContent(channelId, historyBeforeRevert.get(0).getHash());

        File chFile = new File(repo.getDir(), channelId);
        assertEquals(currentContent, FileUtils.readFileToString(chFile, StandardCharsets.UTF_8));

        repo.revertFile(channelId, secondCommit.getHash(), committer);
        List<RevisionInfo> historyAfterRevert = repo.getHistory(channelId);
        assertEquals(historyBeforeRevert.size() + 1, historyAfterRevert.size());
        currentContent = repo.getContent(channelId, historyAfterRevert.get(0).getHash());
        assertEquals(secondCommitContent, currentContent);
        assertTrue(currentContent.contains("description update 2"));

        assertTrue(historyAfterRevert.get(0).getMessage().contains(secondCommit.getHash()));
    }
}
