package com.w3engineers.meshrnd.wifi;

/**
 * Created by LENOVO on 22-Jun-16.
 */
public interface ScanListener {
    void join(String jsonString, String ip);
    void update(String jsonString);
    void scanFinish();
}
