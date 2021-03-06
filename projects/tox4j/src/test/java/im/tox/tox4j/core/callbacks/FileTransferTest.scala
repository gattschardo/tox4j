package im.tox.tox4j.core.callbacks

import java.util
import java.util.Random

import im.tox.tox4j.core.enums.{ ToxConnection, ToxFileControl, ToxFileKind }
import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase }
import org.junit.Assert._

final class FileTransferTest extends AliceBobTest {

  private val fileData = new Array[Byte](1500)
  new Random().nextBytes(fileData)

  sealed case class State(
      receivedData: Array[Byte] = Array.ofDim[Byte](fileData.length),
      position: Long = 0L,
      sentFileNumber: Int = -1
  ) {
    def addData(position: Long, data: Array[Byte]): State = {
      assert(data.nonEmpty)
      val nextPosition = this.position + data.length
      assert(nextPosition <= this.receivedData.length)
      System.arraycopy(data, 0, this.receivedData, position.toInt, data.length)
      copy(position = nextPosition)
    }
  }

  override def initialState: State = State()

  protected override def newChatClient(name: String, expectedFriendName: String) = new Alice(name, expectedFriendName)

  class Alice(name: String, expectedFriendName: String) extends ChatClient(name, expectedFriendName) {

    override def friendConnectionStatus(friendNumber: Int, connectionStatus: ToxConnection)(state: ChatState): ChatState = {
      super.friendConnectionStatus(friendNumber, connectionStatus)(state)
      if (connectionStatus != ToxConnection.NONE && isAlice) {
        state.addTask { (tox, state) =>
          val sentFileNumber = tox.fileSend(
            friendNumber,
            ToxFileKind.DATA,
            fileData.length,
            Array.ofDim[Byte](0),
            s"file for $expectedFriendName.png".getBytes
          )
          state.set(state.get.copy(sentFileNumber = sentFileNumber))
        }
      } else {
        state
      }
    }

    override def fileRecv(friendNumber: Int, fileNumber: Int, kind: Int, fileSize: Long, filename: Array[Byte])(state: ChatState): ChatState = {
      debug(s"received file send request $fileNumber from friend number $friendNumber")
      assert(isBob)
      assert(friendNumber == AliceBobTestBase.FRIEND_NUMBER)
      assert(fileNumber == (0 | 0x10000))
      assert(kind == ToxFileKind.DATA)
      assert(fileSize == fileData.length)
      assert(new String(filename) == s"file for $name.png")
      state.addTask { (tox, state) =>
        debug("sending control RESUME for " + fileNumber)
        tox.fileControl(friendNumber, fileNumber, ToxFileControl.RESUME)
        state
      }
    }

    override def fileRecvControl(friendNumber: Int, fileNumber: Int, control: ToxFileControl)(state: ChatState): ChatState = {
      debug(s"file control from $friendNumber for file $fileNumber: $control")
      assert(isAlice)
      state
    }

    override def fileChunkRequest(friendNumber: Int, fileNumber: Int, position: Long, length: Int)(state: ChatState): ChatState = {
      debug(s"got request for ${length}B from $friendNumber for file $fileNumber at $position")
      assert(friendNumber == AliceBobTestBase.FRIEND_NUMBER)
      assert(isAlice)
      assert(position >= 0)
      assert(position < Integer.MAX_VALUE)
      assert(fileNumber == state.get.sentFileNumber)
      if (length == 0) {
        state.set(state.get.copy(sentFileNumber = -1)).finish
      } else {
        state.addTask { (tox, state) =>
          debug(s"sending ${length}B to $friendNumber")
          tox.fileSendChunk(
            friendNumber,
            fileNumber,
            position,
            util.Arrays.copyOfRange(
              fileData,
              position.toInt,
              Math.min(position.toInt + length, fileData.length)
            )
          )
          state
        }
      }
    }

    override def fileRecvChunk(friendNumber: Int, fileNumber: Int, position: Long, data: Array[Byte])(state: ChatState): ChatState = {
      debug(s"got ${data.length}B from $friendNumber at $position")
      assert(isBob)
      assert(friendNumber == AliceBobTestBase.FRIEND_NUMBER)
      assert(fileNumber == (0 | 0x10000))
      assert(position == state.get.position)
      assert(data != null)
      if (state.get.position == state.get.receivedData.length) {
        assert(data.isEmpty)
        assertArrayEquals(fileData, state.get.receivedData)
        state.finish
      } else {
        state.set(state.get.addData(position, data))
      }
    }
  }

}
