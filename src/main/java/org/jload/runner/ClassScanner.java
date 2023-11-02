package org.jload.runner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassScanner {
    private static final Logger logger = LoggerFactory.getLogger(ClassScanner.class);
    private static final String filePath = "src/main/java/org/jload/";
    private static final File file = new File(filePath + "jLoadFile.java");
    private static final String fileForClass = "org.jload.";
    public static List<String> getDeclaredClasses() {
        logger.info("Running in : {}",System.getProperty("user.dir"));

        List<String> classNames = new ArrayList<>();

        try {
            FileInputStream inputStream = new FileInputStream(file);
            Scanner scanner = new Scanner(inputStream);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line.contains("class") && line.contains("extends User")) {
                    String className = line.substring(line.indexOf("class") + 6, line.indexOf("extends User")).trim();
                    classNames.add(className);
                }
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            logger.error("File not found: {}", e.getMessage(), e);
        }
        logger.info("Users: {}", classNames);
        return classNames;
    }

    public static List<Class<?>> getClasses(){
        List<Class<?>> classes = new ArrayList<>();
        List<String> declaredClasses = getDeclaredClasses();

        if (declaredClasses.isEmpty()) {
            logger.error("No classes found in the file.");
        } else {
            for (String className : declaredClasses) {
                try {
                    Class<?> cls = Class.forName(fileForClass + className);
                    classes.add(cls);
                } catch (Exception e) {
                    logger.error("Error getClass {}: {}", className, e.getMessage(), e);
                }
            }
        }
        return classes;
    }
}
