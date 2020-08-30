package ru.agny.xent

import org.slf4j.{LoggerFactory, Marker, Logger => Underlying}

trait LazyLoggingDotty {

  import LazyLoggingDotty._

  protected val logger: Logger = Logger(LoggerFactory.getLogger(getClass.getName))
}

object LazyLoggingDotty {
  class Logger(underlying: Underlying) {
    inline def info(msg: => String): Unit = underlying.info(msg)

    inline def error(msg: => String): Unit = underlying.error(msg)

    inline def error(msg: => String, e: Throwable): Unit = underlying.error(msg, e)

    inline def debug(msg: => String): Unit = {
      if (underlying.isDebugEnabled) underlying.debug(msg)
    }
  }
}