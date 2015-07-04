package im.tox.hlapi.components

import org.joda.time.Period

sealed trait SyncConfig extends SettingKey

final case object syncPeriod extends SyncConfig {
  type V = org.joda.time.Period
  val default = Period.minutes(10)
}

final case object syncWhenMobile extends SyncConfig {
  type V = Boolean
  val default = false
}
