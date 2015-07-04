package im.tox.hlapi.components

/** The interface implemented by setting keys. */
abstract class SettingKey {
  /** The type of the setting's value */
  type V
  /** The default value */
  val default: V
}
