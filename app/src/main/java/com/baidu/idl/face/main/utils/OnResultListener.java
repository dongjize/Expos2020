package com.baidu.idl.face.main.utils;


import com.baidu.idl.face.main.exception.FaceError;

public interface OnResultListener<T> {
    void onResult(T result);

    void onError(FaceError error);
}

