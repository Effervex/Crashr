package bbw.com.crashr;

import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sam on 4/07/2015.
 */
public class CauseHelper {
    private Map<String, String> appendix_;

    public CauseHelper(Resources resources) {
        appendix_ = new HashMap<>();
        loadAppendix(resources);
    }

    private void loadAppendix(Resources resources) {
        InputStream inStr = resources.openRawResource(R.raw.appendix_b);
        BufferedReader in = new BufferedReader(new InputStreamReader(inStr));
        String input = null;
        try {
            while ((input = in.readLine()) != null) {
                String[] split = input.split(",", 2);
                appendix_.put(split[0], split[1]);
            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCauseString(String causeCode) {
        if (causeCode.length() > 3)
            causeCode = causeCode.substring(0, 3);
        String causeStr = appendix_.get(causeCode);
        return causeStr;
    }

    public String getCategory(String causeCode) {
        String causeStr = getCauseString(causeCode);
        if (causeStr != null) {
            int index = causeStr.indexOf(":");
            return causeStr.substring(0, index).trim();
        }
        return "ERROR";
    }

    public String getCause(String causeCode) {
        String causeStr = getCauseString(causeCode);
        if (causeStr != null) {
            int index = causeStr.indexOf(":");
            return causeStr.substring(index + 1).trim();
        }
        return "ERROR";
    }
}
