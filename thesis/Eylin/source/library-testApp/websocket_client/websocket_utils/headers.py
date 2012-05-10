#!/usr/bin/env python
#-*- coding:utf-8 -*-

import random



def stringfy_rawpakect(list_byte):
      return ''.join(chr(x) for x in list_byte)


def raw_to_protocol_packet(a_rawpacket):
    l_buffer = []
    l_byte1 = fin_opcode(a_rawpacket.frame_type, a_rawpacket.is_end_frame)
    l_buffer.append(l_byte1)
    l_payload_data = a_rawpacket.byte_array
    l_payloadlen = len(l_payload_data)
    mask_pyloadlen(l_payloadlen, l_buffer)
    l_list_mask = []
    for i in range(0, 4):
        l_rnum = random.randrange(0, 256)
        l_buffer.append(l_rnum)
        l_list_mask.append(l_rnum)
    l_data_masked = payload_data_masking(l_payload_data, l_buffer, l_list_mask)
    l_buffer.extend(l_data_masked)
    return l_buffer


def fin_opcode(a_type, a_is_end_frame):
    """
    building the frist byte of the websocket package header
    """
    MSG = "The packet can not be constructed with unknown frame type: %s"
    if not a_type in range(0, 11) and a_type not in range(3, 8):
        raise Exception(MSG % a_type)

    if a_is_end_frame:
        l_first_header_byte = (a_type | 0x80)
    else:
        l_first_header_byte = a_type

    return l_first_header_byte


def mask_pyloadlen(a_pyloadlen, a_lbuf):
    """
     Alter package buffer by reference with the payload len
    """
    if a_pyloadlen < 126:
		    a_lbuf.append((a_pyloadlen | 0x80))
    elif a_pyloadlen >= 126 and a_pyloadlen < 0xFFFF:
        a_lbuf.append((126 | 0x80))
        a_lbuf.append((a_pyloadlen >> 8))
        a_lbuf.append((a_pyloadlen & 0xFF))
    elif a_pyloadlen >= 0xFFFF:
        a_lbuf.append((127 | 0x80))
        for i in range(0,64,8):
	          a_lbuf.append(int(bin(a_pyloadlen)[2:].zfill(64)[i:i+8], 2))


def payload_data_masking(a_data, a_lbuf, a_lmask):
	  l_len = len(a_data)
	  l_mask_pos = 0
	  l_data_masked = []
	  for i in range(0, l_len):
		    l_data_masked.append(a_data[i] ^ a_lmask[l_mask_pos])
		    if l_mask_pos >= 3:
					l_mask_pos = 0
		    else:
					l_mask_pos = l_mask_pos+1
	  return l_data_masked
	   
    
