#!/usr/bin/env python
#-*- coding:utf-8 -*-

class WebsocketRawPacket(object):
	
  def __init__(self, a_frame_type, a_byte_array):
    self.byte_array = a_byte_array
    self.m_is_end_frame =  False 
    self.m_frame_type = a_frame_type
   
  @property
  def byte_array(self):
    return self.m_byte_array

  @byte_array.setter
  def byte_array(self, value):
    if isinstance(value, list):
        self.m_byte_array = value
    else:
        self.m_byte_array = [ord(c) for c in value]
    
  @property
  def is_end_frame(self):
    return self.m_is_end_frame

  @is_end_frame.setter
  def is_end_frame(self, value):
    self.m_is_end_frame = value

  @property
  def frame_type(self):
    return self.m_frame_type

  @frame_type.setter
  def frame_type(self, value):
    self.m_frame_type = value  
