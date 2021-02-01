package com.rkrzmail.srv;

import android.support.v7.widget.SearchView;

import com.rkrzmail.oto.AppActivity;

import java.util.List;

public class SearchListener {


    public interface IFragmentListener {
        void addiSearch(ISearch iSearch, ISearchAutoComplete autoComplete);
        void removeISearch(ISearch iSearch, ISearchAutoComplete autoComplete);
    }

    public interface ISearch {
        void onTextQuery(String text);
    }

    public interface ISearchAutoComplete{
        void attachAdapter(SearchView.SearchAutoComplete searchAutoComplete);
    }
}
