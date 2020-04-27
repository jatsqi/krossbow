package org.hildan.krossbow.stomp.frame

import okio.Buffer
import okio.BufferedSource
import org.hildan.krossbow.stomp.headers.HeaderEscaper
import org.hildan.krossbow.stomp.headers.StompHeaders
import org.hildan.krossbow.stomp.headers.asStompHeaders

internal object StompDecoder {

    private const val NULL_BYTE = 0.toByte()

    fun decode(frameBytes: ByteArray): StompFrame = Buffer().apply { write(frameBytes) }.readStompFrame(true)

    fun decode(frameText: CharSequence): StompFrame = Buffer().apply { writeUtf8(frameText.toString()) }.readStompFrame(false)

    private fun BufferedSource.readStompFrame(isBinary: Boolean): StompFrame {
        try {
            val command = readStompCommand()
            val headers = readStompHeaders(command.supportsHeaderEscapes)
            val body = readBodyBytes(headers.contentLength)?.toFrameBody(isBinary)
            expectNullOctet()
            expectOnlyEOLs()
            return StompFrame.create(command, headers, body)
        } catch (e: Exception) {
            throw InvalidStompFrameException(e)
        }
    }

    private fun BufferedSource.readStompCommand(): StompCommand {
        val firstLine = readUtf8Line() ?: error("Missing command in STOMP frame")
        return StompCommand.parse(firstLine)
    }

    private fun BufferedSource.readStompHeaders(shouldUnescapeHeaders: Boolean): StompHeaders =
            utf8LineSequence()
                .takeWhile { it.isNotEmpty() } // empty line marks end of headers
                .parseLinesAsStompHeaders(shouldUnescapeHeaders)

    private fun Sequence<String>.parseLinesAsStompHeaders(shouldUnescapeHeaders: Boolean): StompHeaders {
        val headersMap = mutableMapOf<String, String>()
        forEach { line ->
            // the colon ':' is safe to use to split the line because it is escaped as \c (see HeaderEscaper)
            val (rawKey, rawValue) = line.split(':', ignoreCase = false, limit = 2)
            val key = if (shouldUnescapeHeaders) HeaderEscaper.unescape(rawKey) else rawKey
            val value = if (shouldUnescapeHeaders) HeaderEscaper.unescape(rawValue) else rawValue
            // If a client or a server receives repeated frame header entries, only the first header entry SHOULD be
            // used as the value of header entry. Subsequent values are only used to maintain a history of state changes
            // of the header and MAY be ignored.
            // https://stomp.github.io/stomp-specification-1.2.html#Repeated_Header_Entries
            if (!headersMap.containsKey(key)) {
                headersMap[key] = value
            }
        }
        return headersMap.asStompHeaders()
    }

    private fun BufferedSource.utf8LineSequence(): Sequence<String> = sequence {
        while (true) {
            yield(readUtf8LineStrict())
        }
    }

    private fun BufferedSource.readBodyBytes(contentLength: Int?) = when (contentLength) {
        0 -> null
        else -> readByteArray(contentLength?.toLong() ?: indexOf(NULL_BYTE))
    }

    private fun ByteArray.toFrameBody(binary: Boolean) = when {
        isEmpty() -> null
        binary -> FrameBody.Binary(this)
        else -> FrameBody.Text(this)
    }

    private fun BufferedSource.expectNullOctet() {
        if (readByte() != NULL_BYTE) {
            error("Expected NULL byte at end of frame")
        }
    }

    private fun BufferedSource.expectOnlyEOLs() {
        if (!exhausted()) {
            val endText = readUtf8()
            if (endText.any { it != '\n' && it != '\r' }) {
                error("Unexpected non-EOL characters after end-of-frame NULL character: $endText")
            }
        }
    }
}

class InvalidStompFrameException(cause: Throwable) : Exception("Failed to decode invalid STOMP frame", cause)
