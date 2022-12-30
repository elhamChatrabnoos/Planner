package com.android.prj.planRiz.daysRelatedClass;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.android.prj.planRiz.DataBase;
import com.android.prj.planRiz.dialogs.InsertDialogFragment;
import com.android.prj.planRiz.R;
import com.android.prj.planRiz.workRelatedClass.WorkDao;
import com.android.prj.planRiz.workRelatedClass.WorkModel;
import com.android.prj.planRiz.workRelatedClass.WorksAdapter;

import java.util.ArrayList;
import java.util.List;


public class DaysAdapter extends RecyclerView.Adapter<DaysAdapter.DaysViewHolder>{

    List<DaysModel> daysModelList = new ArrayList<>();
    private List<WorkModel> workModelList = new ArrayList<>();

    private WorkDao workDao;
    private WorksAdapter worksAdapter;

    private DaysModel daysModel;
    Context context;
    int workPosition;


    public DaysAdapter(Context context, int workPosition) {
        this.context = context;
        this.workPosition = workPosition;
    }

    @NonNull
    @Override
    public DaysViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
// add recyclerView to layout and bind it to holder
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.days_recycler, parent, false);
        return new DaysViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DaysViewHolder holder, int position) {
        daysModel = new DaysModel();
        daysModel = daysModelList.get(position);
        holder.bindDate(daysModel);
    }

    @Override
    public int getItemCount() {
        return daysModelList.size();
    }

    public class DaysViewHolder extends RecyclerView.ViewHolder{

        private final TextView dates;
        private final TextView grDate;
        private final RecyclerView worksRecycler;
        private final ImageView addBtn;
        private ImageView deleteAll;

        public DaysViewHolder(@NonNull View itemView){
            super(itemView);
            dates = itemView.findViewById(R.id.dateTxt);
            grDate = itemView.findViewById(R.id.grDate);
            worksRecycler = itemView.findViewById(R.id.workRecycler);
            addBtn = itemView.findViewById(R.id.card_add);
            deleteAll = itemView.findViewById(R.id.delete_all_icon);
        }

        public void bindDate(DaysModel model){

            if(model != null){
                dates.setText(model.getDate());
                grDate.setText(model.getGrDate());

                // define work list recycler view
                StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(1,
                        StaggeredGridLayoutManager.VERTICAL);
                worksRecycler.setLayoutManager(gridLayoutManager);
                gridLayoutManager.scrollToPosition(workPosition);
                workDao = DataBase.getDataBase(itemView.getContext()).getWorkDao();
                worksAdapter = new WorksAdapter(itemView.getContext());
                workModelList = workDao.getWorkFromDate(daysModel.getDate());
                worksAdapter.showItem(workModelList);
                worksRecycler.setAdapter(worksAdapter);

                addBtn.setOnClickListener(view -> {
                    FragmentActivity activity = (FragmentActivity) itemView.getContext();
                    FragmentManager manager = activity.getSupportFragmentManager();
                    InsertDialogFragment insertDialog = new InsertDialogFragment();

                    Bundle bundle = new Bundle();
                    bundle.putString("workDate", dates.getText().toString());
                    insertDialog.setArguments(bundle);
                    insertDialog.show(manager, null);
                });

                deleteAll.setOnClickListener(view -> {
                    showConfirmDialog();
                });

            }
        }

        private void showConfirmDialog(){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(itemView.getContext());
            alertDialog.setMessage("تمامی فعالیت ها در این تاریخ حذف شوند؟")
                    .setPositiveButton("بله", (dialogInterface, i) -> {
                        deleteAllInDate();
                    })
                    .setNegativeButton("خیر", (dialogInterface, i) -> {} )
                    .create()
                    .show();
        }

        private void deleteAllInDate(){
            List<WorkModel> modelList = workDao.getWorkFromDate(daysModelList.get(getLayoutPosition()).getDate());
            for (int i = 0; i < modelList.size(); i++){
                workDao.deleteWork(modelList.get(i));
                notifyDataSetChanged();
            }
        }
    }


    public void showItem(List<DaysModel> daysModels){
        this.daysModelList.addAll(daysModels);
        notifyDataSetChanged();
    }

    public void insertItem(DaysModel model){
        daysModelList.add(model);
        notifyDataSetChanged();
    }

    public void deleteAllItems(){
        daysModelList.clear();
        notifyDataSetChanged();
    }

    public void searchItems(List<DaysModel> daysModelList){
        this.daysModelList = daysModelList;
        notifyDataSetChanged();
    }


}
