# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'Host_Windows.ui'
#
# Created: Wed Feb 20 12:11:52 2002
#      by: PyQt4 UI code generator 4.8.5
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

try:
    _fromUtf8 = QtCore.QString.fromUtf8
except AttributeError:
    _fromUtf8 = lambda s: s

class Ui_Form(object):
    def setupUi(self, Form):
        Form.setObjectName(_fromUtf8("Form"))
        Form.resize(395, 92)
        font = QtGui.QFont()
        font.setFamily(_fromUtf8("LMRomanDemi10"))
        font.setPointSize(16)
        font.setBold(True)
        font.setWeight(75)
        Form.setFont(font)
        Form.setWindowTitle(QtGui.QApplication.translate("Form", "Connecting Windows", None, QtGui.QApplication.UnicodeUTF8))
        self.label = QtGui.QLabel(Form)
        self.label.setGeometry(QtCore.QRect(10, 0, 291, 31))
        self.label.setText(QtGui.QApplication.translate("Form", "Enter your current Host:", None, QtGui.QApplication.UnicodeUTF8))
        self.label.setObjectName(_fromUtf8("label"))
        self.cmpHostText = QtGui.QLineEdit(Form)
        self.cmpHostText.setGeometry(QtCore.QRect(10, 40, 271, 27))
        font = QtGui.QFont()
        font.setPointSize(10)
        self.cmpHostText.setFont(font)
        self.cmpHostText.setText(QtGui.QApplication.translate("Form", "ws://localhost:8787/demo", None, QtGui.QApplication.UnicodeUTF8))
        self.cmpHostText.setObjectName(_fromUtf8("cmpHostText"))
        self.btnActionConnect = QtGui.QPushButton(Form)
        self.btnActionConnect.setGeometry(QtCore.QRect(290, 40, 97, 27))
        font = QtGui.QFont()
        font.setFamily(_fromUtf8("LMRomanDemi10"))
        font.setPointSize(11)
        font.setBold(True)
        font.setWeight(75)
        self.btnActionConnect.setFont(font)
        self.btnActionConnect.setText(QtGui.QApplication.translate("Form", "Connect", None, QtGui.QApplication.UnicodeUTF8))
        self.btnActionConnect.setObjectName(_fromUtf8("btnActionConnect"))

        self.retranslateUi(Form)
        QtCore.QObject.connect(self.btnActionConnect, QtCore.SIGNAL(_fromUtf8("clicked()")), Form.acctionConnect)
        QtCore.QMetaObject.connectSlotsByName(Form)

    def retranslateUi(self, Form):
        pass

