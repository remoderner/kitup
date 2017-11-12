package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FileOpener {
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
}
