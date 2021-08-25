package com.cherryleafroad.rust.playground.kargoplay

/*
 * A Kotlin native implementation of Cargo-Play
 * All the features of Cargo-Play. Less of the calling overhead
 */

import com.cherryleafroad.rust.playground.runconfig.toolchain.Edition
import com.cherryleafroad.rust.playground.runconfig.toolchain.RustChannel
import com.cherryleafroad.rust.playground.services.Settings
import com.cherryleafroad.rust.playground.utils.toPath
import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import com.intellij.openapi.vfs.VirtualFile
import org.rust.openapiext.document
import java.io.File
import java.io.PrintWriter
import java.nio.file.Paths
import kotlin.io.path.name


/*
 * Kotlin Cargo Play, thus dubbed "Kargo Play"
 * Created out of a sole need for speed!
 * So slam your foot down on that pedal and GO FAST!
 *
 *              ____----------- _____
 * \~~~~~~~~~~/~_--~~~------~~~~~     \
 *  `---`\  _-~      |                   \
 *    _-~  <_         |                     \[]
 *  / ___     ~~--[""] |      ________-------'_
 * > /~` \    |-.   `\~~.~~~~~                _ ~ - _
 *  ~|  ||\%  |       |    ~  ._                ~ _   ~ ._
 *    `_//|_%  \      |          ~  .              ~-_   /\
 *           `--__     |    _-____  /\               ~-_ \/.
 *                ~--_ /  ,/ -~-_ \ \/          _______---~/
 *                    ~~-/._<   \ \`~~~~~~~~~~~~~     ##--~/
 *                          \    ) |`------##---~~~~-~  ) )
 *                           ~-_/_/                  ~~ ~~
 *
 * - Figure 1 : A Ferrari, a.k.a Ferrari "Kargo" Play
 *
 * Pure fresh cold build times (no intermediaries):
 * Cargo play: 1040ms
 * Cargo run : 738ms
 *
 * Warmed up build times:
 * Cargo play: 698ms
 * Cargo run : 209ms
 *
 * Kargo Play takes a grand total of... Drum roll...
 *           >>>> 33ms! <<<<
 *
 * ... So you can see we can be much faster without
 * calling so many binaries. The kotlin class is already
 * loaded up and hot, meaning less calling overhead (only 1
 * binary, that is, cargo itself). With Cargo Play, we call
 * 2 or 3 binaries (cargo-play, potentially "cp", and
 * of course cargo itself). 1 sounds nicer doesn't it. :)
 */

