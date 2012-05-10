#!/usr/bin/env python
# -*- coding: utf-8 -*-


from websocket_client.websocket import WebSocket


def on_text_message(a_token_message):
    print a_token_message

def on_open():
    print "connection open"

def on_close():
    print "connection close"


webClient = WebSocket(on_open=on_open, on_close=on_close,
               on_text_message=on_text_message)


webClient.open("ws://localhost:8787/propio")

raw_input()
webClient.close()

raw_input()







