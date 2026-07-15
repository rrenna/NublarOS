package os.nublar.commandinterface

import com.jediterm.terminal.TtyConnector
import com.jediterm.terminal.ui.JediTermWidget
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider
import com.pty4j.PtyProcess
import com.pty4j.PtyProcessBuilder
import java.io.OutputStream
import java.io.InputStream
import java.nio.charset.Charset

/**
 * A real PTY-backed terminal — spawns the user's actual shell (not a fake
 * command box), matching README "Terminal Design". Built on pty4j + jediterm,
 * the same pair that powers IntelliJ's embedded terminal. See
 * docs/architecture.md for why this replaces a webview-based terminal.
 */
class NublarTtyConnector(private val process: PtyProcess) : TtyConnector {
    private val charset: Charset = Charsets.UTF_8

    override fun init(questioner: com.jediterm.terminal.Questioner?): Boolean = true

    override fun close() {
        process.destroy()
    }

    override fun read(buf: CharArray, offset: Int, length: Int): Int {
        val inputStream: InputStream = process.inputStream
        val bytes = ByteArray(length)
        val read = inputStream.read(bytes, 0, length)
        if (read <= 0) return read
        val chars = String(bytes, 0, read, charset).toCharArray()
        chars.copyInto(buf, offset, 0, chars.size)
        return chars.size
    }

    override fun write(bytes: ByteArray) {
        val out: OutputStream = process.outputStream
        out.write(bytes)
        out.flush()
    }

    override fun write(string: String) {
        write(string.toByteArray(charset))
    }

    override fun isConnected(): Boolean = process.isAlive

    override fun waitFor(): Int = process.waitFor()

    override fun getName(): String = "ParkNet Terminal"

    override fun ready(): Boolean = process.inputStream.available() > 0

    override fun resize(termWinSize: java.awt.Dimension) {
        process.setWinSize(com.pty4j.WinSize(termWinSize.width, termWinSize.height))
    }
}

/**
 * Spawns [shellCommand] (defaults to $SHELL, falling back to /bin/zsh) in a
 * real PTY and returns a JediTermWidget ready to embed via SwingPanel.
 */
fun createEmbeddedTerminal(
    workingDirectory: String = System.getProperty("user.home"),
    shellCommand: Array<String> = arrayOf(System.getenv("SHELL") ?: "/bin/zsh"),
): JediTermWidget {
    val env = HashMap(System.getenv())
    env["TERM"] = "xterm-256color"

    val process: PtyProcess = PtyProcessBuilder(shellCommand)
        .setEnvironment(env)
        .setDirectory(workingDirectory)
        .start()

    val widget = JediTermWidget(80, 24, DefaultSettingsProvider())
    widget.setTtyConnector(NublarTtyConnector(process))
    widget.start()
    return widget
}
