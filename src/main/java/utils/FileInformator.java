package utils;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.VerRsrc;
import com.sun.jna.platform.win32.Version;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.io.File;

class FileInformator {
    String getFileVersion(String pathname, String infoType) {
        System.out.println("pathname: " + pathname);
        System.out.println("infoType: " + infoType);
        return readFileInfo(pathname, infoType);
    }

    private String readFileInfo(String pathname, String infoType) {
        File fileToCheck = new File(pathname);
        short[] rtnData = new short[4];
        String fileInfo = null;

        int infoSize = Version.INSTANCE.GetFileVersionInfoSize(fileToCheck.getAbsolutePath(), null);
        Pointer buffer = Kernel32.INSTANCE.LocalAlloc(WinBase.LMEM_ZEROINIT, infoSize);

        Version.INSTANCE.GetFileVersionInfo(fileToCheck.getAbsolutePath(), 0, infoSize, buffer);
        IntByReference outputSize = new IntByReference();
        PointerByReference pointer = new PointerByReference();
        Version.INSTANCE.VerQueryValue(buffer, "\\", pointer, outputSize);
        VerRsrc.VS_FIXEDFILEINFO fileInfoStructure = new VerRsrc.VS_FIXEDFILEINFO(pointer.getValue());

        // Версия файла
        if (infoType.contains("FileVersion")) {
            rtnData[0] = (short) (fileInfoStructure.dwFileVersionMS.longValue() >> 16);
            rtnData[1] = (short) (fileInfoStructure.dwFileVersionMS.longValue() & 0xffff);
            rtnData[2] = (short) (fileInfoStructure.dwFileVersionLS.longValue() >> 16);
            rtnData[3] = (short) (fileInfoStructure.dwFileVersionLS.longValue() & 0xffff);

            fileInfo = toStandardVersionFormat(rtnData);
        }

        return fileInfo;
    }

    private String toStandardVersionFormat(short[] data) {
        StringBuilder info = new StringBuilder();

        for (int i = 0; i < data.length; i++) {
            info.append(data[i]);
            if (i == data.length - 1) {
                break;
            }
            info.append(".");
        }

        return info.toString();
    }
}
