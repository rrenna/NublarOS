package os.nublar.dashboard.show

/**
 * Apple TV deep link for the film. The `com.apple.tv://` scheme opens macOS's
 * TV app directly (rather than a browser), and `?action=play` starts playback
 * immediately (requires the film in your library or subscription). The link is
 * the TV app's Share -> Copy Link URL for Jurassic Park (CA store) with the
 * scheme swapped in.
 */
const val MOVIE_URL: String =
    "com.apple.tv://tv.apple.com/ca/movie/jurassic-park/umc.cmc.3bp8rj8ab9otj4ug279xhib2f?action=play"

/** Opens [url] via macOS `open` (fire-and-forget; failures are ignored). */
fun launchMovie(url: String = MOVIE_URL) {
    runCatching { ProcessBuilder("open", url).start() }
}
