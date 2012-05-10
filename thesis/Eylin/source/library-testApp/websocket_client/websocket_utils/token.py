#!/usr/bin/env python
#-*- coding:utf-8 -*-

import json

class Token(object):

    def __init__(self,a_ns=None ,a_token_type=None, **args):
        
        if a_ns == None and args["ns"] == None:
            raise TokenFormatException("ns of token is required")
        if a_token_type == None and args["type"] == None:
            raise TokenFormatException("type of token is required")
     
        self.ns = a_ns
        self.type = a_token_type
        
        for key, value in args.items():
            if key == "ns" and self.ns != None:
               continue

            if key == "type" and self.type != None:
               continue
            setattr(self, key, value)
        
        
        
    def add_data(self,a_key,a_value):
        setattr(self, a_key, a_value)
        
    def get_atribute(self, a_key):
        return getattr(self, a_key)
    
    def get_as_dic(self):
        d = {}
        d.update(self.__dict__)
        return json.dumps(d)
        
    
            
        
        
    
