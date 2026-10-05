package com.ecogestion.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecogestion.app.R;
import com.ecogestion.app.models.Auditoria;

import java.util.List;

public class AuditoriaAdapter extends RecyclerView.Adapter<AuditoriaAdapter.VH> {

    private List<Auditoria> lista;

    public AuditoriaAdapter(List<Auditoria> lista) {
        this.lista = lista;
    }

    public void actualizar(List<Auditoria> nueva) {
        this.lista = nueva;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_auditoria, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.bind(lista.get(position));
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvIcono, tvAccion, tvChip, tvDetalle, tvUsuario, tvFecha;

        VH(@NonNull View v) {
            super(v);
            tvIcono   = v.findViewById(R.id.tvAuditoriaIcono);
            tvAccion  = v.findViewById(R.id.tvAuditoriaAccion);
            tvChip    = v.findViewById(R.id.tvAuditoriaChip);
            tvDetalle = v.findViewById(R.id.tvAuditoriaDetalle);
            tvUsuario = v.findViewById(R.id.tvAuditoriaUsuario);
            tvFecha   = v.findViewById(R.id.tvAuditoriaFecha);
        }

        void bind(Auditoria a) {
            tvAccion.setText(a.getAccionLabel());
            tvDetalle.setText(a.getDetalle() != null ? a.getDetalle() : "—");
            tvUsuario.setText("👤 " + (a.getNombreUsuario() != null
                    ? a.getNombreUsuario() : "Sistema"));

            // Formatear fecha: "2024-05-10 14:32:00" → "10/05/2024  14:32"
            tvFecha.setText("🕐 " + formatearFecha(a.getFecha()));

            // Chip e ícono según categoría
            switch (a.getCategoria()) {
                case "CREAR":
                    tvChip.setText("NUEVO");
                    tvChip.setBackgroundResource(R.drawable.bg_estado_activa);
                    tvIcono.setText("✚");
                    tvIcono.setBackgroundColor(Color.parseColor("#E8F5E9"));
                    break;
                case "MODIFICAR":
                    tvChip.setText("EDICIÓN");
                    tvChip.setBackgroundResource(R.drawable.bg_estado_en_curso);
                    tvIcono.setText("✎");
                    tvIcono.setBackgroundColor(Color.parseColor("#E3F2FD"));
                    break;
                case "ELIMINAR":
                    tvChip.setText("BAJA");
                    tvChip.setBackgroundResource(R.drawable.bg_estado_cancelada);
                    tvIcono.setText("✖");
                    tvIcono.setBackgroundColor(Color.parseColor("#FFEBEE"));
                    break;
                default: // LOGIN, SISTEMA, etc.
                    tvChip.setText("SISTEMA");
                    tvChip.setBackgroundResource(R.drawable.bg_estado_pendiente);
                    tvIcono.setText("⚙");
                    tvIcono.setBackgroundColor(Color.parseColor("#FFF8E1"));
                    break;
            }
        }

        /** Convierte "2024-05-10 14:32:00" a "10/05/2024  14:32" */
        private String formatearFecha(String raw) {
            if (raw == null || raw.isEmpty()) return "—";
            try {
                // SQLite datetime → "yyyy-MM-dd HH:mm:ss"
                String[] partes = raw.split(" ");
                String fecha = partes[0]; // "2024-05-10"
                String hora  = partes.length > 1 ? partes[1].substring(0, 5) : ""; // "14:32"
                String[] fechaParts = fecha.split("-");
                if (fechaParts.length == 3) {
                    return fechaParts[2] + "/" + fechaParts[1] + "/" + fechaParts[0]
                            + (hora.isEmpty() ? "" : "  " + hora);
                }
            } catch (Exception ignored) { }
            return raw;
        }
    }
}
