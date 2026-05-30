package com.example.gitmanager;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import java.io.File;

public class GitManager {
    public static void executePush(String path, String message, String user, String token) throws Exception {
        File repoDir = new File(path);
        try (Git git = Git.open(repoDir)) {
            // เพิ่มไฟล์
            git.add().addFilepattern(".").call();
            // ทำ Commit
            git.commit().setMessage(message).call();
            // ส่งขึ้น GitHub
            git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, token)).call();
        }
    }
}
