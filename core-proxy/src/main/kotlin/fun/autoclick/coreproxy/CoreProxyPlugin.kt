package fun.autoclick.coreproxy

import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import fun.autoclick.core.Groups
import fun.autoclick.core.Database
import fun.autoclick.core.PlayerData
import java.util.UUID

class CoreProxyPlugin : Plugin() {
    private val playerDataCache = mutableMapOf<UUID, PlayerData>()

    override fun onEnable() {
        logger.info("CoreProxyPlugin enabled!")
        if (Database.connect()) {
            logger.info("Conectado ao banco de dados!")
            // Registrar comandos aqui (exemplo /sc, /report)
            proxy.pluginManager.registerCommand(StaffChatCommand(this))
        } else {
            logger.severe("Falha na conexão com o banco de dados. O plugin não funcionará corretamente.")
        }
    }

    override fun onDisable() {
        Database.disconnect()
        logger.info("CoreProxyPlugin disabled!")
    }

    fun getPlayerData(uuid: UUID): PlayerData? = playerDataCache[uuid]

    fun loadPlayerData(uuid: UUID): PlayerData? {
        val playerData = Database.loadPlayerData(UUID.fromString(uuid.toString()))
        playerDataCache[uuid] = playerData ?: PlayerData(uuid.toString(), "Unknown", Groups.MEMBRO)
        return playerData
    }
}

class StaffChatCommand(private val plugin: CoreProxyPlugin) : Command("sc") {
    private val toggledPlayers = mutableSetOf<UUID>()

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (sender !is ProxiedPlayer) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.")
            return
        }
        val player = sender
        val uuid = player.uniqueId
        val playerData = plugin.loadPlayerData(uuid) ?: return

        if (args.isEmpty()) {
            sender.sendMessage("Uso: /sc <mensagem> ou /sc toggle")
            return
        }

        if (args[0].equals("toggle", ignoreCase = true)) {
            if (toggledPlayers.contains(uuid)) {
                toggledPlayers.remove(uuid)
                sender.sendMessage("Staff chat desativado.")
            } else {
                toggledPlayers.add(uuid)
                sender.sendMessage("Staff chat ativado.")
            }
            return
        }

        if (playerData.group.permissionLevel < Groups.MOD.permissionLevel) {
            sender.sendMessage("Você não tem permissão para usar o staff chat.")
            return
        }

        val message = args.joinToString(" ")
        val formattedMessage = "§a[SC] ${playerData.group.color}${playerData.group.tag} ${player.name}: §f$message"

        for (onlinePlayer in plugin.proxy.players) {
            val onlinePlayerData = plugin.loadPlayerData(onlinePlayer.uniqueId) ?: continue
            if (onlinePlayerData.group.permissionLevel >= Groups.MOD.permissionLevel && !toggledPlayers.contains(onlinePlayer.uniqueId)) {
                onlinePlayer.sendMessage(formattedMessage)
            }
        }
    }
}
