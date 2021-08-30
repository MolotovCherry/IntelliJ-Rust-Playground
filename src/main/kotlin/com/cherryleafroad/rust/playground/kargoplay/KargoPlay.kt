package com.cherryleafroad.rust.playground.kargoplay

/*
 * A Kotlin native implementation of Cargo-Play
 * All the features of Cargo-Play. Less of the calling overhead
 */

import com.cherryleafroad.rust.playground.runconfig.toolchain.Edition
import com.cherryleafroad.rust.playground.runconfig.toolchain.RustChannel
import com.cherryleafroad.rust.playground.services.Settings
import com.cherryleafroad.rust.playground.settings.ScratchConfiguration
import com.cherryleafroad.rust.playground.utils.toFile
import org.rust.openapiext.document
import java.io.File
import java.io.PrintWriter
import java.nio.file.Paths


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
 * - Figure 1 : A Ferrari, a.k.a. Ferrari "Kargo" Play
 *
 * Pure fresh cold build times (no intermediaries):
 * Cargo Play: ~1040ms*
 * Cargo run : ~738ms*
 * Kargo play: ~20ms** (object instantiation)
 *
 * Warmed up build times:
 * Cargo Play: ~698ms*
 * Cargo run : ~209ms*
 * Kargo Play: ~1ms** (already instantiated)
 *
 * * These figures include multiple factors like waiting for
 *   the command to run from cmd, initialize its code, copying
 *   files, and so on. In cargo-play's case, we have 3 binaru
 *   calls total (cargo-play, cp, and cargo). Cargo-play's total
 *   run time is the difference between cargo run and cargo-play.
 *
 *  ** Figure includes only setup code, not the time it takes to
 *     run cargo. To get a comparison, subtract cargo-play <minus> cargo.
       So, the difference between both is ~300ms or ~500ms
 */

@Suppress("ObjectPropertyName")
object KargoPlay {
    private val settings = Settings.getInstance()
    private val scratchSettings: ScratchConfiguration
        get() = settings.global.runtime.currentScratch
    private val clean: Boolean
        get() = settings.global.runtime.clean

    private var _srcs: List<String>? = null
    private val srcs: List<String>
        get() = _srcs ?: run {
            _srcs = scratchSettings.srcs.toMutableList().also {
                it.add(0, settings.global.runtime.scratchFile.name)
            }
            _srcs!!
        }

    private var _cargoPlayPath: CargoPlayPath? = null
    private val cargoPlayPath: CargoPlayPath
        get() = _cargoPlayPath ?: run {
            _cargoPlayPath = CargoPlayPath(srcs, settings.global.runtime.scratchRoot)
            cwd = _cargoPlayPath!!.cargoPlayDir
            _cargoPlayPath!!
        }

    private var cwd: String = ""

    // track whether the project changed and needs to be compiled again (cached or not)
    private var needsCompile = false

    private var _edition: Edition? = null
    private val edition: String
        get() = _edition?.myName ?: run {
            val e = scratchSettings.edition
            if (e == Edition.DEFAULT) {
                _edition = Edition.EDITION_2018
                Edition.EDITION_2018.myName
            } else {
                _edition = e
                e.myName
            }
        }

    private var cargoOptions = mutableListOf<String>()
    private var kommand = "run"
    private var args = mutableListOf<String>()
    private var directRun = false

    // there's seriously no java/kotlin libraries that can programmatically serialize toml?
    private val _cargoTomlDefault = """
        [package]
        name = "%s"
        version = "0.1.0"
        edition = "%s"

        [dependencies]
    """.trimIndent()
    private var _cargoToml: String? = null
    private var cargoToml: String
        get() = _cargoToml ?: run {
            _cargoToml = _cargoTomlDefault.format(cargoPlayPath.projectHash, edition)
            _cargoToml!!
        }
        set(value) = run { _cargoToml = value }

    var lastExitCode = 0


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

    private fun postSetup() {
        // set up scratch settings
        scratchSettings.kommand = kommand
        scratchSettings.cargoOptions = cargoOptions
        scratchSettings.args = args
        scratchSettings.workingDirectory = cargoPlayPath.cargoPlayDir
        scratchSettings.directRun = directRun

        // clean up and reset vars
        _srcs = null
        _cargoPlayPath = null
        needsCompile = false
        _edition = null
        cargoOptions = mutableListOf()
        kommand = "run"
        args = mutableListOf()
        directRun = false
        _cargoToml = null
    }

    private fun checkOptions() {
        if (scratchSettings.toolchain != RustChannel.DEFAULT) {
            cargoOptions.add("+${scratchSettings.toolchain.channel}")
        }

        if (scratchSettings.quiet) {
            cargoOptions.add("--quiet")
        }

        if (scratchSettings.verbose) {
            cargoOptions.add("--verbose")
        }

        if (scratchSettings.cargoOptions.isNotEmpty()) {
            scratchSettings.cargoOptions.forEach {
                cargoOptions.add(it)
            }
        }

        if (scratchSettings.release) {
            args.add("--release")
        }

        if (scratchSettings.test) {
            cwd = cargoPlayPath.cargoPlayDir
            kommand = "test"
            return
        }

        if (scratchSettings.check) {
            cwd = cargoPlayPath.cargoPlayDir
            kommand = "check"
            return
        }

        if (scratchSettings.expand) {
            cwd = cargoPlayPath.cargoPlayDir
            kommand = "expand"
            return
        }

        if (scratchSettings.args.isNotEmpty()) {
            args.add("--")
            scratchSettings.args.forEach {
                args.add(it)
            }
        }

        if (scratchSettings.mode.isNotBlank()) {
            kommand = scratchSettings.mode
            return
        }
    }

    private fun directRun() {
        // this is a special case here
        // check whether we can run it cached or not (faster)
        if (!needsCompile) {
            val target = if (scratchSettings.release) cargoPlayPath.releaseTarget else cargoPlayPath.debugTarget
            kommand = target
            directRun = true

            // "--" arg is now invalid
            if (args.isNotEmpty()) {
                args.removeAt(0)
            }
        }
    }

    private fun cleanOnly() {
        val target = cargoPlayPath.targetDir.toFile()
        if (target.exists()) {
            kommand = "clean"
        }
    }

    private fun cleanProject() {
        // remove target folder
        if (scratchSettings.clean || lastExitCode != 0) {
            val target = cargoPlayPath.targetDir.toFile()
            if (target.exists()) {
                needsCompile = true
                target.deleteRecursively()
            }
        }
    }

    @Suppress("LiftReturnOrAssignment")
    private fun createCargoProject() {
        val dir = cargoPlayPath.cargoPlayDir.toFile()

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
            val cargoManifest = cargoPlayPath.cargoManifest.toFile()
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

        val sourcesDir = cargoPlayPath.srcDir.toFile()
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
            val srcFiles = cargoPlayPath.srcDir.toFile().listFiles()?.toMutableList()
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
                                val file = Paths.get(settings.global.runtime.scratchRoot, it).toFile()
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

            val file = Paths.get(settings.global.runtime.scratchRoot, it).toFile()
            val targetFile = Paths.get(cargoPlayPath.srcDir, targetName).toFile()
            file.copyTo(targetFile)
        }
    }

    private fun addDependencies() {
        settings.global.runtime.scratchFile.document?.let { doc ->
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
