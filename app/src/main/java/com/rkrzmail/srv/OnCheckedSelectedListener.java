package com.rkrzmail.srv;

import java.util.List;

public interface OnCheckedSelectedListener {

    void getSelectedItem(List<String> items, int position);

    void removeSelectedItem(List<String> items, int position);

}
