package com.languagecenter.init.logger.fac;

import com.languagecenter.init.Logger;
import com.languagecenter.init.logger.FileLogger;

public class FileLogFac implements LogFac {
    @Override
    public Logger createFileLogger() { return new  FileLogger(); };
}
