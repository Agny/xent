package ru.agny.xent

case class MessagePoolConf(
  appId: String,
  bootstrap: String,
  maxRequestSize: Int = 10240,
  maxPollRecords: Int = 100,
  maxPollDuration: Int = 500,
  commitInterval: Int = 1000,
  isAutoCommit: Boolean = false,
  autoOffsetResetConfig: String = "earliest",
  inputTopic: String,
  outputTopic: String
)
