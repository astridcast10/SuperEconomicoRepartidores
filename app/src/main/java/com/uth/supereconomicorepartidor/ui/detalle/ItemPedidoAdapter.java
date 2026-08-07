package com.uth.supereconomicorepartidor.ui.detalle;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.uth.supereconomicorepartidor.databinding.ItemProductoDetalleBinding;
import com.uth.supereconomicorepartidor.domain.entities.ItemPedido;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemPedidoAdapter extends RecyclerView.Adapter<ItemPedidoAdapter.ViewHolder> {

    private final List<ItemPedido> items = new ArrayList<>();

    public void submitList(List<ItemPedido> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductoDetalleBinding binding = ItemProductoDetalleBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductoDetalleBinding binding;

        public ViewHolder(ItemProductoDetalleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ItemPedido item) {
            binding.tvCantidad.setText(String.format(Locale.getDefault(), "%dx", item.getCantidad()));
            binding.tvProductoNombre.setText(item.getProductoNombre());
            binding.tvPrecio.setText(String.format(Locale.getDefault(), "$%.2f", item.getSubtotal()));
        }
    }
}
