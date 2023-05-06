package bt.search.jsoup.common;

public class TorrentUtils {
    public static String torrentSplitHex(String torrent) {
        return torrent.substring(20);
    }

    public static Long inMB(String size) {
        int strSize = size.length() - 2;
        String numStr = size.substring(0, strSize);
        String[] nums = numStr.split("\\.");
        Long longSize;

        if (nums.length == 1) {
            longSize = Long.valueOf(nums[0]) * 100;  // 没有小数点
        } else {
            longSize = Long.valueOf(nums[0]) * 100 + Long.valueOf(nums[1]);
        }

        if (size.contains("GB")) {
            longSize *= 1024;
        }  else if (size.contains("TB")) {
            longSize *= 1024 * 1024;
        }

        return longSize;
    }
}
