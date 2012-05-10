#!/usr/bin/env python
#-*- coding:utf-8 -*-

import uuid
import socket
import thread
import base64
import hashlib
import logging
from urlparse import urlparse

from websocket_utils import headers
from websocket_utils.exception import WebSocketException, ConnectionClosedException
from websocket_utils.raw_packet import WebsocketRawPacket

web_socket_frame_type = {
    'INVALID': -1, 'FRAGMENT': 0x00, 'TEXT': 0x01, 'BINARY': 0x02,
    'CLOSE': 0x08, 'PING': 0x09, 'PONG': 0x0A
}

MAX_FRAME_SIZE = 1048840

HEADERS_TO_CHECK = {
    "Upgrade": "websocket",
    "Connection": "Upgrade",
}

HEADERS_TO_EXIST_FOR_HYBI = [
    'Sec-WebSocket-Protocol'
]

logger = logging.getLogger()

DEFAULT_TIMEOUT = 4000
traceEnabled = False


def enableTrace(tracable):
    """
    turn on/off the tracability.
    """
    global traceEnabled
    traceEnabled = tracable
    if tracable:
        if not logger.handlers:
            logger.addHandler(logging.StreamHandler())
        logger.setLevel(logging.DEBUG)


class WebSocket(object):

    def __init__(self,on_open = None,on_close = None,
                 on_ping= None, on_pong=None,
                 on_text_message=None, on_binary_message=None,
                 on_fragment=None):
        """
        Initalize WebSocket object.
        """
        self.connected = False
        self.on_text_message = on_text_message
        self.on_binary_message = on_binary_message
        self.on_open = on_open
        self.on_close = on_close
        self.on_ping  = on_ping
        self.on_pong  = on_pong

        self.io_sock = self.sock = socket.socket()


    def open(self, url, **options):
        """
        Setup default timeout
        """
        #if ('timeout' in options):
        #    self.set_timeout(options['timeout'])
        #else:
        #    self.set_timeout(DEFAULT_TIMEOUT)
        """
        Connect to url. url is websocket url scheme.
        ie. ws://host:port/resource
        """

        hostname, port, resource, is_secure = self._parse_url(url)
        self.sock.connect((hostname, port))
        if is_secure:
            self.io_sock = _SSLSocketWrapper(self.sock)
            options['issecure'] = True
        if 'subpl' not in options:
            options['subpl'] = "org.jwebsocket.json"
        self._handshake(hostname, port, resource, **options)


    def close(self, a_reason=None):
        if a_reason==None:
            a_reason = "by client"
        #TODO: fix with the closing handshake especification 1.4
        if self.connected:
            l_opcode = 0x08
            try:
              l_ws_rpacket = WebsocketRawPacket(l_opcode, "BYE FROM THE CLIENT")
              l_ws_rpacket.is_end_frame = True
              self._send_packet_text(l_ws_rpacket)
            except:
              pass
            self._callbacks(self.on_close,a_reason)
            self._closeInternal()


    def send_text(self, a_text):
        l_len = len(a_text)
        l_opcode = 1 #l_opcode equal to 1 because is a frame text type
        if l_len < MAX_FRAME_SIZE:
            l_ws_rpacket = WebsocketRawPacket(l_opcode, a_text)
            l_ws_rpacket.is_end_frame = True
            self._send_packet_text(l_ws_rpacket)
        else:
            self.send_text_maxframesize(a_text, MAX_FRAME_SIZE)


    def send_binary(self, a_binary):
        l_len = len(a_binary)
        l_opcode = 1 #l_opcode equal to 1 because is a frame text type
        if l_len < MAX_FRAME_SIZE:
            l_ws_rpacket = WebsocketRawPacket(l_opcode, a_binary)
            l_ws_rpacket.is_end_frame = True
            self._send_packet_binary(l_ws_rpacket)
        else:
            self.send_binary_maxframesize(a_binary, MAX_FRAME_SIZE)


    def send_text_maxframesize(self, a_text, a_size):
        l_opcode = 0x01
        l_opcode_fragment = 0x00
        l_lenpack = len(a_text)
        if a_size > MAX_FRAME_SIZE:
            a_size = MAX_FRAME_SIZE
        if  l_lenpack <= a_size:
            self.send_text(a_text)
        else:
            frag_list = [a_text[i:i + a_size] for i in range(0, len(a_text), a_size)]
            cont = 0
            for key in frag_list:
                cont = cont+1
                if cont == 1:
                    l_ws_rpacket = WebsocketRawPacket(l_opcode, key)
                    l_ws_rpacket.is_end_frame = False
                elif cont == len(frag_list):
                    l_ws_rpacket = WebsocketRawPacket(l_opcode_fragment, key)
                    l_ws_rpacket.is_end_frame = True
                else:
                    l_ws_rpacket = WebsocketRawPacket(l_opcode_fragment, key)
                    l_ws_rpacket.is_end_frame = False
                self._send_packet_text(l_ws_rpacket)


    def send_binary_maxframesize(self, a_binary, a_size):
        l_opcode = 0x01
        l_opcode_fragment = 0x00
        l_lenpack = len(a_binary)
        if a_size > MAX_FRAME_SIZE:
            a_size = MAX_FRAME_SIZE
        if  l_lenpack <= a_size:
            self.send_binary(a_binary)
        else:
            frag_list = [a_binary[i:i + a_size] for i in range(0, len(a_binary), a_size)]
            cont = 0
            for key in frag_list:
                cont = cont+1
                if cont == 1:
                    l_ws_rpacket = WebsocketRawPacket(l_opcode, key)
                    l_ws_rpacket.is_end_frame = False
                elif cont == len(frag_list):
                    l_ws_rpacket = WebsocketRawPacket(l_opcode_fragment, key)
                    l_ws_rpacket.is_end_frame = True
                else:
                    l_ws_rpacket = WebsocketRawPacket(l_opcode_fragment, key)
                    l_ws_rpacket.is_end_frame = False
                self._send_packet_binary(l_ws_rpacket)


    def ping(self):
        if self.connected:
            l_opcode = 0x09
            l_ws_rpacket = WebsocketRawPacket(l_opcode, "Hello")
            l_ws_rpacket.is_end_frame = True
            self._send_packet_text(l_ws_rpacket)



    def pong(self, text):
        l_oldtmout = self.get_timeout()
        #self.set_timeout(timeout)
        if self.connected:
            l_opcode = 0x0A
            l_ws_rpacket = WebsocketRawPacket(l_opcode, text)
            l_ws_rpacket.is_end_frame = True
            self._send_packet_text(l_ws_rpacket)


    def get_request_header(self):
        return self._request_header


    def get_response_header(self):
        return self._response_header


    def _callbacks(self, callback, *args):
        if callback:
            try:
                callback(*args)
            except Exception, e:
                if logger.isEnabledFor(logging.DEBUG):
                    logger.error(e)


    def _isfinal_fragment(self,firs_bit):
        firs_bit =  bin(firs_bit)[2:].zfill(8)
        if firs_bit[0] == "1":
            return False
        return True


    def _get_opcode(self, first):
        first_bytes =  bin(first)[4:].zfill(8)
        return int(first_bytes, 2)


    def _get_paylen_127(self):
        l_payload_extd = ""
        for i in range(0, 8):
            l_byte  = ord(self._recv(1))
            l_byte  = bin(l_third)[2:].zfill(8)
            l_payload_extd += l_byte
            return int(l_payload_extd,2)


    def _get_payload_length(self,a_secund):
        if a_secund <= 125:
            return a_secund
        elif a_secund == 126:
            l_third  = ord(self._recv(1))
            l_third  = bin(l_third)[2:].zfill(8)
            l_fourth = ord(self._recv(1))
            l_fourth = bin(l_fourth)[2:].zfill(8)
            l_payload_extd = l_third+l_fourth
            return int(l_payload_extd,2)
        elif a_secund == 127:
            return self._get_paylen_127()
        else:
            raise WebSocketException("Invalid payload length")


    def has_mask(self, a_secund):
        l_secund =  bin(a_secund)[4:].zfill(8)
        if l_secund[0] == '1':
            return True
        return False


    def recv(self):
        while True:
          if self.connected == False:
            break
          l_data,l_opcode = self.read_frame()
          if l_data == None:
            break
          if l_opcode == 0x09:
             self.close()
          elif l_opcode == 0x02:
             self._callbacks(self.on_binary_message,l_data)
          elif l_opcode == 0X01:
             self._callbacks(self.on_text_message,l_data)


    def read_frame(self):
        l_byte = self._recv(1)
        if l_byte == None:
            return (None,None)
        l_byte = ord(l_byte)
        l_isfinal_fragment = self._isfinal_fragment(l_byte)
        l_opcode = self._get_opcode(l_byte)
        l_secbyte = self._recv(1)
        if not l_secbyte:
            return (None,None)
        l_secbyte = ord(l_secbyte)
        l_payload = self._get_payload_length(l_secbyte)
        l_hasMask = self.has_mask(l_secbyte)

        if l_isfinal_fragment == False:
            bytes = self._recv_strict(l_payload)
        else:
            bytes  += _recv_strict(l_payload)
            while True:
                l_byte = ord(self._recv(1))
                l_isfinal_fragment = self._isfinal_fragment(l_byte)
                l_opcode = self._get_opcode(l_byte)
                l_secbyte = ord(self._recv(1))
                l_payload = self._get_payload_length(l_secbyte)
                bytes  += _recv_strict(l_payload)
                self._callbacks(self.on_fragment,bytes)
                if l_isfinal_fragment:
                    break
        if l_opcode == 0x09:
            self.pong("stell alive")
            self._callbacks(self.on_ping)
            return (None, None)
        elif l_opcode == 0xA:
            self._callbacks(self.on_pong)
            return (None, None)
        else:
            return (bytes, l_opcode)


    def _recv(self, bufsize):
        try:
          bytes = self.io_sock.recv(bufsize)
          if not bytes:
              raise ConnectionClosedException()
          return bytes
        except:
          return None




    def _recv_strict(self, bufsize):
        remaining = bufsize
        bytes = ""
        while remaining:
            bytes += self._recv(remaining)
            remaining = bufsize - len(bytes)

        return bytes


    def _recv_line(self):
        line = []
        while True:
            c = self._recv(1)
            line.append(c)
            if c == "\n":
                break
        return "".join(line)


    def _read_length(self):
        length = 0
        while True:
            b = ord(self._recv(1))
            length = length * (1 << 7) + (b & 0x7f)
            if b < 0x80:
                break

        return length


    def get_request_header_field(self, a_field):
        l_header = self.get_request_header()
        for key  in l_header:
            pos = key.find(a_field)
            if pos != -1:
                return key[len(a_field)+1:].strip()
        return False


    def get_response_header_field(self, a_field):
        l_header = self.get_response_header()
        l_field  = l_header.get(a_field)
        if l_field == None:
            return False
        return l_field


    def set_timeout(self, timeout):
        self.sock.settimeout(timeout)


    def get_timeout(self):
        return self.sock.gettimeout()


    def _handshake(self, host, port, resource, **options):
        sock = self.io_sock
        handshake_headers = []
        if "header" in options:
            headers.extend(options["header"])

        handshake_headers.append("GET %s HTTP/1.1" % resource)
        handshake_headers.append("Upgrade: websocket")
        handshake_headers.append("Connection: Upgrade")

        key = str(self._create_sec_websocket_key())
        handshake_headers.append("Sec-WebSocket-Key: %s" % key)

        if port == 80:
            hostport = host
        else:
            hostport = "%s:%d" % (host, port)

        if 'issecure' in options:
            fullhost = "https://" + host
        else:
            fullhost = "http://" + host

        handshake_headers.append(
            "Host: %s" % hostport)
        handshake_headers.append(
            "Sec-WebSocket-Origin: %s" % fullhost)
        handshake_headers.append(
            "Sec-WebSocket-Protocol: %s" % options['subpl'])
        handshake_headers.append(
            "Sec-WebSocket-Version: %s" % 13)

        self._request_header = handshake_headers
        header_str = '\r\n'.join(handshake_headers) + '\r\n'
        sock.send(header_str)

        if traceEnabled:
            logger.debug("--- request header ---")
            logger.debug(header_str)
            logger.debug("-----------------------")
        #status of the server response
        #resp_headers is an array with all the headers fields by key
        #value or map
        status, resp_headers = self._read_headers()
        self._response_header = resp_headers
        if status != 101:
            self.close()
            raise WebSocketException("Handshake Status %d" % status)
        #just validate if the key of header match with hybie
        success, secure = self._validate_header(resp_headers)
        if not success:
            self.close()
            raise WebSocketException("Invalid WebSocket Header")

        keyresp = resp_headers.get('Sec-WebSocket-Accept')

        if secure:
            if not self._validate_resp(key, keyresp):
                self.close()
                raise WebSocketException("challenge-response error")

        self.connected = True
        self._callbacks(self.on_open)
        thread.start_new_thread(self.recv,())


    def _create_sec_websocket_key(self):
        key = uuid.uuid5(uuid.NAMESPACE_DNS, 'python.org')
        return key


    def _parse_url(self, url):
        """
        parse url and the result is tuple of
        (hostname, port, resource path and the flag of secure mode)
        """
        parsed = urlparse(url)
        if parsed.hostname:
            hostname = parsed.hostname
        else:
            raise ValueError("hostname is invalid")
        port = 0
        if parsed.port:
            port = parsed.port

        is_secure = False
        if parsed.scheme == "ws":
            if not port:
                port = 80
        elif parsed.scheme == "wss":
            is_secure = True
            if not port:
                port = 443
        else:
            raise ValueError("scheme %s is invalid" % parsed.scheme)

        if parsed.path:
            resource = parsed.path
        else:
            resource = "/"
        """
        hostname = "localhost"
        port = 8787
        resource = "/"
        is_secure = False
        """
        return (hostname, port, resource, is_secure)


    def _validate_resp(self, key, resp):

        akey = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
        digest = hashlib.sha1(akey).digest()
        adigest = base64.b64encode(digest)
        return  adigest == resp


    def _validate_header(self, headers):
        #check the commun headers(upgrade,connection)
        for key, value in HEADERS_TO_CHECK.iteritems():
            v = headers.get(key, None)
            if value != v:
                return False, False

        success = 0
        #each field of the header match wit hybie array
        for key in HEADERS_TO_EXIST_FOR_HYBI:
            if key in headers:
                success += 1

        #if there are the same amount of header field for hybie
        if success == len(HEADERS_TO_EXIST_FOR_HYBI):
            return True, True
        elif success != 0:
            return False, True

        return False, False


    def _read_headers(self):
        status = None
        headers = {}
        if traceEnabled:
            logger.debug("--- response header ---")

        while True:
            line = self._recv_line()
            if line == "\r\n":
                break
            line = line.strip()
            if traceEnabled:
                logger.debug(line)
            if not status:
                status_info = line.split(" ", 2)
                status = int(status_info[1])
            else:
                kv = line.split(":", 1)
                if len(kv) == 2:
                    key, value = kv
                    headers[key] = value.strip()
                else:
                    raise WebSocketException("Invalid header")

        if traceEnabled:
            logger.debug("-----------------------")

        return status, headers


    def _send_packet_text(self, a_package):
        """
        Send the data as string. payload must be utf-8 string or unicoce.
        """
        l_wsrawpacket = headers.raw_to_protocol_packet(a_package)
        str_loc = headers.stringfy_rawpakect(l_wsrawpacket)
        self.io_sock.send(str_loc)


    def _send_packet_binary(self, a_package):
        """
        Send the data as string. payload must be utf-8 string or unicoce.
        """
        l_wsrawpacket = headers.raw_to_protocol_packet(a_package)
        str_loc = headers.stringfy_rawpakect(l_wsrawpacket)
        self.io_sock.send(str_loc)


    def _closeInternal(self):
        self.connected = False
        self.sock.close()
        self.io_sock = self.sock
        self.io_sock = self.sock = socket.socket()
