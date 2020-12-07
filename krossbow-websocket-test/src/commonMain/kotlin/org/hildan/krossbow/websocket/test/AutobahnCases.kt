package org.hildan.krossbow.websocket.test

val AUTOBAHN_CASES = listOf(
    AutobahnCase("1.1.1"),
    AutobahnCase("1.1.2"),
    AutobahnCase("1.1.3"),
    AutobahnCase("1.1.4"),
    AutobahnCase("1.1.5"),
    AutobahnCase("1.1.6"),
    AutobahnCase("1.1.7"),
    AutobahnCase("1.1.8"),
    AutobahnCase("1.2.1"),
    AutobahnCase("1.2.2"),
    AutobahnCase("1.2.3"),
    AutobahnCase("1.2.4"),
    AutobahnCase("1.2.5"),
    AutobahnCase("1.2.6"),
    AutobahnCase("1.2.7"),
    AutobahnCase("1.2.8"),
    AutobahnCase("2.1", nEchoFrames = 0),
    AutobahnCase("2.2", nEchoFrames = 0),
    AutobahnCase("2.3", nEchoFrames = 0),
    AutobahnCase("2.4", nEchoFrames = 0),
    AutobahnCase("2.5", expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("2.6", nEchoFrames = 0),
    AutobahnCase("2.7", nEchoFrames = 0),
    AutobahnCase("2.8", nEchoFrames = 0),
    AutobahnCase("2.9", nEchoFrames = 0),
    AutobahnCase("2.10", nEchoFrames = 0),
    AutobahnCase("2.11", nEchoFrames = 0),
    AutobahnCase("3.1", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("3.2", nEchoFrames = 1, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("3.3", nEchoFrames = 1, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("3.4", nEchoFrames = 1, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("3.5", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("3.6", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("3.7", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("4.1.1", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("4.1.2", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("4.1.3", nEchoFrames = 1, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("4.1.4", nEchoFrames = 1, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("4.1.5", nEchoFrames = 1, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("4.2.1", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("4.2.2", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("4.2.3", nEchoFrames = 1, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("4.2.4", nEchoFrames = 1, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("4.2.5", nEchoFrames = 1, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("5.1", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("5.2", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("5.3", nEchoFrames = 1),
    AutobahnCase("5.4", nEchoFrames = 1),
    AutobahnCase("5.5", nEchoFrames = 1),
    AutobahnCase("5.6", nEchoFrames = 1),
    AutobahnCase("5.7", nEchoFrames = 1),
    AutobahnCase("5.8", nEchoFrames = 1),
    AutobahnCase("5.9", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("5.10", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("5.11", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("5.12", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("5.13", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("5.14", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("5.15", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("5.16", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("5.17", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("5.18", nEchoFrames = 0, expectFailure = true, end = CaseEnd.CLIENT_FORCE_CLOSE),
    AutobahnCase("5.19", nEchoFrames = 1),
    AutobahnCase("5.20", nEchoFrames = 1),
)

data class AutobahnCase(
    val id: String,
    val nEchoFrames: Int = 1,
    val expectFailure: Boolean = false,
    val end: CaseEnd = CaseEnd.SERVER_CLOSE,
)

enum class CaseEnd {
    SERVER_CLOSE,
    CLIENT_FORCE_CLOSE,
}