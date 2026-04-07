package com.jstyle.blesdk2208a.callback;


import com.jstyle.blesdk2208a.model.Device;

public interface OnScanResults {
  void Success(Device date);
  void Fail(int code);
}
