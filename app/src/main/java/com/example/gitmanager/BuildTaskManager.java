package com.example.gitmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import java.io.File;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class BuildTaskManager {

    public interface BuildListener {
        void onLogAppend(String text, int color);
        void onFinished(boolean success);
    }

    private final Context context;
    private final String projectPath; // รับค่าเป็น Absolute Path
    private final BuildListener listener;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    public BuildTaskManager(Context context, String projectPath, BuildListener listener) {
        this.context = context;
        this.projectPath = projectPath;
        this.listener = listener;
    }

    public void executePush() {
        new Thread(() -> {
            sendProgress("🚀 เริ่มต้นกระบวนการ...", Color.CYAN);

            SharedPreferences prefs = context.getSharedPreferences("GitHubPrefs", Context.MODE_PRIVATE);
            String username = prefs.getString("username", "").trim();
            String email = prefs.getString("email", "").trim(); // ดึง Email ที่บันทึกไว้
            String token = prefs.getString("token", "").trim();

            // ใช้ File จาก Absolute Path โดยตรง
            File projectDir = new File(projectPath);
            
            if (!projectDir.exists()) {
                sendProgress("❌ ไม่พบโฟลเดอร์: " + projectPath, Color.RED);
                return;
            }

            sendProgress("🧹 เตรียมโปรเจกต์...", Color.CYAN);
            BuildEnvironmentManager env = new BuildEnvironmentManager(context);
            env.createGitIgnore(projectDir);
            env.cleanProject(projectDir);

            try {
                Git git;
                if (!new File(projectDir, ".git").exists()) {
                    sendProgress("📁 สร้าง Git Repository ใหม่...", Color.CYAN);
                    git = Git.init().setDirectory(projectDir).call();
                } else {
                    git = Git.open(projectDir);
                }

                StoredConfig config = git.getRepository().getConfig();
                config.setString("user", null, "name", username);
                config.setString("user", null, "email", email); // ตั้งค่า Email ให้ถูกต้อง
                
                String repoName = projectDir.getName();
                String remoteUrl = "https://github.com/" + username + "/" + repoName + ".git";
                config.setString("remote", "origin", "url", remoteUrl);
                config.save();

                sendProgress("📝 กำลัง Commit...", Color.CYAN);
                git.add().addFilepattern(".").call();
                git.commit().setMessage("Update: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).call();

                sendProgress("📤 กำลัง Push...", Color.CYAN);
                git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token)).call();

                sendProgress("✅ สำเร็จ!", Color.GREEN);
                uiHandler.post(() -> listener.onFinished(true));
            } catch (Exception e) {
                sendProgress("❌ Error: " + e.getMessage(), Color.RED);
                uiHandler.post(() -> listener.onFinished(false));
            }
        }).start();
    }

    private void sendProgress(final String text, final int color) {
        uiHandler.post(() -> listener.onLogAppend(text, color));
    }
}
