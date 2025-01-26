package fun.autoclick.core

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.UUID

object Database {
    private const val URL = "jdbc:mysql://localhost:3306/your_database_name?useSSL=false&serverTimezone=UTC" // Substitua com suas credenciais
    private const val USER = "your_username"
    private const val PASSWORD = "your_password"

    private var connection: Connection? = null

    fun connect(): Boolean {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD)
            return true
        } catch (e: SQLException) {
            println("Erro ao conectar ao banco de dados: ${e.message}")
            return false
        }
    }

    fun disconnect() {
        try {
            connection?.close()
        } catch (e: SQLException) {
            println("Erro ao desconectar do banco de dados: ${e.message}")
        }
    }

    fun getConnection(): Connection? = connection

    fun loadPlayerData(uuid: UUID): PlayerData? {
        val sql = "SELECT name, player_group, bans, mutes, is_vanished FROM players WHERE uuid = ?"
        try {
            val statement = connection?.prepareStatement(sql)
            statement?.setString(1, uuid.toString())
            val resultSet = statement?.executeQuery()
            if (resultSet?.next() == true) {
                val name = resultSet.getString("name")
                val groupName = resultSet.getString("player_group")
                val bans = resultSet.getInt("bans")
                val mutes = resultSet.getInt("mutes")
                val isVanished = resultSet.getBoolean("is_vanished")
                val group = Groups.fromName(groupName) ?: Groups.MEMBRO
                return PlayerData(uuid.toString(), name, group, bans, mutes, isVanished)
            }
        } catch (e: SQLException) {
            println("Erro ao carregar dados do jogador: ${e.message}")
        }
        return null
    }

    fun savePlayerData(data: PlayerData) {
        val sql = "INSERT INTO players (uuid, name, player_group, bans, mutes, is_vanished) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name = ?, player_group = ?, bans = ?, mutes = ?, is_vanished = ?"
        try {
            val statement = connection?.prepareStatement(sql)
            statement?.setString(1, data.uuid)
            statement?.setString(2, data.name)
            statement?.setString(3, data.group.name)
            statement?.setInt(4, data.bans)
            statement?.setInt(5, data.mutes)
            statement?.setBoolean(6, data.isVanished)
            statement?.setString(7, data.name)
            statement?.setString(8, data.group.name)
            statement?.setInt(9, data.bans)
            statement?.setInt(10, data.mutes)
            statement?.setBoolean(11, data.isVanished)
            statement?.executeUpdate()
        } catch (e: SQLException) {
            println("Erro ao salvar dados do jogador: ${e.message}")
        }
    }
}
