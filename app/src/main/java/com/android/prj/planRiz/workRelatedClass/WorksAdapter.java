package com.android.prj.planRiz.workRelatedClass;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.prj.planRiz.R;
import com.android.prj.planRiz.dialogs.UpdateDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class WorksAdapter extends RecyclerView.Adapter<WorksAdapter.WorksViewHolder> {

    private List<WorkModel> worksList = new ArrayList<>();
    CallOperation callOperation;

    public WorksAdapter(Context context) {
        callOperation = (CallOperation) context;
    }

    @NonNull
    @Override
    public WorksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.work_recycler, parent, false);
        return new WorksViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorksViewHolder holder, int position) {
        holder.setItems();
    }

    @Override
    public int getItemCount() {
        return worksList.size();
    }

    public class WorksViewHolder extends RecyclerView.ViewHolder{
        TextView workName, workAlarm;
        ImageView doneImg, undoneImg, deleteImg;

        public WorksViewHolder(@NonNull View itemView) {
            super(itemView);
            workName = itemView.findViewById(R.id.workNameTxt);
            workAlarm = itemView.findViewById(R.id.workAlarmClock);
            deleteImg = itemView.findViewById(R.id.delete_icon);
            doneImg = itemView.findViewById(R.id.done_img);
            undoneImg = itemView.findViewById(R.id.undone_img);
        }

        public void setItems(){

            // do operation on clicked item not just last item
            WorkModel workModel2 = worksList.get(getLayoutPosition());
            boolean isDone = workModel2.isDone();
            boolean isUndone = workModel2.isUndone();

            workName.setText(workModel2.getWork_name());
            workAlarm.setText(workModel2.getAlarm());

            if(isDone){
                doneImg.setImageResource(R.drawable.done_img);
            }
            else if(isUndone){
                undoneImg.setImageResource(R.drawable.undone_img);
            }
            else{
                doneImg.setImageResource(R.drawable.gray_done);
                undoneImg.setImageResource(R.drawable.gray_undone);
            }

            doneImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!isDone && !isUndone){
                        doneImg.setImageResource(R.drawable.done_img);
                        workModel2.setDone(true);
                    }
                    else if(!isDone && isUndone){
                        undoneImg.setImageResource(R.drawable.gray_undone);
                        workModel2.setUndone(false);
                        doneImg.setImageResource(R.drawable.done_img);
                        workModel2.setDone(true);
                    }
                    else if(isDone){
                        doneImg.setImageResource(R.drawable.gray_done);
                        workModel2.setDone(false);
                    }

                    callOperation.updateDoneUndone(workModel2, getLayoutPosition());
                }
            });

            undoneImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isDone && !isUndone){
                        doneImg.setImageResource(R.drawable.gray_done);
                        workModel2.setDone(false);
                        undoneImg.setImageResource(R.drawable.undone_img);
                        workModel2.setUndone(true);
                    }
                    else if(!isUndone && !isDone){
                        undoneImg.setImageResource(R.drawable.undone_img);
                        workModel2.setUndone(true);
                    }
                    else if(isUndone){
                        undoneImg.setImageResource(R.drawable.gray_undone);
                        workModel2.setUndone(false);
                    }
                    callOperation.updateDoneUndone(workModel2, getLayoutPosition());
                }
            });

            deleteImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callOperation.deleteRecord(worksList.get(getLayoutPosition()), getLayoutPosition());
                }
            });



            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openUpdateDialog();
                }
            });
        }

        public void openUpdateDialog(){
            FragmentActivity activity = (FragmentActivity) itemView.getContext();
            FragmentManager manager = activity.getSupportFragmentManager();

            UpdateDialogFragment dialogFragment = new UpdateDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable("work_info" , worksList.get(getLayoutPosition()));
            dialogFragment.setArguments(bundle);
            dialogFragment.show(manager, null);
        }
    }

    public void showItem(List<WorkModel> worksList){
        this.worksList.addAll(worksList);
        notifyDataSetChanged();
    }

    public void addItems(WorkModel model){
        worksList.add(model);
        notifyDataSetChanged();
    }

    public void deleteItems(WorkModel model){
       for(int i = 0; i < worksList.size(); i++){
           if(worksList.get(i).getId() == model.getId()){
               worksList.remove(i);
               notifyItemRemoved(i);
           }
       }
    }

    public void updateItems(WorkModel model){
        for( int i = 0; i < worksList.size(); i++){
            if(model.getId() == worksList.get(i).getId()){
                worksList.set(i, model);
                notifyItemChanged(i);
            }
        }
    }

    public void searchItems(List<WorkModel> worksList){
        this.worksList = worksList;
        notifyDataSetChanged();
    }

    public void deleteAllItems(){
        worksList.clear();
        notifyDataSetChanged();
    }

    public interface CallOperation{
         void deleteRecord(WorkModel workModel, int workPosition);
         void updateDoneUndone(WorkModel workModel, int workPosition);
    }

}
