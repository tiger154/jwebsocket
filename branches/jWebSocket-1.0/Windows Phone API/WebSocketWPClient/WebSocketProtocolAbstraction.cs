using System;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using ClientLibrary.org.jwebsocket.client.api;
using ClientLibrary.org.jwebsocket.client.kit;
using ClientLibrary.org.jwebsocket.client.config;

namespace WebSocketWPClient
{
    internal class WebSocketProtocolAbstraction
    {
        private static Random mRan = new Random();

        internal static bool isHybiVersion(int aVersion)
        {
            return WebSocketConstants.WS_SUPPORTED_HYBI_VERSIONS.Contains(aVersion);
        }

        internal static byte[] RawToProtocolPacket(WebSocketPacket aDataPacket)
        {
            byte[] lBuff = new byte[2];
            WebSocketFrameType lFrameType = aDataPacket.GetFrameType();
            int lTargetType = (int)lFrameType;

            if (lTargetType.Equals(-1))
                throw new WebSocketRuntimeException("Cannot construct a packet with unknown packet type: " + lFrameType);

            if (aDataPacket.IsEndFrame())
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
            Array.Copy(aDataPacket.GetByteArray(), 0, lBuffData, 0, lBuffData.Length);
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

        private static byte[] CopyOf(byte[] aOriginal, int aNewLength)
        {
            byte[] lCopy = new byte[aNewLength];
            Array.Copy(aOriginal, 0, lCopy, 0, Math.Min(aOriginal.Length, aNewLength));
            return lCopy;
        }

        internal static void WriteByte(ref byte[] aArray, byte aByte)
        {
            byte[] lArray = new byte[aArray.Length + 1];
            Array.Copy(aArray, lArray, aArray.Length);
            lArray[lArray.Length - 1] = aByte;
            aArray = lArray;
        }
    }
}
