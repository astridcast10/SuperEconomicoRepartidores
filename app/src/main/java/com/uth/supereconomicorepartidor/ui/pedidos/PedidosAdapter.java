package com.uth.supereconomicorepartidor.ui.pedidos;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.uth.supereconomicorepartidor.R;
import com.uth.supereconomicorepartidor.databinding.ItemPedidoBinding;
import com.uth.supereconomicorepartidor.domain.entities.PedidoRepartidor;

import java.util.Locale;

public class PedidosAdapter extends ListAdapter<PedidoRepartidor, PedidosAdapter.ViewHolder> {

    public interface OnPedidoClickListener {
        void onPedidoClick(PedidoRepartidor pedido);
    }

    private final OnPedidoClickListener listener;

    public PedidosAdapter(OnPedidoClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPedidoBinding binding = ItemPedidoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPedidoBinding binding;

        public ViewHolder(ItemPedidoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(PedidoRepartidor pedido, OnPedidoClickListener listener) {
            binding.tvPedidoId.setText(String.format(Locale.getDefault(), "#%d", pedido.getId()));
            binding.tvDireccion.setText("Cargando dirección..."); // Luego implementaremos el join con direcciones
            binding.tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", pedido.getTotal()));
            
            String estado = pedido.getEstado();
            binding.chipEstado.setText(estado.substring(0, 1).toUpperCase() + estado.substring(1));
            
            int colorRes = R.color.status_pendiente;
            if ("en_camino".equalsIgnoreCase(estado)) colorRes = R.color.status_en_camino;
            else if ("entregado".equalsIgnoreCase(estado)) colorRes = R.color.status_entregado;
            
            binding.chipEstado.setChipBackgroundColorResource(colorRes);
            
            itemView.setOnClickListener(v -> listener.onPedidoClick(pedido));
        }
    }

    private static final DiffUtil.ItemCallback<PedidoRepartidor> DIFF_CALLBACK = new DiffUtil.ItemCallback<PedidoRepartidor>() {
        @Override
        public boolean areItemsTheSame(@NonNull PedidoRepartidor oldItem, @NonNull PedidoRepartidor newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull PedidoRepartidor oldItem, @NonNull PedidoRepartidor newItem) {
            return oldItem.getEstado().equals(newItem.getEstado()) && 
                   oldItem.getTotal().equals(newItem.getTotal());
        }
    };
}
