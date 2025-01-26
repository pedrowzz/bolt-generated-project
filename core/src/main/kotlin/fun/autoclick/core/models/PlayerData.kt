package fun.autoclick.core.models

data class PlayerData(
  val uuid: String,
  val name: String,
  val group: String,
  val bans: Int,
  val mutes: Int,
  val isStaff: Boolean
)
