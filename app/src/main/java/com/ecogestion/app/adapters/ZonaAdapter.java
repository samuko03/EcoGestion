package com.ecogestion.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecogestion.app.R;
import com.ecogestion.app.models.Zona;

import java.util.List;

public class ZonaAdapter extends RecyclerView.Adapter<ZonaAdapter.ZonaViewHolder> {

    private final Context context;
    private List<Zona> lista;
    private OnZonaListener listener;
    private boolean puedeEditar = true;

    public interface OnZonaListener {
        void onEditar(Zona zona);
        void onEliminar(Zona zona);
    }

    public ZonaAdapter(Context context, List<Zona> lista, OnZonaListener listener) {
        this.context = context;
        this.lista = lista;
        this.listener = listener;
    }

    public ZonaAdapter(Context context, List<Zona> lista, OnZonaListener listener,
                       boolean puedeEditar) {
        this(context, lista, listener);
        this.puedeEditar = puedeEditar;
    }

    public void actualizarLista(List<Zona> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ZonaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_zona, parent, false);
        return new ZonaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ZonaViewHolder holder, int position) {
        Zona zona = lista.get(position);
        holder.txtNombre.setText(zona.getNombre());
        holder.txtUbicacion.setText(zona.getUbicacionCompleta());
        holder.txtEstado.setText(zona.getEstado());
        holder.txtResponsable.setText("Responsable: " +
                (zona.getResponsableNombre() != null ? zona.getResponsableNombre() : "Sin asignar"));

        // Color del chip según estado
        switch (zona.getEstado()) {
            case "ACTIVA":
                holder.txtEstado.setBackgroundResource(R.drawable.bg_estado_activa);
                break;
            case "FINALIZADA":
                holder.txtEstado.setBackgroundResource(R.drawable.bg_estado_finalizada);
                break;
            default:
                holder.txtEstado.setBackgroundResource(R.drawable.bg_estado_pendiente);
                break;
        }

        if (puedeEditar) {
            holder.btnEditar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setVisibility(View.VISIBLE);
            holder.btnEditar.setOnClickListener(v -> listener.onEditar(zona));
            holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(zona));
        } else {
            holder.btnEditar.setVisibility(View.GONE);
            holder.btnEliminar.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    static class ZonaViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtUbicacion, txtEstado, txtResponsable;
        View btnEditar, btnEliminar;

        ZonaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreZona);
            txtUbicacion = itemView.findViewById(R.id.txtUbicacionZona);
            txtEstado = itemView.findViewById(R.id.txtEstadoZona);
            txtResponsable = itemView.findViewById(R.id.txtResponsableZona);
            btnEditar = itemView.findViewById(R.id.btnEditarZona);
            btnEliminar = itemView.findViewById(R.id.btnEliminarZona);
        }
    }
}
