//	---------------------------------------------------------------------------
//	jWebSocket - TimeoutOutputStreamNIOWriter
//	Copyright (c) 2011 Alexander Schulze, Innotrade GmbH
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using ClientLibrary.org.jwebsocket.client.config;
using ClientLibrary.org.jwebsocket.client.api;
using System.IO;
using System.Net.Sockets;

namespace ClientLibrary.org.jwebsocket.client.kit
{
    /// <summary>
    /// Author Rolando Betancourt Toucet
    /// </summary>
    public class WebSocketProtocolAbstraction
    {
        private static Random mRan = new Random();

        public static bool isHybiVersion(int aVersion)
        {
            return WebSocketConstants.WS_SUPPORTED_HYBI_VERSIONS.Contains(aVersion);
        }

        public static byte[] RawToProtocolPacket(WebSocketPacket aDataPacket)
        {
            byte[] lBuff = new byte[2];
            WebSocketFrameType lFrameType = aDataPacket.GetFrameType();
            int lTargetType = (int)lFrameType;

            if (lTargetType.Equals(-1))
                throw new WebSocketRuntimeException("Cannot construct a packet with unknown packet type: " + lFrameType);

            if(aDataPacket.IsEndFrame())
                lBuff[0] = (byte)(lTargetType | 0x80);
            else
                lBuff[0] = (byte)(lTargetType);

            int lPayloadLen = aDataPacket.GetByteArray().Length;

            if (lPayloadLen < 126)
                lBuff[1] = (byte)(lPayloadLen | 0x80);
            else if (lPayloadLen >= 126 && lPayloadLen < 0xFFFF)
            {
                lBuff[1] = (byte)(126 | 0x80);
                int lSize = lBuff.Length;
                lBuff = CopyOf(lBuff, lSize + 2);
                lBuff[lSize] = (byte)((lPayloadLen >> 8) & 0xFF);
                lBuff[lSize + 1] = (byte)(lPayloadLen & 0xFF);
            }
            else if (lPayloadLen >= 0xFFFF)
            {
                lBuff[1] = (byte)(127 | 0x80);
                long lLen = (long)lPayloadLen;
                int lSize = lBuff.Length;
                lBuff = CopyOf(lBuff, lSize + 8);
                lBuff[lSize] = (byte)(lLen >> 56);
                lBuff[lSize + 1] = (byte)(lLen >> 48);
                lBuff[lSize + 2] = (byte)(lLen >> 40);
                lBuff[lSize + 3] = (byte)(lLen >> 32);
                lBuff[lSize + 4] = (byte)(lLen >> 24);
                lBuff[lSize + 5] = (byte)(lLen >> 16);
                lBuff[lSize + 6] = (byte)(lLen >> 8);
                lBuff[lSize + 7] = (byte)lLen;
            }

            int lSizes = lBuff.Length;
            lBuff = CopyOf(lBuff, lSizes + 4);
            lBuff[lSizes] = (byte)mRan.Next(0, 255);
            lBuff[lSizes + 1] = (byte)mRan.Next(0, 255);
            lBuff[lSizes + 2] = (byte)mRan.Next(0, 255);
            lBuff[lSizes + 3] = (byte)mRan.Next(0, 255);

            byte[] lMask = new byte[4];
            Array.Copy(lBuff, lBuff.Length - 4, lMask, 0, 4);

            byte[] lBuffData = new byte[aDataPacket.GetByteArray().Length];
            Array.Copy(aDataPacket.GetByteArray(),0, lBuffData,0,lBuffData.Length);
            byte[] lMaskedData = new byte[lBuffData.Length];
            int lPos = 0;
            for (int i = 0; i < lBuffData.Length; i++)
            {
                lMaskedData[i] = (byte)Convert.ToInt32(lBuffData[i] ^ lMask[lPos]);
                if (lPos == 3)
                    lPos = 0;
                else
                    lPos++;
            }

            lBuff = CopyOf(lBuff, lBuff.Length + lMaskedData.Length);
            Array.Copy(lMaskedData, 0, lBuff, lBuff.Length - lMaskedData.Length, lMaskedData.Length);
            return lBuff;
        }

        public static WebSocketPacket protocolToRawPacket(Stream aIS)
        {
            int lFlags = aIS.ReadByte();
            if (lFlags == -1)
                return null;

            byte[] lBuff = new byte[0];

            bool lFragmented = false;

            if ((lFlags & 0x80) == 0x00)
                lFragmented = true;

            bool lMasked = true;
            int[] lMask = new int[4];

            int lOpcode = lFlags & 0x0F;

            WebSocketFrameType lFrameType = (WebSocketFrameType)lOpcode;

            if (lFrameType.Equals(WebSocketFrameType.INVALID))
            {
                throw new WebSocketException("Invalid frame type");
            }
            else
            {
                long lPayloadLen = Read(aIS);

                lMasked = (lPayloadLen & 0x80) == 0x80;
                lPayloadLen &= 0x7F;

                if (lPayloadLen == 126)
                {
                    lPayloadLen = Read(aIS) & 0xFF;
                    lPayloadLen = (lPayloadLen << 8) ^ (Read(aIS) & 0xFF);
                }
                else if (lPayloadLen == 127)
                {

                    lPayloadLen = Read(aIS) & 0xFF;
                    lPayloadLen = (lPayloadLen << 8) ^ (Read(aIS) & 0xFF);
                    lPayloadLen = (lPayloadLen << 8) ^ (Read(aIS) & 0xFF);
                    lPayloadLen = (lPayloadLen << 8) ^ (Read(aIS) & 0xFF);
                    lPayloadLen = (lPayloadLen << 8) ^ (Read(aIS) & 0xFF);
                    lPayloadLen = (lPayloadLen << 8) ^ (Read(aIS) & 0xFF);
                    lPayloadLen = (lPayloadLen << 8) ^ (Read(aIS) & 0xFF);
                    lPayloadLen = (lPayloadLen << 8) ^ (Read(aIS) & 0xFF);
                }

                if (lMasked)
                {
                    lMask[0] = Read(aIS) & 0xFF;
                    lMask[1] = Read(aIS) & 0xFF;
                    lMask[2] = Read(aIS) & 0xFF;
                    lMask[3] = Read(aIS) & 0xFF;
                }

                if (lPayloadLen > 0)
                {
                    if (lMasked)
                    {
                        int j = 0;
                        while (lPayloadLen-- > 0)
                        {
                            WriteByte(ref lBuff, Convert.ToByte(Read(aIS) ^ lMask[j]));
                            j++;
                            j &= 3;
                        }
                    }
                    else
                    {
                        while (lPayloadLen-- > 0)
                        {
                            WriteByte(ref lBuff, (byte)Read(aIS));
                        }
                    }
                }
            }
            WebSocketPacket lRes = new WebSocketRawPacket(lFrameType, lBuff);
            return lRes;
        }

        private static byte[] CopyOf(byte[] aOriginal, int aNewLength)
        {
            byte[] lCopy = new byte[aNewLength];
            Array.Copy(aOriginal, 0, lCopy, 0, Math.Min(aOriginal.Length, aNewLength));
            return lCopy;
        }

        public static int Read(Stream aIS)
        {
            int lByte = aIS.ReadByte();
            if (lByte < 0)
                throw new Exception("EOF");
            return lByte;
        }

        public static void WriteByte(ref byte [] aArray,byte aByte)
        {
            byte[] lArray = new byte[aArray.Length + 1];
            Array.Copy(aArray, lArray, aArray.Length);
            lArray[lArray.Length - 1] = aByte;
            aArray = lArray;
        }
    }
}
