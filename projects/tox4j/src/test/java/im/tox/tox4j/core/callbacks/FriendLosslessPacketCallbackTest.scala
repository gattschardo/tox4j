package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase }
import org.junit.Assert.assertEquals

final class FriendLosslessPacketCallbackTest extends AliceBobTest {

  override type State = Unit
  override def initialState: State = ()

  protected override def newChatClient(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    override def friendConnectionStatus(friendNumber: Int, connectionStatus: ToxConnection)(state: ChatState): ChatState = {
      super.friendConnectionStatus(friendNumber, connectionStatus)(state)
      if (connectionStatus != ToxConnection.NONE) {
        state.addTask { (tox, state) =>
          val packet = s"_My name is $selfName".getBytes
          packet(0) = 160.toByte
          tox.sendLosslessPacket(friendNumber, packet)
          state
        }
      } else {
        state
      }
    }

    override def friendLosslessPacket(friendNumber: Int, packet: Array[Byte])(state: ChatState): ChatState = {
      val message = new String(packet, 1, packet.length - 1)
      debug(s"received a lossless packet[id=${packet(0)}]: $message")
      assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)
      assertEquals(160.toByte, packet(0))
      assertEquals("My name is " + expectedFriendName, message)
      state.finish
    }
  }

}
