package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

class FileCopyer {
    private static final Logger log = LogManager.getLogger(FileCopyer.class);
    FileCopyer() {
    }

    void copyFiles(String lastversionDirName, String componentDirName) { //Копирование файлов из одной дериктории в другую

        File folder = new File(lastversionDirName);
        File[] listOfFiles = folder.listFiles(File::isFile);
        Path destDir = Paths.get(componentDirName);
        log.info("Copying files....");

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                log.info(file.getName());
                if (!file.getName().endsWith(".ini")) { //Только файлы, т.е. не копируем папки
                    try {
                        Files.copy(file.toPath(), destDir.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}