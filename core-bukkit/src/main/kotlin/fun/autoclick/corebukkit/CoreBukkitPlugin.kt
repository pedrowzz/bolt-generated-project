package fun.autoclick.corebukkit

import org.bukkit.plugin.java.JavaPlugin
import fun.autoclick.core.Groups
import fun.autoclick.core.Database
import fun.autoclick.core.PlayerData
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

class CoreBukkitPlugin : JavaPlugin(), Listener {
    private val playerDataCache = mutableMapOf<UUID, PlayerData>()

    override fun onEnable() {
        logger.info("CoreBukkitPlugin enabled!")
        if (Database.connect()) {
            logger.info("Conectado ao banco de dados!")
            // Registrar comandos aqui (exemplo /acc, /v)
            getCommand("acc")?.setExecutor(AccCommand(this))
            getCommand("v")?.setExecutor(VCommand(this))
            server.pluginManager.registerEvents(this, this)
        } else {
            logger.severe("Falha na conexão com o banco de dados. O plugin não funcionará corretamente.")
        }
    }

    override fun onDisable() {
        Database.disconnect()
        logger.info("CoreBukkitPlugin disabled!")
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val playerData = Database.loadPlayerData(uuid) ?: PlayerData(uuid.toString(), player.name, Groups.MEMBRO)
        playerDataCache[uuid] = playerData
        Database.savePlayerData(playerData)
        if (playerData.isVanished && playerData.group.permissionLevel >= Groups.MOD.permissionLevel) {
            VCommand(this).setVanished(player, true)
        }
        updatePlayerPrefix(player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        val uuid = player.uniqueId
        playerDataCache.remove(uuid)
    }

    fun getPlayerData(uuid: UUID): PlayerData? = playerDataCache[uuid]

    fun updatePlayerPrefix(player: Player) {
        val playerData = getPlayerData(player.uniqueId) ?: return
        val prefix = when (playerData.group) {
            Groups.ADMIN -> "§4§lADMIN "
            Groups.MOD -> "§5§lMOD "
            Groups.VIP -> "§6§lVIP "
            else -> "§7"
        }
        player.setDisplayName(prefix + player.name + "§r")
    }
}

class AccCommand(private val plugin: CoreBukkitPlugin) : org.bukkit.command.CommandExecutor {
    override fun onCommand(sender: org.bukkit.command.CommandSender, command: org.bukkit.command.Command, label: String, args: Array<String>): Boolean {
        if (args.size < 3) {
            sender.sendMessage("Uso: /acc <jogador> <add|remove> <grupo>")
            return true
        }
        val playerName = args[0]
        val action = args[1]
        val groupName = args[2]

        val group = Groups.fromName(groupName) ?: run {
            sender.sendMessage("Grupo inválido.")
            return true
        }

        val target = plugin.server.getPlayer(playerName) ?: run {
            sender.sendMessage("Jogador offline.")
            return true
        }

        val playerData = plugin.getPlayerData(target.uniqueId) ?: run {
            sender.sendMessage("Dados do jogador não encontrados.")
            return true
        }

        if (action.equals("add", ignoreCase = true)) {
            val updatedData = playerData.copy(group = group)
            plugin.playerDataCache[target.uniqueId] = updatedData
            Database.savePlayerData(updatedData)
            plugin.updatePlayerPrefix(target)
            sender.sendMessage("Grupo do jogador ${target.name} alterado para ${group.name}.")
        } else if (action.equals("remove", ignoreCase = true)) {
            val updatedData = playerData.copy(group = Groups.MEMBRO)
            plugin.playerDataCache[target.uniqueId] = updatedData
            Database.savePlayerData(updatedData)
            plugin.updatePlayerPrefix(target)
            sender.sendMessage("Grupo do jogador ${target.name} removido.")
        } else {
            sender.sendMessage("Ação inválida. Use 'add' ou 'remove'.")
        }
        return true
    }
}

class VCommand(private val plugin: CoreBukkitPlugin) : org.bukkit.command.CommandExecutor {
    override fun onCommand(sender: org.bukkit.command.CommandSender, command: org.bukkit.command.Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Este comando só pode ser usado por jogadores.")
            return true
        }
        val player = sender
        val playerData = plugin.getPlayerData(player.uniqueId) ?: return true
        if (playerData.group.permissionLevel < Groups.MOD.permissionLevel) {
            player.sendMessage("Você não tem permissão para usar este comando.")
            return true
        }
        setVanished(player, !playerData.isVanished)
        return true
    }

    fun setVanished(player: Player, vanished: Boolean) {
        val playerData = plugin.getPlayerData(player.uniqueId) ?: return
        val updatedData = playerData.copy(isVanished = vanished)
        plugin.playerDataCache[player.uniqueId] = updatedData
        Database.savePlayerData(updatedData)
        if (vanished) {
            player.sendMessage("Você está agora invisível.")
            player.inventory.clear()
            player.gameMode = org.bukkit.GameMode.CREATIVE
            for (onlinePlayer in plugin.server.onlinePlayers) {
                if (onlinePlayer != player) {
                    onlinePlayer.hidePlayer(player)
                }
            }
        } else {
            player.sendMessage("Você está agora visível.")
            player.gameMode = org.bukkit.GameMode.SURVIVAL
            for (onlinePlayer in plugin.server.onlinePlayers) {
                if (onlinePlayer != player) {
                    onlinePlayer.showPlayer(player)
                }
            }
        }
    }
}
