package xyz.r2turntrue.tripleworld

import io.github.monun.kommand.getValue
import io.github.monun.kommand.kommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class TripleWorld: JavaPlugin() {

    val worldList = arrayListOf<String>()

    fun loadWorld(name: String): Boolean = WorldCreator(name)
        .environment(World.Environment.NORMAL)
        .createWorld() != null

    override fun onEnable() {

        if(config.isList("worlds")) {
            for(world in config.getList("worlds")!!) {
                worldList.add(world as String)
            }
        }

        kommand {
            register("move") {
                requires { isPlayer && isOp }
                then("world" to dimension()) {
                    executes { ctx ->
                        val world: World by ctx

                        player.teleport(world.spawnLocation)
                    }
                }
            }
            register("spawn") {
                requires { isPlayer && isOp }
                executes {
                    player.teleport(player.world.spawnLocation)
                }
                then("set") {
                    executes { ctx ->
                        player.world.spawnLocation = player.location
                    }
                }
            }
            register("import") {
                requires { isOp }
                then("world" to string()) {
                    executes { ctx ->
                        val world: String by ctx

                        if(worldList.contains(world)) {
                            sender.sendMessage(Component.text("월드가 이미 존재합니다!", NamedTextColor.RED))
                            return@executes
                        }

                        val worldFile = File(server.worldContainer, world)

                        if(!worldFile.exists()) {
                            sender.sendMessage(Component.text("월드 파일이 존재하지 않습니다!", NamedTextColor.RED))
                            return@executes
                        }

                        if(loadWorld(world)) {
                            sender.sendMessage(Component.text("월드 임포트가 성공되었습니다!", NamedTextColor.GREEN))
                        } else {
                            sender.sendMessage(Component.text("실패!", NamedTextColor.RED))
                        }

                        worldList.add(world)
                        config.set("worlds", worldList)
                        saveConfig()
                    }
                }
            }
        }

        for(world in worldList) {
            val worldFile = File(server.worldContainer, world)

            if(!worldFile.exists()) continue

            if(loadWorld(world)) {
                println("Success to load: $world")
            } else {
                println("Failed to load: $world")
            }
        }
    }

}