package org.hildan.krossbow.websocket.jdk

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.hildan.krossbow.websocket.WebSocketCloseCodes
import org.hildan.krossbow.websocket.WebSocketFrame
import org.hildan.krossbow.websocket.test.testEchoWs
import org.hildan.krossbow.websocket.test.withEchoServer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Jdk11WebSocketClientTest {

    @Test
    fun test() {
        withEchoServer { server ->
            testEchoWs(Jdk11WebSocketClient(), "ws://localhost:${server.port}")
        }
    }

    @Test
    fun testClose() {
        withEchoServer { server ->
            runBlocking {
                val session = Jdk11WebSocketClient().connect("ws://localhost:${server.port}")

                session.sendText("hello")
                val helloResponse = withTimeout(500) { session.incomingFrames.receive() }
                assertTrue(helloResponse is WebSocketFrame.Text)
                assertEquals("hello", helloResponse.text)

                val connections = server.connections()
                assertEquals(1, connections.size)
                connections.forEach { it.closeConnection(WebSocketCloseCodes.SERVER_ERROR, "simulated error") }

                delay(100)
                session.close()
            }
        }
    }
}
