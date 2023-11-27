package dadb

import java.net.Socket

internal class DadbTestImpl : DadbTest() {

    override fun <T> localEmulator(body: (dadb: Dadb) -> T): T {
        val socket = Socket("localhost", 5555)
        socket.soTimeout = 300
        val keyPair = AdbKeyPair.readDefault()
        val connection = AdbConnection.connect(socket, keyPair)
        val value = TestDadb(connection).use(body)
        connection.ensureEmpty()
        return value
    }

    private class TestDadb(
        private val connection: AdbConnection,
    ) : Dadb {

        override fun open(destination: String) = connection.open(destination)

        override fun supportsFeature(feature: String) = connection.supportsFeature(feature)

        override fun getDeviceApiLevel(): Int {
            return AdbShellV1Stream(connection.open("exec:getprop ro.build.version.sdk")).readAll().output.trim().toInt()
        }

        override fun close() = connection.close()
    }
}