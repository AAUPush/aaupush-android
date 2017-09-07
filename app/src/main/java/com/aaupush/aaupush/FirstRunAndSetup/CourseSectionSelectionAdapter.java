package com.aaupush.aaupush.FirstRunAndSetup;


import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.aaupush.aaupush.PushUtils;
import com.aaupush.aaupush.R;

import java.util.ArrayList;

public class CourseSectionSelectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Object> list;

    // Static variable that represent if a view is a Section or Course
    private static int TYPE_COURSE = 1;
    private static int TYPE_SECTION = 2;

    public CourseSectionSelectionAdapter(ArrayList<Object> list) {
        this.list = list;
    }

    /**
     * Called when RecyclerView needs a new {@link android.support.v7.widget.RecyclerView.ViewHolder}
     * of the given type to represent an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(RecyclerView.ViewHolder, int)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(RecyclerView.ViewHolder, int) (ViewHolder, int)
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        CheckableViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.checkable_list_item, parent, false);
        viewHolder = new CheckableViewHolder(view);

        return viewHolder;
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link android.support.v7.widget.RecyclerView.ViewHolder#itemView}
     * to reflect the item at the given position.
     * <p>
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 1:
                configureCourse((CheckableViewHolder)holder, position);
                break;
            case 2:
                configureSection((CheckableViewHolder)holder, position);
                break;
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position) instanceof String){
            return TYPE_SECTION;
        } else if (list.get(position) instanceof CourseSelectionFragment.Course){
            return TYPE_COURSE;
        }

        return -1;
    }

    public ArrayList<Object> getList() {
        return list;
    }

    private void configureCourse(CheckableViewHolder viewHolder, int position) {
        // Get the course from the list
        final CourseSelectionFragment.Course course = (CourseSelectionFragment.Course)list.get(position);

        // Set default value for isChecked
        course.isSelected = course.isInPrimarySection;

        // Hide the text view because is not needed for courses
        viewHolder.getTextView().setVisibility(View.GONE);

        // Set checkbox properties
        viewHolder.getCheckBox().setText(course.courseName);
        viewHolder.getCheckBox().setChecked(course.isSelected);

        // Listen for onCheckChange
        viewHolder.getCheckBox().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton checkbox, boolean isChecked) {
                course.isSelected = isChecked;
            }
        });
    }

    private void configureSection(CheckableViewHolder viewHolder, int position) {
        // Get the section code from the list
        final String section = (String)list.get(position);

        // Hide the checkbox
        viewHolder.getCheckBox().setVisibility(View.GONE);

        // Set the section code to the text view
        viewHolder.getTextView().setText(section);

        // Set on click listener on the text view
        viewHolder.getTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send a broadcast with an intent extra of section code
                Intent clickedOnSectionBroadcast = new Intent(PushUtils.CLICKED_ON_SECTION_BROADCAST);
                clickedOnSectionBroadcast.putExtra("section_code", section);
                view.getContext().getApplicationContext().sendBroadcast(clickedOnSectionBroadcast);
            }
        });
    }

    // View Holders

    class CheckableViewHolder extends RecyclerView.ViewHolder {

        private CheckBox checkBox;
        private TextView textView;

        CheckableViewHolder(View itemView) {
            super(itemView);

            checkBox = (CheckBox)itemView.findViewById(R.id.checkbox);
            textView = (TextView)itemView.findViewById(R.id.text_view);
        }

        CheckBox getCheckBox() { return checkBox; }
        TextView getTextView() { return textView; }
    }


}
