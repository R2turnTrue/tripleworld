package xyz.r2turntrue.tripleworld

import io.github.monun.kommand.getValue
import io.github.monun.kommand.kommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
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
            register("clone") {
                requires { isOp }
                then("old" to dimension()) {
                    then("new" to string()) {
                        executes { ctx ->
                            val old: World by ctx
                            val new: String by ctx
                            val oldWorldFile = File(server.worldContainer, old.name)
                            val newWorldFile = File(server.worldContainer, new)

                            if(!oldWorldFile.exists() || newWorldFile.exists()) {
                                sender.sendMessage(Component.text("이미 월드가 존재하거나 기존 월드가 존재하지 않습니다!", NamedTextColor.RED))
                                return@executes
                            }

                            if(!FileUtils.copyFolder(oldWorldFile, newWorldFile, listOf("session.lock", "uid.dat"))) {
                                sender.sendMessage(Component.text("파일을 복사하는 도중 오류가 발생했습니다!", NamedTextColor.RED))
                                return@executes
                            }

                            if(loadWorld(new)) {
                                sender.sendMessage(Component.text("월드 임포트가 성공적으로 완료되었습니다!", NamedTextColor.GREEN))
                                worldList.add(new)
                                config.set("worlds", worldList)
                                saveConfig()
                            } else {
                                sender.sendMessage(Component.text("실패!", NamedTextColor.RED))
                            }
                        }
                    }
                }
            }
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
                            sender.sendMessage(Component.text("월드 복제가 성공적으로 완료되었습니다!", NamedTextColor.GREEN))
                            worldList.add(world)
                            config.set("worlds", worldList)
                            saveConfig()
                        } else {
                            sender.sendMessage(Component.text("실패!", NamedTextColor.RED))
                        }
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