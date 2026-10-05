package com.ecogestion.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecogestion.app.R;
import com.ecogestion.app.models.Tarea;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {

    public interface OnTareaListener {
        void onEditar(Tarea tarea);
        void onEliminar(Tarea tarea);
    }

    private final List<Tarea> lista;
    private final OnTareaListener listener;
    private boolean puedeEditar   = true;
    private boolean puedeEliminar = true;

    public TareaAdapter(List<Tarea> lista, OnTareaListener listener) {
        this.lista    = lista;
        this.listener = listener;
    }

    public TareaAdapter(List<Tarea> lista, OnTareaListener listener,
                        boolean puedeEditar, boolean puedeEliminar) {
        this(lista, listener);
        this.puedeEditar   = puedeEditar;
        this.puedeEliminar = puedeEliminar;
    }

    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarea, parent, false);
        return new TareaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        holder.bind(lista.get(position), listener, puedeEditar, puedeEliminar);
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class TareaViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtTitulo;
        private final TextView txtPrioridadChip;
        private final TextView txtTipo;
        private final TextView txtEstadoChip;
        private final TextView txtZona;
        private final TextView txtPlantacion;
        private final TextView txtAsignado;
        private final TextView txtFecha;
        private final MaterialButton btnEditar;
        private final MaterialButton btnEliminar;

        TareaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo        = itemView.findViewById(R.id.txtTituloTarea);
            txtPrioridadChip = itemView.findViewById(R.id.txtPrioridadChip);
            txtTipo          = itemView.findViewById(R.id.txtTipoTarea);
            txtEstadoChip    = itemView.findViewById(R.id.txtEstadoChip);
            txtZona          = itemView.findViewById(R.id.txtZonaTarea);
            txtPlantacion    = itemView.findViewById(R.id.txtPlantacionTarea);
            txtAsignado      = itemView.findViewById(R.id.txtAsignadoTarea);
            txtFecha         = itemView.findViewById(R.id.txtFechaTarea);
            btnEditar        = itemView.findViewById(R.id.btnEditarTarea);
            btnEliminar      = itemView.findViewById(R.id.btnEliminarTarea);
        }

        void bind(Tarea t, OnTareaListener listener,
                  boolean puedeEditar, boolean puedeEliminar) {
            txtTitulo.setText(t.getTitulo());
            txtTipo.setText(t.getTipo());

            // Chip prioridad
            txtPrioridadChip.setText(t.getPrioridad());
            switch (t.getPrioridad()) {
                case "ALTA":
                    txtPrioridadChip.setBackgroundResource(R.drawable.bg_prioridad_alta);
                    break;
                case "MEDIA":
                    txtPrioridadChip.setBackgroundResource(R.drawable.bg_prioridad_media);
                    break;
                default: // BAJA
                    txtPrioridadChip.setBackgroundResource(R.drawable.bg_prioridad_baja);
                    break;
            }

            // Chip estado
            txtEstadoChip.setText(t.getEstado());
            switch (t.getEstado()) {
                case "EN_CURSO":
                    txtEstadoChip.setBackgroundResource(R.drawable.bg_estado_activa);
                    break;
                case "COMPLETADA":
                    txtEstadoChip.setBackgroundResource(R.drawable.bg_estado_finalizada);
                    break;
                case "CANCELADA":
                    txtEstadoChip.setBackgroundResource(R.drawable.bg_estado_cancelada);
                    break;
                default: // PENDIENTE
                    txtEstadoChip.setBackgroundResource(R.drawable.bg_estado_pendiente);
                    break;
            }

            // Zona
            String zona = t.getZonaNombre();
            if (zona != null && !zona.isEmpty()) {
                txtZona.setVisibility(View.VISIBLE);
                txtZona.setText("Zona: " + zona);
            } else {
                txtZona.setVisibility(View.GONE);
            }

            // Plantación
            String plantacion = t.getPlantacionNombre();
            if (plantacion != null && !plantacion.isEmpty()) {
                txtPlantacion.setVisibility(View.VISIBLE);
                txtPlantacion.setText("Plantación: " + plantacion);
            } else {
                txtPlantacion.setVisibility(View.GONE);
            }

            // Asignado
            String asignado = t.getAsignadoNombre();
            txtAsignado.setText("Asignado: " + (asignado != null && !asignado.trim().isEmpty()
                    ? asignado : "Sin asignar"));

            // Fecha límite
            String limite = t.getFechaLimite();
            txtFecha.setText(limite != null && !limite.isEmpty()
                    ? "Límite: " + limite : "Sin fecha límite");

            btnEditar.setVisibility(puedeEditar ? View.VISIBLE : View.GONE);
            btnEliminar.setVisibility(puedeEliminar ? View.VISIBLE : View.GONE);
            if (puedeEditar)   btnEditar.setOnClickListener(v -> listener.onEditar(t));
            if (puedeEliminar) btnEliminar.setOnClickListener(v -> listener.onEliminar(t));
        }
    }
}
