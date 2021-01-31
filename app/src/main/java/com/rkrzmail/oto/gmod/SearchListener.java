package com.rkrzmail.oto.gmod;

import java.util.List;

public class SearchListener {

    public interface IDataCallback {
        void onFragmentCreated(List<String> listData);
    }

    public interface IFragmentListener {
        void addiSearch(ISearch iSearch);
        void removeISearch(ISearch iSearch);
    }

    public interface ISearch {
        void onTextQuery(String text);
    }
}
