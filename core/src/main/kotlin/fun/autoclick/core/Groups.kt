package fun.autoclick.core

enum class Groups(val tag: String, val color: String, val aliases: Array<String> = emptyArray(), val permissionLevel: Int) {
    MEMBRO("Membro", "§7", arrayOf("Default"), 0),
    VIP("VIP", "§6", emptyArray(), 1),
    MOD("Mod", "§5", arrayOf("moderador"), 2),
    ADMIN("Admin", "§4", arrayOf("adm", "Administrador"), 3),
    DONO("Dono", "§c", arrayOf("Owner"), 4);

    companion object {
        fun fromName(name: String): Groups? = values().find { it.name.equals(name, ignoreCase = true) || it.aliases.any { alias -> alias.equals(name, ignoreCase = true) } }
    }
}
