package utils;

import classroom.KitupConfig;
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
    private KitupConfig kitupConfig = KitupConfig.getKitupConfig();

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
                alreadyCopiedFiles.add(file.getName());
            }
        }

        return alreadyCopiedFiles;
    }

    void copyFiles(String sourceDirName, String targetDirName, ArrayList<String> alreadyCopiedFiles, Boolean isRollback) { //Копирование файлов из одной дериктории в другую
        File folder = new File(sourceDirName);
        File[] listOfFiles = folder.listFiles(File::isFile);

        ArrayList<String> ignoredCopiedFiles;
        if (alreadyCopiedFiles == null) {
            ignoredCopiedFiles = new ArrayList<>();
        } else {
            ignoredCopiedFiles = alreadyCopiedFiles;
        }

        Path destDir = Paths.get(targetDirName);
        log.info("Copying files...");
        log.info("FROM: " + sourceDirName);
        log.info("TO: " + targetDirName);

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (isRollback) {
                    for (int i = 0; i < kitupConfig.getIgnoreByFileNameListOnRollback().size(); i++) {
                        if (file.getName().equals(kitupConfig.getIgnoreByFileNameListOnRollback().get(i))) {
                            ignoredCopiedFiles.add(file.getName());
                            break;
                        }
                    }

                    for (int i = 0; i < kitupConfig.getIgnoreByFileTypeListOnRollback().size(); i++) {
                        if (file.getName().endsWith("." + kitupConfig.getIgnoreByFileTypeListOnRollback().get(i))) {
                            ignoredCopiedFiles.add(file.getName());
                            break;
                        }
                    }
                } else {
                    for (int i = 0; i < kitupConfig.getIgnoreByFileNameListOnUpdate().size(); i++) {
                        if (file.getName().equals(kitupConfig.getIgnoreByFileNameListOnUpdate().get(i))) {
                            ignoredCopiedFiles.add(file.getName());
                            break;
                        }
                    }

                    for (int i = 0; i < kitupConfig.getIgnoreByFileTypeListOnUpdate().size(); i++) {
                        if (file.getName().endsWith("." + kitupConfig.getIgnoreByFileTypeListOnUpdate().get(i))) {
                            ignoredCopiedFiles.add(file.getName());
                            break;
                        }
                    }
                }

                if (ignoredCopiedFiles.contains(file.getName())) {
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

    void renameFiles(String sourceDirName, String targetDirName, String exeNameQortes, String exeNameQortesDB) {
        String qortesExeLink = sourceDirName + "qortes.exe";
        String qortesDBExeLink = sourceDirName + "qortesDB.exe";

        String qortesExeNewName = targetDirName + exeNameQortes;
        String qortesDBExeNewName = targetDirName + exeNameQortesDB;

        Boolean QortesExeRename = new File(qortesExeLink).renameTo(new File(qortesExeNewName));
        Boolean QortesDBExeRename = new File(qortesDBExeLink).renameTo(new File(qortesDBExeNewName));

        log.debug("QortesExeRename: " + QortesExeRename);
        log.debug("QortesDBExeRename: " + QortesDBExeRename);
    }
}
