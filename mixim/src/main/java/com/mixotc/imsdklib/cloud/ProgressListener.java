package com.mixotc.imsdklib.cloud;

/**
 * Created by junnikokuki on 2017/9/20.
 */

public interface ProgressListener {
    void update(long bytesFinished, long contentLength, boolean done);
}
