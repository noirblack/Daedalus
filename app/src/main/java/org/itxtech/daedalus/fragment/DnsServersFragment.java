package org.itxtech.daedalus.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.itxtech.daedalus.Daedalus;
import org.itxtech.daedalus.R;
import org.itxtech.daedalus.activity.ConfigActivity;
import org.itxtech.daedalus.util.CustomDnsServer;
import org.itxtech.daedalus.util.DnsServerHelper;

/**
 * Daedalus Project
 *
 * @author iTX Technologies
 * @link https://itxtech.org
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
public class DnsServersFragment extends Fragment {
    private DnsServersFragment.DnsServerAdapter adapter;
    private CustomDnsServer server = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_dns_servers, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_dns_servers);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        adapter = new DnsServerAdapter();
        recyclerView.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof ViewHolder) {
                    if (DnsServerHelper.isInUsing(Daedalus.configurations.getCustomDnsServers().get(((ViewHolder) viewHolder).getIndex()))) {
                        return 0;
                    }
                }
                return makeMovementFlags(0, ItemTouchHelper.START | ItemTouchHelper.END);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                server = Daedalus.configurations.getCustomDnsServers().get(position);
                Daedalus.configurations.getCustomDnsServers().remove(position);
                Snackbar.make(view, R.string.action_removed, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_undo, new SnackbarClickListener(position)).show();
                adapter.notifyItemRemoved(position);
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_add_server);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ConfigActivity.class).putExtra(ConfigActivity.LAUNCH_ACTION_CUSTOM_DNS_SERVER_ID,
                        ConfigActivity.CUSTOM_DNS_SERVER_ID_NONE));
            }
        });
        return view;
    }

    private class SnackbarClickListener implements View.OnClickListener {
        private final int position;

        private SnackbarClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Daedalus.configurations.getCustomDnsServers().add(position, server);
            adapter.notifyItemInserted(position);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Daedalus.configurations.save();
        adapter = null;
        server = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        adapter.notifyDataSetChanged();
    }

    private class DnsServerAdapter extends RecyclerView.Adapter<ViewHolder> {
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            CustomDnsServer server = Daedalus.configurations.getCustomDnsServers().get(position);
            holder.setIndex(position);
            holder.textViewName.setText(server.getName());
            holder.textViewAddress.setText(server.getAddress() + ":" + server.getPort());
        }

        @Override
        public int getItemCount() {
            return Daedalus.configurations.getCustomDnsServers().size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_server, parent, false);
            return new ViewHolder(view);
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView textViewName;
        private final TextView textViewAddress;
        private int index;

        ViewHolder(View view) {
            super(view);
            textViewName = (TextView) view.findViewById(R.id.textView_custom_dns_name);
            textViewAddress = (TextView) view.findViewById(R.id.textView_custom_dns_address);
            view.setOnClickListener(this);
        }

        void setIndex(int index) {
            this.index = index;
        }

        int getIndex() {
            return index;
        }

        @Override
        public void onClick(View v) {
            if (!DnsServerHelper.isInUsing(Daedalus.configurations.getCustomDnsServers().get(index))) {
                Daedalus.getInstance().startActivity(new Intent(Daedalus.getInstance(), ConfigActivity.class)
                        .putExtra(ConfigActivity.LAUNCH_ACTION_CUSTOM_DNS_SERVER_ID, index));
            }
        }
    }
}
