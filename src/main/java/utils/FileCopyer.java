package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

class FileCopyer {
    private static Logger log = Logger.getLogger(ComponentOperator.class.getName());

    FileCopyer() {
    }

    void copyFiles(String lastversionDirName, String componentDirName) { //Копирование файлов из одной дериктории в другую

        File folder = new File(lastversionDirName);
        File[] listOfFiles = folder.listFiles(File::isFile);
        Path destDir = Paths.get(componentDirName);
        System.out.println("Copying files....");
        log.info("Copying files....");

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                System.out.println(file.getName());
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