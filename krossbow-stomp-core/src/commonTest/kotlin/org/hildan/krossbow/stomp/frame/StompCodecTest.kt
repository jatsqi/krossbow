package org.hildan.krossbow.stomp.frame

import org.hildan.krossbow.stomp.headers.StompConnectHeaders
import org.hildan.krossbow.stomp.headers.StompMessageHeaders
import org.hildan.krossbow.stomp.headers.StompSendHeaders
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalStdlibApi::class)
class StompCodecTest {
    private val nullChar = '\u0000'

    @Test
    fun stomp_basic() {
        val frameText = """
            STOMP
            host:some.host
            accept-version:1.2
            
            $nullChar
        """.trimIndent()

        val headers = StompConnectHeaders(host = "some.host")
        val frame = StompFrame.Stomp(headers)
        assertEncodingDecoding(frameText, frame, frame)
    }

    @Test
    fun connect_basic() {
        val frameText = """
            CONNECT
            host:some.host
            accept-version:1.2
            
            $nullChar
        """.trimIndent()

        val headers = StompConnectHeaders(host = "some.host")
        val frame = StompFrame.Connect(headers)
        assertEncodingDecoding(frameText, frame, frame)
    }

    @Test
    fun connect_credentials() {
        val frameText = """
            CONNECT
            host:some.host
            accept-version:1.2
            login:bob
            passcode:mypass
            
            $nullChar
        """.trimIndent()

        val headers = StompConnectHeaders(host = "some.host", login = "bob", passcode = "mypass")
        val frame = StompFrame.Connect(headers)
        assertEncodingDecoding(frameText, frame, frame)
    }

    @Test
    fun connect_versions() {
        val frameText = """
            CONNECT
            host:some.host
            accept-version:1.0,1.1,1.2
            
            $nullChar
        """.trimIndent()

        val headers = StompConnectHeaders(host = "some.host", acceptVersion = listOf("1.0", "1.1", "1.2"))
        val frame = StompFrame.Connect(headers)
        assertEncodingDecoding(frameText, frame, frame)
    }

    @Test
    fun send_noBody_noContentLength() {
        val frameText = """
            SEND
            destination:test
            
            $nullChar
        """.trimIndent()

        val headers = StompSendHeaders(destination = "test")
        val frame = StompFrame.Send(headers, body = null)
        assertEncodingDecoding(frameText, frame, frame)
    }

    @Test
    fun send_classicTextBody_noContentLength() {
        val frameText = """
            SEND
            destination:test
            
            The body of the message.$nullChar
        """.trimIndent()

        val headers = StompSendHeaders(destination = "test")
        val bodyText = "The body of the message."
        val textFrame = StompFrame.Send(headers, FrameBody.Text(bodyText))
        val binFrame = StompFrame.Send(headers, FrameBody.Binary(bodyText.encodeToByteArray()))
        assertEncodingDecoding(frameText, textFrame, binFrame)
    }

    @Test
    fun send_classicTextBody_withContentLength() {
        val frameText = """
            SEND
            destination:test
            content-length:24
            
            The body of the message.$nullChar
        """.trimIndent()

        val headers = StompSendHeaders(destination = "test").apply { contentLength = 24 }
        val bodyText = "The body of the message."
        val textFrame = StompFrame.Send(headers, FrameBody.Text(bodyText))
        val binFrame = StompFrame.Send(headers, FrameBody.Binary(bodyText.encodeToByteArray()))
        assertEncodingDecoding(frameText, textFrame, binFrame)
    }

    @Test
    fun send_bodyWithNullChar_withContentLength() {
        val frameText = """
            SEND
            destination:test
            content-length:25
            
            The body of$nullChar the message.$nullChar
        """.trimIndent()

        val headers = StompSendHeaders(destination = "test").apply { contentLength = 25 }
        val bodyText = "The body of$nullChar the message."
        val textFrame = StompFrame.Send(headers, FrameBody.Text(bodyText))
        val binFrame = StompFrame.Send(headers, FrameBody.Binary(bodyText.encodeToByteArray()))
        assertEncodingDecoding(frameText, textFrame, binFrame)
    }

    @Test
    fun message_noBody_noContentLength() {
        val frameText = """
            MESSAGE
            destination:test
            message-id:123
            subscription:42
            
            $nullChar
        """.trimIndent()

        val headers = StompMessageHeaders(
            destination = "test",
            messageId = "123",
            subscription = "42"
        )
        val frame = StompFrame.Message(headers, body = null)
        assertEncodingDecoding(frameText, frame, frame)
    }

    @Test
    fun message_classicTextBody_noContentLength() {
        val frameText = """
            MESSAGE
            destination:test
            message-id:123
            subscription:42
            
            The body of the message.$nullChar
        """.trimIndent()

        val headers = StompMessageHeaders(
            destination = "test",
            messageId = "123",
            subscription = "42"
        )
        val bodyText = "The body of the message."
        val textFrame = StompFrame.Message(headers, FrameBody.Text(bodyText))
        val binFrame = StompFrame.Message(headers, FrameBody.Binary(bodyText.encodeToByteArray()))
        assertEncodingDecoding(frameText, textFrame, binFrame)
    }

    @Test
    fun message_classicTextBody_withContentLength() {
        val frameText = """
            MESSAGE
            destination:test
            message-id:123
            subscription:42
            content-length:24
            
            The body of the message.$nullChar
        """.trimIndent()

        val headers = StompMessageHeaders(
            destination = "test",
            messageId = "123",
            subscription = "42"
        ).apply {
            contentLength = 24
        }
        val bodyText = "The body of the message."
        val textFrame = StompFrame.Message(headers, FrameBody.Text(bodyText))
        val binFrame = StompFrame.Message(headers, FrameBody.Binary(bodyText.encodeToByteArray()))
        assertEncodingDecoding(frameText, textFrame, binFrame)
    }

    @Test
    fun message_bodyWithNullChar_withContentLength() {
        val frameText = """
            MESSAGE
            destination:test
            message-id:123
            subscription:42
            content-length:25
            
            The body of$nullChar the message.$nullChar
        """.trimIndent()

        val headers = StompMessageHeaders(
            destination = "test",
            messageId = "123",
            subscription = "42"
        ).apply {
            contentLength = 25
        }
        val bodyText = "The body of$nullChar the message."
        val textFrame = StompFrame.Message(headers, FrameBody.Text(bodyText))
        val binFrame = StompFrame.Message(headers, FrameBody.Binary(bodyText.encodeToByteArray()))
        assertEncodingDecoding(frameText, textFrame, binFrame)
    }

    private fun assertEncodingDecoding(frameText: String, textFrame: StompFrame, binFrame: StompFrame) {
        assertEquals(textFrame, StompDecoder.decode(frameText))
        assertEquals(binFrame, StompDecoder.decode(frameText.encodeToByteArray()))
        assertEquals(frameText, textFrame.encodeToText())
        assertTrue(frameText.encodeToByteArray().contentEquals(textFrame.encodeToBytes()))
    }
}
