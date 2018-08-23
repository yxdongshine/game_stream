package com.game.instance

import com.game.log.Logging

/**
 * Created by YXD on 2018/8/23.
 */
object LogInstance {
  @volatile private var instance: Logging = null
  def getInstance(): Logging = {
    if (instance == null) {
      synchronized {
        if (instance == null) {
          instance = Logging.getLogging("game")
        }
      }
    }
    instance
  }
}
