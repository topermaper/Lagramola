package com.marcosedo.lagramola;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class ListaEventosCustomAdapter extends ArrayAdapter<Evento> {

    private Context context;
    private List<Evento> items;


    public ListaEventosCustomAdapter(Context context, int resourceId, List<Evento> items, View.OnClickListener listener) {
        super(context, resourceId, items);
        this.context = context;
        this.items = items;

    }

    @Override
    public int getCount() {
        return (this.items.size() - getHiddenCount());
    }

    private int getHiddenCount() {
        int count = 0;
        Evento evento;
        for (int i = 0; i < items.size(); i++) {
            evento = items.get(i);

            if (evento.getHided() == true)
                count++;
        }
        return count;
    }

    private int getRealPosition(int position) {
        int hElements = getHiddenCountUpTo(position);
        int diff = 0;

        for (int i = 0; i < hElements; i++) {
            diff++;
            if (items.get(position + diff).getHided())
                i--;
        }
        return (position + diff);
    }

    private int getHiddenCountUpTo(int location) {
        int count = 0;
        for (int i = 0; i <= location; i++) {
            if (items.get(i).getHided())
                count++;
        }
        return count;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int position = getRealPosition(index);
        Evento rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.linea_lista_eventos, null);

            holder.layout = (RelativeLayout) convertView.findViewById((R.id.relativeLayout));
            holder.ivCartel = (ImageView) convertView.findViewById(R.id.ivCartel);
            holder.tvFecha = (TextView) convertView.findViewById(R.id.tvFecha);
            holder.tvHora = (TextView) convertView.findViewById(R.id.tvHora);
            holder.tvTitulo = (TextView) convertView.findViewById(R.id.tvTitulo);
            holder.tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);

            holder.tvSeparator = (TextView) convertView.findViewById(R.id.tvSeparator);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        /*
        if (rowItem.getFollowed() == 1) {
            holder.ivStarOff.setVisibility(View.GONE);
            holder.ivStarOn.setVisibility(View.VISIBLE);
        } else {
            holder.ivStarOff.setVisibility(View.VISIBLE);
            holder.ivStarOn.setVisibility(View.GONE);
        }*/

        if (rowItem.getSeparatorText().equals("")) {
            holder.tvSeparator.setVisibility(View.GONE);
        } else {
            holder.tvSeparator.setVisibility(View.VISIBLE);
            holder.tvSeparator.setText(rowItem.getSeparatorText());
        }

        if (rowItem.getThumb() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(rowItem.getThumb(), 0, rowItem.getThumb().length);
            holder.ivCartel.setImageBitmap(bitmap);
        } else holder.ivCartel.setImageBitmap(null);
        ////////////////////////////////////////////////////////////7

        holder.tvFecha.setText(rowItem.getFecha());
        holder.tvHora.setText(rowItem.getHora());
        holder.tvTitulo.setText(rowItem.getTitulo());
        holder.tvDescription.setText(rowItem.getDescription());

        return convertView;
    }


    /*private view holder class*/
    private class ViewHolder {
        RelativeLayout layout;
        ImageView ivCartel;
        TextView tvFecha;
        TextView tvHora;
        TextView tvTitulo;
        TextView tvDescription;
        ImageView ivStarOn;
        ImageView ivStarOff;
        TextView tvSeparator;
    }
}