package com.tsascanner.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.tsascanner.TsaGame;

/**
 * Desktop launcher.
 * 
 * Run with: ./gradlew lwjgl3:run
 * Build JAR: ./gradlew lwjgl3:jar
 */
public class Lwjgl3Launcher {

    public static void main(String[] args) {
        createApplication();
    }

    private static void createApplication() {
        new Lwjgl3Application(new TsaGame(), getConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getConfiguration() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        
        config.setTitle("TSA Scanner");
        config.setWindowedMode(1280, 720);
        config.useVsync(true);
        config.setForegroundFPS(60);
        
        // Window icon (add your icon to assets/)
        // config.setWindowIcon("icon128.png", "icon64.png", "icon32.png", "icon16.png");
        
        return config;
    }
}
