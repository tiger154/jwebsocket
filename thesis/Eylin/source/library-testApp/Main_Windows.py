# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'Main_Windows.ui'
#
# Created: Sun Feb 17 07:17:12 2002
#      by: PyQt4 UI code generator 4.8.5
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

try:
    _fromUtf8 = QtCore.QString.fromUtf8
except AttributeError:
    _fromUtf8 = lambda s: s

class Ui_MainWindow(object):
    def setupUi(self, MainWindow):
        MainWindow.setObjectName(_fromUtf8("MainWindow"))
        MainWindow.resize(489, 558)
        MainWindow.setWindowTitle(QtGui.QApplication.translate("MainWindow", "MainWindow", None, QtGui.QApplication.UnicodeUTF8))
        icon = QtGui.QIcon()
        icon.addPixmap(QtGui.QPixmap(_fromUtf8("../../Tesis/Plantillas e Imagenes/Imagenes/Muñecos/zhukovy2.jpg")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        MainWindow.setWindowIcon(icon)
        self.centralwidget = QtGui.QWidget(MainWindow)
        self.centralwidget.setObjectName(_fromUtf8("centralwidget"))
        self.label = QtGui.QLabel(self.centralwidget)
        self.label.setGeometry(QtCore.QRect(10, 10, 461, 41))
        font = QtGui.QFont()
        font.setFamily(_fromUtf8("LMRomanDemi10"))
        font.setPointSize(20)
        font.setBold(True)
        font.setWeight(75)
        self.label.setFont(font)
        self.label.setText(QtGui.QApplication.translate("MainWindow", "jWebSocket Python Client Demo", None, QtGui.QApplication.UnicodeUTF8))
        self.label.setObjectName(_fromUtf8("label"))
        self.groupBox = QtGui.QGroupBox(self.centralwidget)
        self.groupBox.setGeometry(QtCore.QRect(20, 60, 441, 291))
        self.groupBox.setTitle(QtGui.QApplication.translate("MainWindow", "Chat room", None, QtGui.QApplication.UnicodeUTF8))
        self.groupBox.setObjectName(_fromUtf8("groupBox"))
        self.cmpWriteText = QtGui.QLineEdit(self.groupBox)
        self.cmpWriteText.setGeometry(QtCore.QRect(10, 250, 311, 27))
        self.cmpWriteText.setObjectName(_fromUtf8("cmpWriteText"))
        self.btnSendChat = QtGui.QPushButton(self.groupBox)
        self.btnSendChat.setGeometry(QtCore.QRect(330, 250, 101, 27))
        self.btnSendChat.setText(QtGui.QApplication.translate("MainWindow", "Send", None, QtGui.QApplication.UnicodeUTF8))
        self.btnSendChat.setObjectName(_fromUtf8("btnSendChat"))
        self.cmpListChat = QtGui.QListWidget(self.groupBox)
        self.cmpListChat.setGeometry(QtCore.QRect(10, 20, 421, 221))
        self.cmpListChat.setObjectName(_fromUtf8("cmpListChat"))
        self.groupBox_2 = QtGui.QGroupBox(self.centralwidget)
        self.groupBox_2.setGeometry(QtCore.QRect(20, 360, 441, 141))
        self.groupBox_2.setTitle(QtGui.QApplication.translate("MainWindow", "Logs", None, QtGui.QApplication.UnicodeUTF8))
        self.groupBox_2.setObjectName(_fromUtf8("groupBox_2"))
        self.cmpListLogs = QtGui.QListWidget(self.groupBox_2)
        self.cmpListLogs.setGeometry(QtCore.QRect(10, 20, 421, 111))
        self.cmpListLogs.setObjectName(_fromUtf8("cmpListLogs"))
        MainWindow.setCentralWidget(self.centralwidget)
        self.menubar = QtGui.QMenuBar(MainWindow)
        self.menubar.setGeometry(QtCore.QRect(0, 0, 489, 19))
        self.menubar.setObjectName(_fromUtf8("menubar"))
        self.menuArchivo = QtGui.QMenu(self.menubar)
        self.menuArchivo.setGeometry(QtCore.QRect(218, 115, 120, 106))
        self.menuArchivo.setTitle(QtGui.QApplication.translate("MainWindow", "Connection", None, QtGui.QApplication.UnicodeUTF8))
        self.menuArchivo.setObjectName(_fromUtf8("menuArchivo"))
        self.menuEdit = QtGui.QMenu(self.menubar)
        self.menuEdit.setTitle(QtGui.QApplication.translate("MainWindow", "Edit", None, QtGui.QApplication.UnicodeUTF8))
        self.menuEdit.setObjectName(_fromUtf8("menuEdit"))
        MainWindow.setMenuBar(self.menubar)
        self.statusbar = QtGui.QStatusBar(MainWindow)
        self.statusbar.setObjectName(_fromUtf8("statusbar"))
        MainWindow.setStatusBar(self.statusbar)
        self.actionConnect = QtGui.QAction(MainWindow)
        self.actionConnect.setText(QtGui.QApplication.translate("MainWindow", "Connect", None, QtGui.QApplication.UnicodeUTF8))
        self.actionConnect.setObjectName(_fromUtf8("actionConnect"))
        self.actionDisconnect = QtGui.QAction(MainWindow)
        self.actionDisconnect.setText(QtGui.QApplication.translate("MainWindow", "Disconnect", None, QtGui.QApplication.UnicodeUTF8))
        self.actionDisconnect.setObjectName(_fromUtf8("actionDisconnect"))
        self.actionClear_Chat = QtGui.QAction(MainWindow)
        self.actionClear_Chat.setText(QtGui.QApplication.translate("MainWindow", "Clear Chat", None, QtGui.QApplication.UnicodeUTF8))
        self.actionClear_Chat.setObjectName(_fromUtf8("actionClear_Chat"))
        self.actionClear_Logs = QtGui.QAction(MainWindow)
        self.actionClear_Logs.setText(QtGui.QApplication.translate("MainWindow", "Clear Logs", None, QtGui.QApplication.UnicodeUTF8))
        self.actionClear_Logs.setObjectName(_fromUtf8("actionClear_Logs"))
        self.actionClear_Chat_2 = QtGui.QAction(MainWindow)
        self.actionClear_Chat_2.setText(QtGui.QApplication.translate("MainWindow", "Clear Chat", None, QtGui.QApplication.UnicodeUTF8))
        self.actionClear_Chat_2.setObjectName(_fromUtf8("actionClear_Chat_2"))
        self.actionClear_Logs_2 = QtGui.QAction(MainWindow)
        self.actionClear_Logs_2.setText(QtGui.QApplication.translate("MainWindow", "Clear Logs", None, QtGui.QApplication.UnicodeUTF8))
        self.actionClear_Logs_2.setObjectName(_fromUtf8("actionClear_Logs_2"))
        self.actionMake_Ping = QtGui.QAction(MainWindow)
        self.actionMake_Ping.setText(QtGui.QApplication.translate("MainWindow", "Make Ping", None, QtGui.QApplication.UnicodeUTF8))
        self.actionMake_Ping.setObjectName(_fromUtf8("actionMake_Ping"))
        self.menuArchivo.addAction(self.actionConnect)
        self.menuArchivo.addAction(self.actionDisconnect)
        self.menuArchivo.addAction(self.actionMake_Ping)
        self.menuEdit.addAction(self.actionClear_Chat_2)
        self.menuEdit.addAction(self.actionClear_Logs_2)
        self.menubar.addAction(self.menuArchivo.menuAction())
        self.menubar.addAction(self.menuEdit.menuAction())

        self.retranslateUi(MainWindow)
        QtCore.QObject.connect(self.btnSendChat, QtCore.SIGNAL(_fromUtf8("clicked()")), MainWindow.sendText)
        QtCore.QObject.connect(self.actionDisconnect, QtCore.SIGNAL(_fromUtf8("activated()")), MainWindow.acctionDisconnects)
        QtCore.QObject.connect(self.actionConnect, QtCore.SIGNAL(_fromUtf8("activated()")), MainWindow.showConnectWindow)
        QtCore.QObject.connect(self.actionClear_Chat_2, QtCore.SIGNAL(_fromUtf8("activated()")), MainWindow.actionClearChat)
        QtCore.QObject.connect(self.actionClear_Logs_2, QtCore.SIGNAL(_fromUtf8("activated()")), MainWindow.actionClearLogs)
        QtCore.QObject.connect(self.actionMake_Ping, QtCore.SIGNAL(_fromUtf8("activated()")), MainWindow.actionMakePing)
        QtCore.QMetaObject.connectSlotsByName(MainWindow)

    def retranslateUi(self, MainWindow):
        pass

