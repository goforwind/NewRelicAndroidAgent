// 
// Decompiled by Procyon v0.5.30
// 

package com.newrelic.agent.android.util;

import com.newrelic.agent.android.logging.AgentLogManager;
import android.os.Environment;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import org.json.JSONException;
import java.io.IOException;
import java.io.FileNotFoundException;
import org.json.JSONObject;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import com.newrelic.agent.android.analytics.AnalyticsControllerImpl;
import com.newrelic.agent.android.analytics.AnalyticAttribute;
import com.newrelic.agent.android.stats.StatsEngine;
import java.util.Arrays;
import android.telephony.TelephonyManager;
import android.provider.Settings;
import android.os.Build;
import java.util.UUID;
import android.text.TextUtils;
import android.content.Context;
import com.newrelic.agent.android.logging.AgentLog;
import java.io.File;

public class PersistentUUID
{
    private static final String UUID_KEY = "nr_uuid";
    private static final String UUID_FILENAME = "nr_installation";
    public static final String METRIC_UUID_RECOVERED = "UUIDRecovered";
    private static File UUID_FILE;
    private static AgentLog log;
    
    public PersistentUUID(final Context context) {
        PersistentUUID.UUID_FILE = new File(context.getFilesDir(), "nr_installation");
    }
    
    public String getDeviceId(final Context context) {
        String id = this.generateUniqueID(context);
        if (TextUtils.isEmpty((CharSequence)id)) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }
    
    private String generateUniqueID(final Context context) {
        String hardwareDeviceId = Build.SERIAL;
        String androidDeviceId = Build.ID;
        String uuid;
        try {
            androidDeviceId = Settings.Secure.getString(context.getContentResolver(), "android_id");
            if (!TextUtils.isEmpty((CharSequence)androidDeviceId)) {
                try {
                    final TelephonyManager tm = (TelephonyManager)context.getSystemService("phone");
                    if (tm != null) {
                        hardwareDeviceId = tm.getDeviceId();
                    }
                }
                catch (Exception e) {
                    hardwareDeviceId = "badf00d";
                }
                if (TextUtils.isEmpty((CharSequence)hardwareDeviceId)) {
                    hardwareDeviceId = Build.HARDWARE + Build.DEVICE + Build.BOARD + Build.BRAND;
                }
                uuid = this.intToHexString(androidDeviceId.hashCode(), 8) + "-" + this.intToHexString(hardwareDeviceId.hashCode(), 4) + "-" + this.intToHexString(Build.VERSION.SDK_INT, 4) + "-" + this.intToHexString(Build.VERSION.RELEASE.hashCode(), 12);
                throw new RuntimeException("Not supported (TODO)");
            }
            uuid = UUID.randomUUID().toString();
        }
        catch (Exception e) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }
    
    private String intToHexString(final int value, final int sublen) {
        String result = "";
        String string = Integer.toHexString(value);
        final int remain = sublen - string.length();
        final char[] chars = new char[remain];
        Arrays.fill(chars, '0');
        string = new String(chars) + string;
        int count = 0;
        for (int i = string.length() - 1; i >= 0; --i) {
            ++count;
            result = string.substring(i, i + 1) + result;
            if (0 == count % sublen) {
                result = "-" + result;
            }
        }
        if (result.startsWith("-")) {
            result = result.substring(1, result.length());
        }
        return result;
    }
    
    protected void noticeUUIDMetric(final String tag) {
        final StatsEngine statsEngine = StatsEngine.get();
        if (statsEngine != null) {
            statsEngine.inc("Supportability/AgentHealth/" + tag);
        }
        else {
            PersistentUUID.log.error("StatsEngine is null. " + tag + "  not recorded.");
        }
    }
    
    public String getPersistentUUID() {
        String uuid = this.getUUIDFromFileStore();
        if (!TextUtils.isEmpty((CharSequence)uuid)) {
            this.noticeUUIDMetric("UUIDRecovered");
        }
        else {
            uuid = UUID.randomUUID().toString();
            PersistentUUID.log.info("Created random UUID: " + uuid);
            StatsEngine.get().inc("Mobile/App/Install");
            final AnalyticAttribute attribute = new AnalyticAttribute("install", true);
            AnalyticsControllerImpl.getInstance().addAttributeUnchecked(attribute, false);
            this.setPersistedUUID(uuid);
        }
        return uuid;
    }
    
    protected void setPersistedUUID(final String uuid) {
        this.putUUIDToFileStore(uuid);
    }
    
    protected String getUUIDFromFileStore() {
        String uuid = "";
        if (PersistentUUID.UUID_FILE.exists()) {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(PersistentUUID.UUID_FILE));
                final String uuidJson = in.readLine();
                final JSONObject jsonObject = new JSONObject(uuidJson);
                uuid = jsonObject.getString("nr_uuid");
            }
            catch (FileNotFoundException e) {
                PersistentUUID.log.error(e.getMessage());
            }
            catch (IOException e2) {
                PersistentUUID.log.error(e2.getMessage());
            }
            catch (JSONException e3) {
                PersistentUUID.log.error(e3.getMessage());
            }
            catch (NullPointerException e4) {
                PersistentUUID.log.error(e4.getMessage());
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (IOException e5) {
                        PersistentUUID.log.error(e5.getMessage());
                    }
                }
            }
        }
        return uuid;
    }
    
    protected void putUUIDToFileStore(final String uuid) {
        BufferedWriter out = null;
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("nr_uuid", (Object)uuid);
            out = new BufferedWriter(new FileWriter(PersistentUUID.UUID_FILE));
            out.write(jsonObject.toString());
            out.flush();
        }
        catch (IOException e) {
            PersistentUUID.log.error(e.getMessage());
        }
        catch (JSONException e2) {
            PersistentUUID.log.error(e2.getMessage());
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException e3) {
                    PersistentUUID.log.error(e3.getMessage());
                }
            }
        }
    }
    
    static {
        PersistentUUID.UUID_FILE = new File(Environment.getDataDirectory(), "nr_installation");
        PersistentUUID.log = AgentLogManager.getAgentLog();
    }
}
