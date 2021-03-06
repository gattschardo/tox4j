package im.tox.gui.events

import im.tox.gui.MainView
import im.tox.tox4j.core.enums.ToxFileKind
import im.tox.tox4j.core.exceptions.ToxFileSendException
import javax.swing._
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File

final class SendFileButtonOnAction(toxGui: MainView) extends ActionListener {

  override def actionPerformed(event: ActionEvent): Unit = {
    try {
      val friendNumber = toxGui.friendList.getSelectedIndex
      if (friendNumber == -1) {
        JOptionPane.showMessageDialog(toxGui, "Select a friend to send a message to")
      }

      val file = new File(toxGui.fileName.getText)
      if (!file.exists) {
        JOptionPane.showMessageDialog(toxGui, "File does not exist: " + file)
      } else {
        toxGui.fileModel.addOutgoing(
          friendNumber,
          file,
          toxGui.tox.fileSend(
            friendNumber,
            ToxFileKind.DATA,
            file.length,
            null,
            file.getName.getBytes
          )
        )
      }
    } catch {
      case e: ToxFileSendException =>
        toxGui.addMessage("Send file failed: ", e.code)
      case e: Throwable =>
        JOptionPane.showMessageDialog(toxGui, MainView.printExn(e))
    }
  }

}
