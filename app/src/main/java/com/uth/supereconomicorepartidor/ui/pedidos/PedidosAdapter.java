package com.uth.supereconomicorepartidor.ui.pedidos;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

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
            binding.tvDireccion.setText("Consultar dirección en detalle"); 
            binding.tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", pedido.getTotal()));
            binding.tvFecha.setText(pedido.getCreadoAt() != null ? pedido.getCreadoAt().split("T")[0] : "");
            
            updateSteps(pedido.getEstado());
            
            binding.btnVerDetalle.setOnClickListener(v -> listener.onPedidoClick(pedido));
            itemView.setOnClickListener(v -> listener.onPedidoClick(pedido));
        }

        private void updateSteps(String estado) {
            int activeColor = ContextCompat.getColor(itemView.getContext(), R.color.green_primary);
            int inactiveColor = ContextCompat.getColor(itemView.getContext(), R.color.grey_light);

            resetStep(binding.step1, inactiveColor);
            resetStep(binding.step2, inactiveColor);
            resetStep(binding.step3, inactiveColor);
            resetStep(binding.step4, inactiveColor);

            if ("pendiente".equalsIgnoreCase(estado)) {
                highlightStep(binding.step1, activeColor);
            } else if ("preparando".equalsIgnoreCase(estado)) {
                highlightStep(binding.step1, activeColor);
                highlightStep(binding.step2, activeColor);
            } else if ("en_camino".equalsIgnoreCase(estado)) {
                highlightStep(binding.step1, activeColor);
                highlightStep(binding.step2, activeColor);
                highlightStep(binding.step3, activeColor);
            } else if ("entregado".equalsIgnoreCase(estado)) {
                highlightStep(binding.step1, activeColor);
                highlightStep(binding.step2, activeColor);
                highlightStep(binding.step3, activeColor);
                highlightStep(binding.step4, activeColor);
            }
        }

        private void resetStep(ImageView img, int color) {
            img.setColorFilter(color);
        }

        private void highlightStep(ImageView img, int color) {
            img.setColorFilter(color);
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
