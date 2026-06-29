package com.ultimatelld.problems.logging.appender;

import com.ultimatelld.problems.logging.core.LogMessage;

/**
 * OCP output sink. Console, file, network, async-wrapper — each a separate implementation, composed
 * freely (decorator-style). The logger fans a message out to all configured appenders.
 */
public interface Appender {
    void append(LogMessage message);
}
