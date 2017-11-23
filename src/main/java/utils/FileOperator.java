package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class FileOperator {
    private static final Logger log = LogManager.getLogger(ComponentOperator.class);

    public void openFile(String link) {
        log.info(link);
        File fileToOpen = new File(link);

        if (fileToOpen.exists()) {
            try {
                Desktop.getDesktop().open(fileToOpen);
            } catch (IOException e) {
                log.warn("WARNING: " + e.toString());
            }
        } else {
            log.info("File does not exist");
        }
    }

    public void openDir(String link) {
        log.info(link);
        File fileToOpen = new File(link);

        if (fileToOpen.exists()) {
            try {
                Desktop.getDesktop().open(fileToOpen);
            } catch (IOException e) {
                log.warn("WARNING: " + e.toString());
            }
        } else {
            log.info("Directory does not exist");
        }
    }

    void deleteFile(String link) {
        log.info(link);
        File fileToDelete = new File(link);

        while (fileToDelete.exists()) {
            if (!fileToDelete.delete()) {
                log.info(fileToDelete + " can't deleted, file is lock!");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    log.warn("WARNING: " + e.toString());
                }
            }
        }
    }

    ArrayList<String> findFiles(String sourceDirName) {
        File folder = new File(sourceDirName);
        File[] listOfFiles = folder.listFiles(File::isFile);
        ArrayList<String> alreadyCopiedFiles = new ArrayList<>();
        log.info("Finding files...");
        log.info("FROM: " + sourceDirName);

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (!file.getName().endsWith(".ini")) { //Только файлы, т.е. не копируем папки
                    alreadyCopiedFiles.add(file.getName());
                }
            }
        }

        return alreadyCopiedFiles;
    }

    void copyFiles(String sourceDirName, String componentDirName, ArrayList<String> alreadyCopiedFiles) { //Копирование файлов из одной дериктории в другую
        File folder = new File(sourceDirName);
        File[] listOfFiles = folder.listFiles(File::isFile);
        Path destDir = Paths.get(componentDirName);
        log.info("Copying files...");
        log.info("FROM: " + sourceDirName);
        log.info("TO: " + componentDirName);

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (!file.getName().endsWith(".ini")) { //Только файлы, т.е. не копируем папки
                    if (alreadyCopiedFiles != null && alreadyCopiedFiles.contains(file.getName())) {
                        continue;
                    }

                    log.info(file.getName());
                    try {
                        Files.copy(file.toPath(), destDir.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        log.warn("WARNING: " + e.toString());
                    }
                }
            }
        }
    }
}
