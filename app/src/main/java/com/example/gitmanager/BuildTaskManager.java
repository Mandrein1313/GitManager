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
    private final String projectPath;
    private final BuildListener listener;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    public BuildTaskManager(Context context, String projectPath, BuildListener listener) {
        this.context = context;
        this.projectPath = projectPath;
        this.listener = listener;
    }

    public void executePush() {
        new Thread(() -> {
            SharedPreferences prefs = context.getSharedPreferences("GitHubPrefs", Context.MODE_PRIVATE);
            String username = prefs.getString("username", "").trim();
            String email = prefs.getString("email", "").trim();
            String token = prefs.getString("token", "").trim();

            if (username.isEmpty() || token.isEmpty()) {
                sendProgress("❌ ข้อมูล GitHub ไม่ครบถ้วน", Color.RED);
                return;
            }

            File projectDir = new File(projectPath);
            if (!projectDir.exists()) {
                sendProgress("❌ ไม่พบโฟลเดอร์โปรเจกต์", Color.RED);
                return;
            }

            try {
                sendProgress("🚀 เริ่มต้นกระบวนการ...", Color.CYAN);
                Git git = !new File(projectDir, ".git").exists() ? Git.init().setDirectory(projectDir).call() : Git.open(projectDir);

                StoredConfig config = git.getRepository().getConfig();
                config.setString("user", null, "name", username);
                config.setString("user", null, "email", email);
                config.setString("remote", "origin", "url", "https://github.com/" + username + "/" + projectDir.getName() + ".git");
                config.save();

                sendProgress("📝 กำลัง Commit...", Color.CYAN);
                git.add().addFilepattern(".").call();
                try {
                    git.commit().setMessage("Update: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).call();
                } catch (Exception e) {
                    sendProgress("⚠️ ไม่มีการเปลี่ยนแปลงไฟล์", Color.YELLOW);
                }

                sendProgress("📤 กำลัง Push...", Color.CYAN);
                git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token)).call();

                sendProgress("✅ สำเร็จ!", Color.GREEN);
                uiHandler.post(() -> listener.onFinished(true));
            } catch (Exception e) {
                sendProgress("❌ Error: " + e.getLocalizedMessage(), Color.RED);
                uiHandler.post(() -> listener.onFinished(false));
            }
        }).start();
    }

    private void sendProgress(final String text, final int color) {
        uiHandler.post(() -> listener.onLogAppend(text, color));
    }
}