class KargoPlay(
    private val scratchFile: VirtualFile,
    private val clean: Boolean
) {
    private val settings = Settings.getInstance().scratches[scratchFile.path]
    private var cwd = ScratchFileService.getInstance().getRootPath(ScratchRootType.getInstance())
    private val srcs = settings.srcs.toMutableList().also {
        it.add(0, scratchFile.path.toPath().name)
    }

    private val cargoPlayPath = CargoPlayPath(settings.srcs, cwd)

    // track whether the project changed and needs to be compiled again (cached or not)
    private var needsCompile = false
    private val edition = run {
        val e = settings.edition
        if (e == Edition.DEFAULT) {
            Edition.EDITION_2018.myName
        } else {
            e.myName
        }
    }

    private var cargoOptions = mutableListOf<String>()
    private var kommand = "run"
    private var args = mutableListOf<String>()
    private var directRun = false

    // there's seriously no java/kotlin libraries that can programmatically serialize toml?
    private var cargoToml = """
        [package]
        name = "${cargoPlayPath.projectHash}"
        version = "0.1.0"
        edition = "$edition"

        [dependencies]
    """.trimIndent()

    // returns Cargo Options, command, args, and a cwd
    fun run() {
        // do steps
        if (!clean) {
            cleanProject()
            createCargoProject()
            checkOptions()
            directRun()
            postSetup()
        } else {
            cleanOnly()
        }
    }

    private fun checkOptions() {
        if (settings.toolchain != RustChannel.DEFAULT) {
            cargoOptions.add("+${settings.toolchain.channel}")
        }

        if (settings.quiet) {
            cargoOptions.add("--quiet")
        }

        if (settings.verbose) {
            cargoOptions.add("--verbose")
        }

        if (settings.cargoOptions.isNotEmpty()) {
            settings.cargoOptions.forEach {
                cargoOptions.add(it)
            }
        }

        if (settings.release) {
            args.add("--release")
        }

        if (settings.test) {
            cwd = cargoPlayPath.cargoPlayDir
            kommand = "test"
            return
        }

        if (settings.check) {
            cwd = cargoPlayPath.cargoPlayDir
            kommand = "check"
            return
        }

        if (settings.expand) {
            cwd = cargoPlayPath.cargoPlayDir
            kommand = "expand"
            return
        }

        if (settings.args.isNotEmpty()) {
            args.add("--")
            settings.args.forEach {
                args.add(it)
            }
        }

        if (settings.mode.isNotBlank()) {
            kommand = settings.mode
            return
        }
    }

    private fun postSetup() {
        settings.kommand = kommand
        settings.cargoOptions = cargoOptions
        settings.args = args
        settings.workingDirectory = cargoPlayPath.cargoPlayDir
        settings.directRun = directRun
    }

    private fun directRun() {
        // this is a special case here
        // check whether we can run it cached or not (faster)
        if (!needsCompile) {
            val target = if (settings.release) cargoPlayPath.releaseTarget else cargoPlayPath.debugTarget
            kommand = target
            directRun = true

            // "--" arg is now invalid
            if (args.isNotEmpty()) {
                args.removeAt(0)
            }
        }
    }

    private fun cleanOnly() {
        val target = File(cargoPlayPath.targetDir)
        if (target.exists()) {
            kommand = "clean"
        }
    }

    private fun cleanProject() {
        // remove target folder
        if (settings.clean) {
            val target = File(cargoPlayPath.targetDir)
            if (target.exists()) {
                needsCompile = true
                target.deleteRecursively()
            }
        }
    }

    @Suppress("LiftReturnOrAssignment")
    private fun createCargoProject() {
        val dir = File(cargoPlayPath.cargoPlayDir)

        // make temp directory
        if (!dir.exists()) {
            needsCompile = true
            dir.mkdir()
        }

        // currently missing infer functionality
        // disabled checkbox in ui. only does normal dependency add
        addDependencies()

        // compare cargo toml to see if it changed or not
        if (!needsCompile) {
            val cargoManifest = File(cargoPlayPath.cargoManifest)
            if (cargoManifest.exists()) {
                needsCompile = cargoManifest.readText().hashCode() != cargoToml.hashCode()
            } else {
                needsCompile = true
            }
        }

        // create or overwrite cargo manifest and write the cargo TOML
        if (needsCompile) {
            PrintWriter(cargoPlayPath.cargoManifest).use { it.print(cargoToml) }
        }

        val sourcesDir = File(cargoPlayPath.srcDir)
        if (!sourcesDir.exists()) {
            needsCompile = true
            sourcesDir.mkdir()
            copyFilesOver(true)
        } else {
            copyFilesOver(false)
        }
    }

    private fun copyFilesOver(empty: Boolean = false) {
        if (!empty) {
            val srcFiles = Paths.get(cargoPlayPath.srcDir).toFile().listFiles()?.toMutableList()
            if (srcFiles != null) {
                if (srcFiles.size != srcs.size) {
                    needsCompile = true
                } else {
                    val mainFile = srcFiles.find { it.name == "main.rs" }
                    if (mainFile != null && !needsCompile) {
                        // sort them in a regular order we know and can compare to
                        srcFiles.remove(mainFile)
                        srcFiles.sort()
                        srcFiles.add(0, mainFile)

                        // sort input list to be the same
                        val mainInputFile = srcs[0]
                        val sortedInputSrcs = srcs.toMutableList()
                        sortedInputSrcs.remove(mainInputFile)
                        sortedInputSrcs.sort()
                        sortedInputSrcs.add(0, mainInputFile)

                        // compare all file hashes inside
                        run run@ {
                            sortedInputSrcs.forEachIndexed { i, it ->
                                val file = Paths.get(cwd, it).toFile()
                                // file contents different
                                needsCompile = file.readText().hashCode() != srcFiles[i].readText().hashCode()

                                if (!needsCompile && i > 0) {
                                    // file names are different, definitely need recompile
                                    needsCompile = file.name != srcFiles[i].name
                                }

                                if (needsCompile) return@run
                            }
                        }
                    } else {
                        needsCompile = true
                    }
                }

                if (needsCompile) {
                    // remove all src files so we can copy them over
                    srcFiles.forEach {
                        it.delete()
                    }
                } else {
                    // all files were the same, so skip deleting and copying
                    return
                }
            } else {
                needsCompile = true
            }
        }

        srcs.forEachIndexed { i, it ->
            var targetName = it
            // first file is always main.rs
            if (i == 0) targetName = "main.rs"

            val file = Paths.get(cwd, it).toFile()
            val targetFile = Paths.get(cargoPlayPath.srcDir, targetName).toFile()
            file.copyTo(targetFile)
        }
    }

    private fun addDependencies() {
        scratchFile.document?.let { doc ->
            run beg@ {
                doc.text.lines().forEach {
                    // skip these lines at beginning (just like original program)
                    if (it.isEmpty() || it.startsWith("#!")) {
                        return@forEach
                    }

                    // only take //# if it's next
                    if (it.startsWith("//#")) {
                        // this really should parse the toml, but not sure if there's a kotlin lib for that
                        cargoToml += "\n${it.substring(3).trim()}"
                    }

                    if (!it.startsWith("//#")) {
                        return@beg
                    }
                }
            }

            // one last newline at the end
            cargoToml += "\n"
        }
    }
}
