#!/usr/bin/env python
# -*- coding: utf-8 -*-


from websocket_client.websocket import WebSocket


def on_text_message(a_message):
    print "************* Mensaje recibido desde el servidor *******************"
    print a_message
    print "********************************************************************"

def on_open():
    print "Para ejecutar esta prueba entre los mensajes y prsione la tecla Enter"
    print "Para finalizar el envio de mensajes entre el texto fin"

    while True :
      print "Entre el texto a enviar:"
      text = raw_input()
      if text == "fin":
        print "prueba finalizada"
        webClient.close()
        break
      else:
        webClient.send_text(text)

def on_close():
    print "connection close"


webClient = WebSocket(on_open=on_open, on_close=on_close,
               on_text_message=on_text_message)


webClient.open("ws://localhost:8787/propio")

raw_input()





