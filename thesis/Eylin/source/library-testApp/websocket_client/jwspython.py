#!/usr/bin/env python
#-*- coding:utf-8 -*-

from websocket import WebSocket
from websocket_utils.token_proccessor import convert_to_dic_type
from websocket_utils.token_proccessor import dict_to_object


class JWSClient(WebSocket):

    def __init__(self,on_open = None,on_close = None, 
                 on_ping= None, on_pong=None,
                 on_text_message=None, on_binary_message=None,
                 on_fragment=None):
                     
        super(JWSClient,self).__init__(on_open, on_close, on_ping,
                                       on_pong, on_text_message,
                                       on_binary_message, on_fragment)

        self._client_token_text_message  = on_text_message
        self._client_token_binary_message = on_binary_message
        
        def _on_client_text_message(a_data):
            l_token = self._convert_to_token(a_data)
            self._client_token_text_message(l_token)
            
        def _on_client_binary_message(a_data):
            l_token = self._convert_to_token(a_data)
            self._client_token_binary_message(l_token)
        
        self.on_text_message = _on_client_text_message
        self.on_binary_message = _on_client_binary_message

        
    def send_token_text(self, a_token):
        #@TODO get the token and strinfy to send
        self.send_text(a_token)
        
    def send_token_binary(self, a_token):
        #@TODO get the token and strinfy to send
        self.send_binary(a_token)
        
    def send_token(self, a_token):
        #@TODO if token has a byte array as data send in binary mode 
        #else send as simple text
        token_str = convert_to_dic_type(a_token)
        self.send_text(token_str)
        
    def send_token_fragmented(self, a_token, a_fragment_size):
        #@TODO if token has a byte array as data send fragments 
        #in binary mode else send fragment as simple text
        self.send_text_maxframesize(a_token, a_fragment_size)

    def _convert_to_token(self, a_data):
        return dict_to_object(a_data)


        
    
        

