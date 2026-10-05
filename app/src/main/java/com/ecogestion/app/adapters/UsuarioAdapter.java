package com.ecogestion.app.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecogestion.app.R;
import com.ecogestion.app.models.Usuario;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    public interface OnUsuarioListener {
        void onEditar(Usuario usuario);
        void onEliminar(Usuario usuario);
    }

    private final List<Usuario> lista;
    private final OnUsuarioListener listener;

    public UsuarioAdapter(List<Usuario> lista, OnUsuarioListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        holder.bind(lista.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtAvatar;
        private final TextView txtNombreUsuario;
        private final TextView txtUsernameUsuario;
        private final TextView txtRolUsuario;
        private final TextView txtEmailUsuario;
        private final TextView txtEstadoUsuario;
        private final MaterialButton btnEditar;
        private final MaterialButton btnEliminar;

        UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAvatar         = itemView.findViewById(R.id.txtAvatar);
            txtNombreUsuario  = itemView.findViewById(R.id.txtNombreUsuario);
            txtUsernameUsuario= itemView.findViewById(R.id.txtUsernameUsuario);
            txtRolUsuario     = itemView.findViewById(R.id.txtRolUsuario);
            txtEmailUsuario   = itemView.findViewById(R.id.txtEmailUsuario);
            txtEstadoUsuario  = itemView.findViewById(R.id.txtEstadoUsuario);
            btnEditar         = itemView.findViewById(R.id.btnEditarUsuario);
            btnEliminar       = itemView.findViewById(R.id.btnEliminarUsuario);
        }

        void bind(Usuario usuario, OnUsuarioListener listener) {
            // Nombre completo y avatar
            String nombreCompleto = usuario.getNombre() + " " + usuario.getApellido();
            txtNombreUsuario.setText(nombreCompleto);
            String inicial = (usuario.getNombre() != null && !usuario.getNombre().isEmpty())
                    ? String.valueOf(usuario.getNombre().charAt(0)).toUpperCase()
                    : "?";
            txtAvatar.setText(inicial);

            // Username
            txtUsernameUsuario.setText("@" + usuario.getNombreUsuario());

            // Email
            String email = usuario.getEmail() != null && !usuario.getEmail().isEmpty()
                    ? usuario.getEmail() : "Sin email";
            txtEmailUsuario.setText(email);

            // Estado activo
            if (usuario.isActivo()) {
                txtEstadoUsuario.setText("● Activo");
                txtEstadoUsuario.setTextColor(
                        itemView.getContext().getResources().getColor(R.color.green_primary, null));
            } else {
                txtEstadoUsuario.setText("● Inactivo");
                txtEstadoUsuario.setTextColor(Color.parseColor("#9E9E9E"));
            }

            // Chip de rol con color según tipo
            String rol = usuario.getRol() != null ? usuario.getRol() : "OPERADOR";
            txtRolUsuario.setText(rol);
            switch (rol) {
                case "ADMIN":
                    txtRolUsuario.setBackgroundResource(R.drawable.bg_estado_activa);
                    break;
                case "SUPERVISOR":
                case "COORDINADOR":
                    txtRolUsuario.setBackgroundResource(R.drawable.bg_estado_pendiente);
                    break;
                case "INSPECTOR":
                    txtRolUsuario.setBackgroundResource(R.drawable.bg_estado_en_curso);
                    break;
                default: // OPERADOR u otros
                    txtRolUsuario.setBackgroundResource(R.drawable.bg_estado_finalizada);
                    break;
            }

            btnEditar.setOnClickListener(v -> listener.onEditar(usuario));
            btnEliminar.setOnClickListener(v -> listener.onEliminar(usuario));
        }
    }
}
