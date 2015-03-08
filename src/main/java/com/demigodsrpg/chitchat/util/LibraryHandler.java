package com.demigodsrpg.chitchat.util;

import org.bukkit.plugin.Plugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LibraryHandler {
    // -- IMPORTANT FIELDS -- //

    public static final String MAVEN_CENTRAL = "http://central.maven.org/maven2/";
    private static final int BYTE_SIZE = 1024;

    private final List<String> FILE_NAMES;
    private final Plugin PLUGIN;

    // -- CONSTRUCTOR -- //

    public LibraryHandler(Plugin plugin) {
        this.PLUGIN = plugin;
        FILE_NAMES = new ArrayList<>();
        checkDirectory();
    }

    // -- HELPER METHODS -- //

    public void addMavenLibrary(String repo, String groupId, String artifactId, String version) {
        try {
            String fileName = artifactId + "-" + version + ".jar";
            loadLibrary(fileName, new URI(repo + groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/" + fileName).toURL());
        } catch (Exception oops) {
            oops.printStackTrace();
        }
    }

    public void checkDirectory() {
        // Get the file
        File libraryDirectory = new File(PLUGIN.getDataFolder().getPath() + "/lib");

        // If it exists and isn't a directory, throw an error
        if (libraryDirectory.exists() && !libraryDirectory.isDirectory()) {
            PLUGIN.getLogger().severe("The library directory isn't a directory!");
            return;
        }
        // Otherwise, make the directory
        else if (!libraryDirectory.exists()) {
            libraryDirectory.mkdirs();
        }

        // Check if all libraries exist

        File[] filesArray = libraryDirectory.listFiles();
        List<File> files = Arrays.asList(filesArray != null ? filesArray : new File[]{});

        for (File file : files) {
            if (file.getName().endsWith(".jar")) {
                FILE_NAMES.add(file.getName());
            }
        }
    }

    public void loadLibrary(String fileName, URL url) {
        // Get the file
        File libraryDirectory = new File(PLUGIN.getDataFolder().getPath() + "/lib");


        // Check if the files are found or not
        File libraryFile = null;
        if (FILE_NAMES.contains(fileName)) {
            libraryFile = new File(libraryDirectory + "/" + fileName);
        }

        // If they aren't found, download them
        if (libraryFile == null) {
            PLUGIN.getLogger().warning(fileName + " is missing, downloading now.");
            libraryFile = downloadLibrary(libraryDirectory, fileName, url);
        }

        // Add the library to the classpath
        addToClasspath(libraryFile);
    }

    public void addToClasspath(File file) {
        try {
            ClassPathHack.addFile(file);
        } catch (Exception oops) {
            PLUGIN.getLogger().severe("Couldn't load " + (file != null ? file.getName() : "a required library") + ", this may cause problems.");
            oops.printStackTrace();
        }
    }

    public File downloadLibrary(File libraryDirectory, String libraryFileName, URL libraryUrl) {
        // Get the file
        File libraryFile = new File(libraryDirectory.getPath() + "/" + libraryFileName);

        // Create the streams
        BufferedInputStream in = null;
        FileOutputStream fout = null;

        try {
            // Setup the streams
            in = new BufferedInputStream(libraryUrl.openStream());
            fout = new FileOutputStream(libraryFile);

            // Create variables for loop
            final byte[] data = new byte[BYTE_SIZE];
            int count;

            // Write the data to the file
            while ((count = in.read(data, 0, BYTE_SIZE)) != -1) {
                fout.write(data, 0, count);
            }

            PLUGIN.getLogger().info("Download complete.");

            // Return the file
            return libraryFile;
        } catch (final Exception oops) {
            // Couldn't download the file
            PLUGIN.getLogger().severe("Download could not complete");
        } finally {
            // Close the streams
            try {
                if (in != null) {
                    in.close();
                }
                if (fout != null) {
                    fout.close();
                }
            } catch (final Exception ignored) {
            }
        }

        return null;
    }
}
