package org.hildan.krossbow.websocket.test

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.takeWhile
import org.hildan.krossbow.websocket.WebSocketClient
import org.hildan.krossbow.websocket.WebSocketException
import org.hildan.krossbow.websocket.WebSocketFrame
import org.hildan.krossbow.websocket.WebSocketSession
import kotlin.test.*

/*
Run this container in parallel of the test suite (use absolute windows paths instead of $PWD):

docker run -it --rm -v "C:\Projects\krossbow\config:/config" -v "C:\Projects\krossbow\reports:/reports" -p 12345:12345 crossbario/autobahn-testsuite
 */
abstract class AutobahnClientTestSuite {

    abstract fun provideClient(): WebSocketClient

    val baseUrl = "ws://localhost:12345"

    private lateinit var wsClient: WebSocketClient

    @BeforeTest
    fun setupClient() {
        wsClient = provideClient()
    }

    @Test
    fun autobahn_1_x_x_echo_payload() = runSuspendingTest {
        AUTOBAHN_CASES.filter { it.id.startsWith("1") }.forEach { case -> runEchoAutobahnCase(case) }
    }

    @Test
    fun autobahn_2_x_ping_pong() = runSuspendingTest {
        AUTOBAHN_CASES.filter { it.id.startsWith("2") }.forEach { case -> runEchoAutobahnCase(case) }
    }

    @Test
    fun autobahn_3_x_x_reserved_bits() = runSuspendingTest {
        AUTOBAHN_CASES.filter { it.id.startsWith("3") }.forEach { case -> runEchoAutobahnCase(case) }
    }

    @Test
    fun autobahn_4_x_x_opcodes() = runSuspendingTest {
        AUTOBAHN_CASES.filter { it.id.startsWith("4") }.forEach { case -> runEchoAutobahnCase(case) }
    }

    @Test
    fun autobahn_5_x_x_fragmentation() = runSuspendingTest {
        AUTOBAHN_CASES.filter { it.id.startsWith("5") }.forEach { case -> runEchoAutobahnCase(case) }
    }

    @AfterTest
    fun generateReports() {
        runSuspendingTest {
            wsClient.autobahnReports()
        }
    }

    private suspend fun runEchoAutobahnCase(case: AutobahnCase) {
        try {
            val session = wsClient.autobahnTestConnect(case.id)
            when (case.end) {
                CaseEnd.SERVER_CLOSE -> session.echoFramesUntilClosed()
                CaseEnd.CLIENT_FORCE_CLOSE -> session.echoNFramesAndExpectClientForceClose(case.nEchoFrames)
            }
        } catch (e: WebSocketException) {
            assertTrue(case.expectFailure)
        } catch (e: Exception) {
            val message = "Exception during test case ${case.id} (expected: ${case.expectFailure}): ${e.message}"
            if (case.expectFailure) {
                println(message)
            } else {
                fail(message)
            }
        }
    }

    private suspend fun WebSocketClient.autobahnTestConnect(
        case: String, agent: String = "krossbow"
    ): WebSocketSession = connect("$baseUrl/runCase?casetuple=$case&agent=$agent")

    private suspend fun WebSocketClient.autobahnReports(
        agent: String = "krossbow"
    ): WebSocketSession = connect("$baseUrl/updateReports?agent=$agent")
}

private suspend fun WebSocketSession.echoFramesUntilClosed() {
    incomingFrames.consumeAsFlow().takeWhile { it !is WebSocketFrame.Close }.collect { frame ->
        echoFrame(frame)
    }
}

private suspend fun WebSocketSession.echoNFramesAndExpectClientForceClose(n: Int) {
    echoNFrames(n)
    //assertFalse(canSend) // FIXME why doesn't that work
    // TODO assert incomingFrames channel closed?
    if (canSend) {
        assertFails { close() }
    }
}

private suspend fun WebSocketSession.echoNFrames(n: Int) {
    repeat(n) {
        echoFrame(incomingFrames.receive())
    }
}

private suspend fun WebSocketSession.echoFrame(frame: WebSocketFrame) {
    when (frame) {
        is WebSocketFrame.Text -> sendText(frame.text)
        is WebSocketFrame.Binary -> sendBinary(frame.bytes)
        is WebSocketFrame.Ping -> Unit // nothing special, we expect the underlying impl to PONG properly
        is WebSocketFrame.Pong -> Unit // nothing to do
        is WebSocketFrame.Close -> error("should not receive CLOSE frame at that point")
    }
}
