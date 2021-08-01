package com.rkrzmail.oto.modules.Adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NikitaViewHolder extends RecyclerView.ViewHolder {
    public View v ;
    public NikitaViewHolder(@NonNull View itemView) {
        super(itemView);
        v = itemView;
    }
    public static NikitaViewHolder getInstance(ViewGroup viewParent, int view){
        View v = LayoutInflater.from(viewParent.getContext()).inflate(view, viewParent, false);
        return new NikitaViewHolder(v);
    }
    public <T extends View> T find(int id) {
        return (T) v.findViewById(id);
    }
    public <T extends View> T find(int id, Class<? super T> s) {
        return (T) v.findViewById(id);
    }
    public <T extends View> T findView(View v, int id, Class<? super T> s) {
        return (T) v.findViewById(id);
    }


}
