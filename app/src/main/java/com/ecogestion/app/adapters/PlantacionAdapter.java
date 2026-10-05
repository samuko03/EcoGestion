package com.ecogestion.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecogestion.app.R;
import com.ecogestion.app.models.Plantacion;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class PlantacionAdapter extends RecyclerView.Adapter<PlantacionAdapter.PlantacionViewHolder> {

    public interface OnPlantacionListener {
        void onEditar(Plantacion plantacion);
        void onEliminar(Plantacion plantacion);
    }

    private final List<Plantacion> lista;
    private final OnPlantacionListener listener;
    private boolean puedeEditar   = true;
    private boolean puedeEliminar = true;

    public PlantacionAdapter(List<Plantacion> lista, OnPlantacionListener listener) {
        this.lista    = lista;
        this.listener = listener;
    }

    public PlantacionAdapter(List<Plantacion> lista, OnPlantacionListener listener,
                             boolean puedeEditar, boolean puedeEliminar) {
        this(lista, listener);
        this.puedeEditar   = puedeEditar;
        this.puedeEliminar = puedeEliminar;
    }

    @NonNull
    @Override
    public PlantacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plantacion, parent, false);
        return new PlantacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantacionViewHolder holder, int position) {
        holder.bind(lista.get(position), listener, puedeEditar, puedeEliminar);
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class PlantacionViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtNombre;
        private final TextView txtEstadoChip;
        private final TextView txtZona;
        private final TextView txtEspecie;
        private final TextView txtCantidad;
        private final TextView txtFecha;
        private final TextView txtResponsable;
        private final MaterialButton btnEditar;
        private final MaterialButton btnEliminar;

        PlantacionViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre      = itemView.findViewById(R.id.txtNombrePlantacion);
            txtEstadoChip  = itemView.findViewById(R.id.txtEstadoChip);
            txtZona        = itemView.findViewById(R.id.txtZonaPlantacion);
            txtEspecie     = itemView.findViewById(R.id.txtEspeciePlantacion);
            txtCantidad    = itemView.findViewById(R.id.txtCantidadPlantacion);
            txtFecha       = itemView.findViewById(R.id.txtFechaPlantacion);
            txtResponsable = itemView.findViewById(R.id.txtResponsablePlantacion);
            btnEditar      = itemView.findViewById(R.id.btnEditarPlantacion);
            btnEliminar    = itemView.findViewById(R.id.btnEliminarPlantacion);
        }

        void bind(Plantacion p, OnPlantacionListener listener,
                  boolean puedeEditar, boolean puedeEliminar) {
            txtNombre.setText(p.getNombre());
            txtZona.setText(p.getZonaNombre() != null ? p.getZonaNombre() : "Sin zona");
            txtEspecie.setText(p.getEspecieNombre() != null ? p.getEspecieNombre() : "Sin especie");
            txtCantidad.setText("🌱 " + p.getCantidadArboles() + " árboles");
            txtFecha.setText(p.getFechaPlantacion() != null && !p.getFechaPlantacion().isEmpty()
                    ? p.getFechaPlantacion() : "Sin fecha");

            String resp = p.getResponsableNombre();
            txtResponsable.setText("Responsable: " + (resp != null && !resp.trim().isEmpty() ? resp : "Sin asignar"));

            // Chip de estado
            txtEstadoChip.setText(p.getEstado());
            switch (p.getEstado()) {
                case "EN_PROCESO":
                    txtEstadoChip.setBackgroundResource(R.drawable.bg_estado_activa);
                    break;
                case "COMPLETADA":
                    txtEstadoChip.setBackgroundResource(R.drawable.bg_estado_finalizada);
                    break;
                case "CANCELADA":
                    txtEstadoChip.setBackgroundResource(R.drawable.bg_estado_cancelada);
                    break;
                default: // PLANIFICADA
                    txtEstadoChip.setBackgroundResource(R.drawable.bg_estado_pendiente);
                    break;
            }

            btnEditar.setVisibility(puedeEditar ? View.VISIBLE : View.GONE);
            btnEliminar.setVisibility(puedeEliminar ? View.VISIBLE : View.GONE);
            if (puedeEditar)   btnEditar.setOnClickListener(v -> listener.onEditar(p));
            if (puedeEliminar) btnEliminar.setOnClickListener(v -> listener.onEliminar(p));
        }
    }
}
