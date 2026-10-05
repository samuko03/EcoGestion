package com.ecogestion.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecogestion.app.R;
import com.ecogestion.app.models.Especie;

import java.util.List;

public class EspecieAdapter extends RecyclerView.Adapter<EspecieAdapter.EspecieViewHolder> {

    private final Context context;
    private List<Especie> lista;
    private final OnEspecieListener listener;
    private boolean puedeEditar = true;

    public interface OnEspecieListener {
        void onEditar(Especie especie);
        void onEliminar(Especie especie);
    }

    public EspecieAdapter(Context context, List<Especie> lista, OnEspecieListener listener) {
        this.context = context;
        this.lista = lista;
        this.listener = listener;
    }

    public EspecieAdapter(Context context, List<Especie> lista, OnEspecieListener listener,
                          boolean puedeEditar) {
        this(context, lista, listener);
        this.puedeEditar = puedeEditar;
    }

    public void actualizarLista(List<Especie> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EspecieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_especie, parent, false);
        return new EspecieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EspecieViewHolder holder, int position) {
        Especie e = lista.get(position);
        holder.txtNombre.setText(e.getNombre());
        holder.txtNombreCientifico.setText(e.getNombreCientifico() != null ? e.getNombreCientifico() : "");
        holder.txtEcorregion.setText("Ecorregión: " + (e.getEcorregion() != null ? e.getEcorregion() : "—"));
        holder.txtStock.setText(e.getStockTexto());
        holder.txtStockEstado.setText(e.getStockEstado());

        switch (e.getStockEstado()) {
            case "DISPONIBLE":
                holder.txtStockEstado.setBackgroundResource(R.drawable.bg_estado_activa);
                break;
            case "BAJO":
                holder.txtStockEstado.setBackgroundResource(R.drawable.bg_estado_pendiente);
                break;
            default:
                holder.txtStockEstado.setBackgroundResource(R.drawable.bg_estado_finalizada);
                break;
        }

        if (puedeEditar) {
            holder.btnEditar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setVisibility(View.VISIBLE);
            holder.btnEditar.setOnClickListener(v -> listener.onEditar(e));
            holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(e));
        } else {
            holder.btnEditar.setVisibility(View.GONE);
            holder.btnEliminar.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return lista != null ? lista.size() : 0; }

    static class EspecieViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtNombreCientifico, txtEcorregion, txtStock, txtStockEstado;
        View btnEditar, btnEliminar;

        EspecieViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreEspecie);
            txtNombreCientifico = itemView.findViewById(R.id.txtNombreCientifico);
            txtEcorregion = itemView.findViewById(R.id.txtEcorregion);
            txtStock = itemView.findViewById(R.id.txtStock);
            txtStockEstado = itemView.findViewById(R.id.txtStockEstado);
            btnEditar = itemView.findViewById(R.id.btnEditarEspecie);
            btnEliminar = itemView.findViewById(R.id.btnEliminarEspecie);
        }
    }
}
