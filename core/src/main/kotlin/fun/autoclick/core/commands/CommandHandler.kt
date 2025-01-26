package fun.autoclick.core.commands

interface CommandHandler {
  fun execute(sender: Any, args: Array<String>)
}
