package bt.search.analyzer.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class TypeConvert {
    public static List<Long> stringToLongList(String rolesStr) {
        if (rolesStr == null) {
            return null;
        }
        List<Long> roleList = null;
        if (!"".equals(rolesStr)) {
            String[] roleIdStrArray = rolesStr.split(",");
            roleList = Arrays.stream(roleIdStrArray)
                    .map(item -> Long.valueOf(item))
                    .collect(Collectors.toList());
        }

        return roleList;
    }

    public static String longListToString(List<Long> longs) {
        return TypeConvert.listToStr(longs);
    }

    public static String strListToString(List<String> strings) {
        return TypeConvert.listToStr(strings);
    }

    public static List<String> stringToStrList(String rolesStr) {
        if (rolesStr == null) {
            return null;
        }
        List<String> roleList = null;
        if (!"".equals(rolesStr)) {
            String[] roleIdStrArray = rolesStr.split(",");
            roleList = Arrays.asList(roleIdStrArray);
        }

        return roleList;
    }

    public static <T> String listToStr(List<T> list) {
        if (list.size() == 0) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        list.stream().map(item -> item + ",").forEach(buffer::append);
        buffer.deleteCharAt(buffer.length() - 1);
        return buffer.toString();
    }

    public static byte[] int2Bytes(int integer) {
        byte[] bytes=new byte[4];
        bytes[0]=(byte) (integer>>24);
        bytes[1]=(byte) (integer>>16);
        bytes[2]=(byte) (integer>>8);
        bytes[3]=(byte) integer;

        return bytes;
    }
}
