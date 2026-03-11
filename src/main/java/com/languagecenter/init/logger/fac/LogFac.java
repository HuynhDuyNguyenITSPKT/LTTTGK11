package com.languagecenter.init.logger.fac;

import com.languagecenter.init.Logger;
import com.languagecenter.init.logger.FileLogger;

public interface LogFac {
    public Logger createFileLogger();
}
