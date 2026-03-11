package com.languagecenter.init.logger.fac;

import com.languagecenter.init.Logger;
import com.languagecenter.init.logger.FileLogger;

public class LogFacSer {
    public static Logger getFileLogger(LogFac logger) {return logger.createFileLogger();}
}
