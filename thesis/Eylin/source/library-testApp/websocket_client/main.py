#!/usr/bin/env python
# -*- coding: utf-8 -*-


from jwspython import JWSClient
from websocket_utils.token import Token



def on_pong():
    print "pong received"

def on_ping():
    print "pong received"
    
def on_open():
    print "the connection opened"
    
def on_close(a_close_reason):
    print "connection closed: "+a_close_reason
    
    
def on_text_message(a_token_message):
    print a_token_message.get_atribute("ns")
    print a_token_message.get_atribute("type")
    print a_token_message.get_as_dic()
    a_token_message.add_data("te","quiero")
    print a_token_message.get_atribute("te")
    print a_token_message.get_as_dic()


jwsClient = JWSClient(on_open=on_open, on_close=on_close,
               on_text_message=on_text_message,
               on_pong=on_pong, on_ping=on_ping)
               
jwsClient.open("ws://localhost:8787/propio")

print "enter to make ping"
raw_input()
jwsClient.ping()

print "enter to send text"
raw_input()
obj = Token("persona","crear",nombre="osvaldo")
obj.add_data("edad", 25)


jwsClient.send_token(obj)

print "enter to close connection"
raw_input()
jwsClient.close()

raw_input()







