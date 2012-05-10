#!/usr/bin/env python
#-*- coding:utf-8 -*-

import json
from token import Token

def convert_to_dic_type(obj):
    d = {}
    d.update(obj.__dict__)
    return json.dumps(d)


def dict_to_object(d):
    d = json.loads(d)
    class_name = "Token"
    args = dict( (key.encode('ascii'), value) for key, value in d.items())
    inst = Token(**args)
    return inst
