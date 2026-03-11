package com.languagecenter.init.logger;

import com.languagecenter.init.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileLogger extends Logger {

    @Override
    protected void writeLog(String message) {

        try {

            File folder = new File("src/main/resources/log");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            FileWriter writer = new FileWriter("src/main/resources/log/log.txt", true);
            writer.write(message + "\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
