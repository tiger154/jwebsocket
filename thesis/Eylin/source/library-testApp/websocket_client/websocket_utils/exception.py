#!/usr/bin/env python
#-*- coding:utf-8 -*-

class WebSocketException(Exception):
    pass


class ConnectionClosedException(WebSocketException):
    pass


class TokenFormatException(WebSocketException):
    pass
