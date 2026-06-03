package com.example.gitmanager;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BuildEnvironmentManager {
    private final Context context;

    public BuildEnvironmentManager(Context context) { this.context = context; }

    public void createGitIgnore(File projectDir) {
        try {
            File gitIgnoreFile = new File(projectDir, ".gitignore");
            String content = ".gradle/\nbuild/\napp/build/\n*.iml\nlocal.properties\n.idea/\n";
            try (FileOutputStream fos = new FileOutputStream(gitIgnoreFile)) {
                fos.write(content.getBytes("UTF-8"));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void cleanProject(File projectDir) {
        deleteDirectory(new File(projectDir, "build"));
        deleteDirectory(new File(projectDir, "app/build"));
    }

    private void deleteDirectory(File file) {
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) for (File child : files) deleteDirectory(child);
            file.delete();
        }
    }
}
