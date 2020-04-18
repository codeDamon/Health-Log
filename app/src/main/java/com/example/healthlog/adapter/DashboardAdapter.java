package com.example.healthlog.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.healthlog.HealthLog;
import com.example.healthlog.R;
import com.example.healthlog.interfaces.OnItemClickListener;
import com.example.healthlog.model.Patient;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.DashboardViewHolder> {

    OnItemClickListener onItemClickListener;

    private List<Patient> allPatientList;
    private List<Patient> currentPatientList;

    private String currentFilter = "Active";

    Context context;

    public DashboardAdapter(Context context, List<Patient> patientList, OnItemClickListener listener) {
        this.context = context;
        this.allPatientList = patientList;
        currentPatientList = new ArrayList<>();

        onItemClickListener = listener;
    }

    @NonNull
    @Override
    public DashboardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_list_item, parent, false);
        return new DashboardViewHolder(itemView);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull DashboardViewHolder holder, int position) {
        // COMPLETED(Shashank) add colors to res directory rather than hardcoding
        Patient patient = currentPatientList.get(position);
        holder.patientName.setText(patient.getId());
        holder.patientStatus.setText(patient.getStatus());

        holder.patientStatus.setTextColor(ContextCompat.getColor(context, patient.getStatusColor()));
        holder.patientColorStatus.setBackgroundResource(patient.getStatusDrawable());

        holder.patientLogDescription.setText(patient.getRecentLog());
        holder.patientDateAdded.setText("Added: "+ HealthLog.getDate(patient.getDateAdded()) + ", " + HealthLog.getTime(patient.getDateAdded()));
        holder.bind(patient, onItemClickListener);
    }


    void listenForStatusAndLogChanges(final Patient p) {
        FirebaseFirestore mRef = FirebaseFirestore.getInstance();
        mRef.collection("Hospital")
                .document(HealthLog.ID)
                .collection("Patient")
                .document(p.getId())
                .addSnapshotListener(
                        new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(
                                    @Nullable DocumentSnapshot documentSnapshot,
                                    @Nullable FirebaseFirestoreException e) {
                                String status = documentSnapshot.getString("status");
                                String log = documentSnapshot.getString("recentLog");
                                p.setStatus(status);
                                p.setRecentLog(log);
                                notifyDataSetChanged();
                            }
                        });
    }

    @Override
    public int getItemCount() {
        return currentPatientList.size();
    }

    public void add(Patient p) {
        for (Patient patient : allPatientList) {
            if (p.getId().equals(patient.getId())) {
                return;
            }
        }
        listenForStatusAndLogChanges(p);
        allPatientList.add(p);
        if (currentFilter.equals("All")) {
            currentPatientList.clear();
            currentPatientList.addAll(allPatientList);
            notifyDataSetChanged();
            return;
        }
        if (p.getStatus().equals(currentFilter)) {
            currentPatientList.add(p);
            notifyDataSetChanged();
        }
    }

    // COMPLETED(Danish) handle filter  here
    public void applyFilter(String filter) {
        currentFilter = filter;
        if (filter.equals("All")) {
            for (Patient p : allPatientList) {
                if (!currentPatientList.contains(p)) {
                    currentPatientList.add(p);
                }
            }

            notifyDataSetChanged();
            return;
        }
        currentPatientList.clear();
        for (Patient p : allPatientList) {
            if (p.getStatus().equals(filter)) {
                currentPatientList.add(p);
            }
        }
        notifyDataSetChanged();
    }

    public void filter(String name) {
        currentPatientList.clear();
        if (name.equals("")) {
            applyFilter(currentFilter);
            return;
        }
        for (Patient p : allPatientList) {
            if (p.getId().toLowerCase().contains(name.toLowerCase())
                    || p.getName().toLowerCase().contains(name.toLowerCase())) {
                currentPatientList.add(p);
            }
        }
        notifyDataSetChanged();
    }

    public void setCurrentFilter(String currentFilter) {
        this.currentFilter = currentFilter;
    }

    // COMPLETED(Shashank) create Add and AddAll method to add new patients in array

    public class DashboardViewHolder extends RecyclerView.ViewHolder {

        TextView patientName;
        TextView patientStatus;
        TextView patientLogDescription;
        TextView patientDateAdded;
        View patientColorStatus;

        View view;

        public DashboardViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            patientName = itemView.findViewById(R.id.patient_list_item_name_textView);
            patientStatus = itemView.findViewById(R.id.patient_list_item_statusText_textView);
            patientLogDescription = itemView.findViewById(R.id.patient_list_item_logDescription_textView);
            patientColorStatus = itemView.findViewById(R.id.patient_list_item_statusCircle_view);
            patientDateAdded = itemView.findViewById(R.id.patient_list_item_dateAdded_textView);
        }

        void bind(final Patient currentPatient, final OnItemClickListener listener) {
            view.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listener.onItemClicked(currentPatient, itemView);
                        }
                    });
        }
    }
}
