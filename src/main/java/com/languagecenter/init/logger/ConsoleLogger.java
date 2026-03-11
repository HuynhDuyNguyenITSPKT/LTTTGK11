package com.languagecenter.init.logger;

import com.languagecenter.init.Logger;

public class ConsoleLogger extends Logger {

    @Override
    protected void writeLog(String message) {
        System.out.println("Console Log: " + message);
    }

}