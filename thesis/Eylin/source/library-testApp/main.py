#!/usr/bin/env python
# -*- coding: utf-8 -*-

from PyQt4 import QtCore, QtGui
import sys


from Host_Windows import Ui_Form
from Main_Windows import Ui_MainWindow


from websocket_client.jwspython import JWSClient
from websocket_client.websocket_utils.token import Token


class HostWindows(QtGui.QWidget):
    def __init__(self, parent=None, mainWin=None):
        QtGui.QWidget.__init__(self, parent)
        self.ui=Ui_Form()
        self.ui.setupUi(self)
        self.mainWin = mainWin



    def acctionConnect(self):
        wsUrl=self.ui.cmpHostText.text()
        wsUrl1=  wsUrl.toLocal8Bit().data()
        self.mainWin.openConnection(wsUrl1)

class MainWindows(QtGui.QMainWindow):
    def __init__(self, parent=None):
        QtGui.QMainWindow.__init__(self, parent)
        self.mainWin=Ui_MainWindow()
        self.mainWin.setupUi(self)
        self.formConnect = HostWindows(None, self)
        self.jwsClient = JWSClient(on_open=self.on_open,
                         on_close=self.on_close,
                         on_text_message=self.on_text_message,
                         on_pong=self.on_pong, on_ping=self.on_ping)


    def showConnectWindow(self):
        self.formConnect.show()

    def sendText(self):
        text =self.mainWin.cmpWriteText.text()
        text1=  text.toLocal8Bit().data()

        obj = Token("jws.py.demo","chat",message=text1,sender="pyClient")
        try:
          self.jwsClient.send_token(obj)
          self.mainWin.cmpWriteText.setText("")
          self.addChatMessage(text,"pyClient")
        except:
          win =  QtGui.QErrorMessage(self)
          win.showMessage("The connection is already close, the message could not be sent");


    def acctionDisconnects(self):
        self.jwsClient.close()

    def actionClearChat(self):
        self.mainWin.cmpListChat.clear()

    def actionClearLogs(self):
        self.mainWin.cmpListLogs.clear()

    def addChatMessage(self, text, sender=None):
        self.mainWin.cmpListChat.addItem(sender+":  "+text)

    def on_pong(self):
        self.addLog("pong received")

    def on_ping(self):
        self.addLog("pong received")

    def on_open(self):
        self.addLog("the connection opened")

    def on_close(self,a_close_reason):
        self.addLog("connection closed: "+a_close_reason)


    def on_text_message(self,a_token_message):
        self.addLog(a_token_message.get_as_dic())
        if a_token_message.type == "chat":
          self.addChatMessage(a_token_message.message, a_token_message.sender)


    def openConnection(self, wsUrl):
      #try:
        self.jwsClient.open(wsUrl)
      #except:
      #  win =  QtGui.QErrorMessage(self)
      #  win.showMessage("Connection failed, server not found");

    def addLog(self,text):
        self.mainWin.cmpListLogs.addItem("log: "+text)

    def actionMakePing(self):
        self.jwsClient.ping()

if __name__ == "__main__":
    app = QtGui.QApplication(sys.argv)
    myapp = MainWindows()
    myapp.show()
    sys.exit(app.exec_())

