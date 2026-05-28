package com.example.gitmanager;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import java.io.File;

public class GitManager {
    
    // ใส่ Token ของคุณที่นี่ (หรือรับค่าจากช่อง Input)
    private static final String GITHUB_TOKEN = "YOUR_PERSONAL_ACCESS_TOKEN";
    private static final String GITHUB_USER = "YOUR_USERNAME";

    public static void executePush(String path, String message) throws Exception {
        File repoDir = new File(path);
        
        // เปิดโปรเจกต์ Git
        try (Git git = Git.open(repoDir)) {
            
            // 1. Git Add .
            git.add().addFilepattern(".").call();
            
            // 2. Git Commit
            git.commit().setMessage(message).call();
            
            // 3. Git Push
            git.push()
               .setCredentialsProvider(new UsernamePasswordCredentialsProvider(GITHUB_USER, GITHUB_TOKEN))
               .call();
        }
    }
}
