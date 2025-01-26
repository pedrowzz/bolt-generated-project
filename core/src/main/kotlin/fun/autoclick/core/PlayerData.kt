package fun.autoclick.core

data class PlayerData(val uuid: String, val name: String, val group: Groups, val bans: Int = 0, val mutes: Int = 0, val isVanished: Boolean = false)
