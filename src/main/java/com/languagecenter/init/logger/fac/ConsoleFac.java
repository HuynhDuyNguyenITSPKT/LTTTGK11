package com.languagecenter.init.logger.fac;

import com.languagecenter.init.Logger;
import com.languagecenter.init.logger.ConsoleLogger;
import com.languagecenter.init.logger.FileLogger;

public class ConsoleFac implements LogFac {
    @Override
    public Logger createFileLogger() {return new ConsoleLogger(); };
}
